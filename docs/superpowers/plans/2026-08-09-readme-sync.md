# README 同步功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 README 镜像列表改为由 registry tags/list 自动生成（可反推 tag），并整合进 auto-sync workflow。

**Architecture:** 修改 tag 格式（`/`→`___`、`:`→`__`）使其可反推原始镜像名；`sync.py` 新增 `--update-readme` 子命令，纯标准库 urllib 拉取两个 registry 的 tags/list 生成嵌套列表 README；`auto-sync-job.yml` 在同步成功后更新 README 并关闭 issue。

**Tech Stack:** Python 3 标准库（urllib）、GitHub Actions、Docker Registry HTTP API v2。

## Global Constraints

- 不引入新 Python 依赖（requirements.txt 保持 `# uses docker CLI, no python dependencies needed`）
- 反推规则：最后一个 `__` 是 `:`，其余 `___` 是 `/`；单下划线保留在镜像名
- 无法反推的旧格式 tag（含单下划线）跳过，不计入 README
- README 嵌套列表格式：镜像名 + 缩进的 registry 地址行（`- 阿里云: \`地址\`` / `- ghcr: \`地址\``）
- 无 `## 已同步镜像` 小节时自动重建
- 保留旧数据，不做迁移

---

### Task 1: 修改 tag 格式使其可反推

**Files:**
- Modify: `docker_service.py:15-16`

**Interfaces:**
- Consumes: 无
- Produces: `_format_tag(image)` 将 `/`→`___`、`:`→`__`，其余字符（含单下划线）原样保留

- [ ] **Step 1: 修改 `_format_tag`**

```python
    def _format_tag(self, image):
        return image.replace("/", "___").replace(":", "__")
```

- [ ] **Step 2: 验证**

Run: `python -c "from docker_service import DockerService; s=DockerService({'target_repository':'r'}) ; print(s._format_tag('python:3.12')); print(s._format_tag('1186258278/openclaw-zh:latest')); print(s._format_tag('a_b/c_d:e'))"`
Expected:
```
python__3.12
1186258278___openclaw-zh__latest
a_b___c_d__e
```

- [ ] **Step 3: 提交**

```bash
git add docker_service.py
git commit -m "feat: tag 格式改用双/三下划线分隔以支持反推"
```

### Task 2: 实现 tag 反推函数

**Files:**
- Modify: `docker_service.py:15`（在 `_format_tag` 旁新增 `reverse_tag`）

**Interfaces:**
- Consumes: 无
- Produces: `reverse_tag(tag) -> str|None`：反推原始镜像名；无法反推（无 `__` 分隔）返回 None

- [ ] **Step 1: 写失败测试**

Run（先验证当前无此函数会报错）:
```
python -c "from docker_service import reverse_tag; print(reverse_tag('python__3.12'))"
```
Expected: FAIL（`ImportError: cannot import name 'reverse_tag'`）

- [ ] **Step 2: 写最小实现**

在 `docker_service.py` 顶部（`logger = ...` 之后）新增模块级函数：

```python
def reverse_tag(tag):
    if "__" not in tag:
        return None
    head, _, tail = tag.rpartition("__")
    if not head:
        return None
    return head.replace("___", "/") + ":" + tail
```

- [ ] **Step 3: 运行测试验证通过**

Run:
```
python -c "from docker_service import reverse_tag; assert reverse_tag('python__3.12')=='python:3.12'; assert reverse_tag('1186258278___openclaw-zh__latest')=='1186258278/openclaw-zh:latest'; assert reverse_tag('a_b___c_d__e')=='a_b/c_d:e'; assert reverse_tag('python_3.12') is None; print('OK')"
```
Expected: `OK`

- [ ] **Step 4: 提交**

```bash
git add docker_service.py
git commit -m "feat: 新增 reverse_tag 反推镜像名"
```

### Task 3: sync.py 实现 registry tag 拉取与反推

**Files:**
- Modify: `sync.py`（新增模块级函数 + 复用现有结构）

**Interfaces:**
- Consumes: `config.py:get_config()`、`docker_service.py:reverse_tag`
- Produces:
  - `fetch_tags(registry_url, repository, user, pwd, token=None) -> list[str]`
  - `collect_images() -> dict[str, dict[str,str]]`（image → {"阿里云": addr, "ghcr": addr}）

- [ ] **Step 1: 写失败测试**

Run: `python sync.py --update-readme`
Expected: FAIL（`error: unrecognized arguments` 或类似，说明子命令未实现）

- [ ] **Step 2: 写实现**

在 `sync.py` 中 `main()` 之前新增：

```python
import base64
import json
from urllib.request import Request, urlopen
from urllib.error import HTTPError

from docker_service import reverse_tag


def _basic_auth(user, pwd):
    token = base64.b64encode(f"{user}:{pwd}".encode()).decode()
    return f"Basic {token}"


def fetch_tags(registry_url, repository, user, pwd, token=None):
    url = f"https://{registry_url}/v2/{repository}/tags/list"
    headers = {"Accept": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    else:
        headers["Authorization"] = _basic_auth(user, pwd)
    req = Request(url, headers=headers)
    with urlopen(req, timeout=15) as resp:
        data = json.load(resp)
    return data.get("tags") or []


def collect_images():
    config = get_config()
    result = {}

    try:
        tags = fetch_tags(
            config["registry_url"],
            config["target_repository"].split("/", 1)[1],
            config["registry_user"],
            config["registry_pwd"],
        )
        for tag in tags:
            image = reverse_tag(tag)
            if image:
                result.setdefault(image, {})["阿里云"] = f"{config['target_repository']}:{tag}"
    except HTTPError as e:
        logger.error("阿里云 tags/list 失败: %s", e)

    ghcr_repo = config["ghcr_repository"]
    ghcr_token = config["ghcr_token"]
    if ghcr_repo and ghcr_token:
        try:
            tags = fetch_tags("ghcr.io", ghcr_repo, "", "", ghcr_token)
            for tag in tags:
                image = reverse_tag(tag)
                if image:
                    result.setdefault(image, {})["ghcr"] = f"ghcr.io/{ghcr_repo}:{tag}"
        except HTTPError as e:
            logger.error("ghcr tags/list 失败: %s", e)

    return result
```

- [ ] **Step 3: 运行测试验证（无网络则不验证网络路径）**

Run: `python -c "from sync import reverse_tag; print(reverse_tag('python__3.12'))"`
Expected: `python:3.12`（确认 import 链正常）

- [ ] **Step 4: 提交**

```bash
git add sync.py
git commit -m "feat: 实现 registry tags/list 拉取与镜像名反推"
```

### Task 4: sync.py 生成 README 嵌套列表

**Files:**
- Modify: `sync.py`（新增 `generate_readme` + `update_readme`）

**Interfaces:**
- Consumes: `collect_images()`（Task 3）
- Produces: `generate_readme(images) -> str`；`update_readme()` 读写 README.md

- [ ] **Step 1: 写失败测试**

Run: `python sync.py --update-readme`
Expected: FAIL（子命令分支未实现，报错或提示）

- [ ] **Step 2: 写实现**

在 `main()` 之前新增：

```python
def generate_readme(images):
    lines = ["", "## 已同步镜像", ""]
    for image in sorted(images):
        lines.append(f"- {image}")
        for name, addr in images[image].items():
            lines.append(f"  - {name}: `{addr}`")
    return "\n".join(lines) + "\n"


def update_readme():
    images = collect_images()
    section = generate_readme(images)

    readme_path = "README.md"
    with open(readme_path, encoding="utf-8") as f:
        content = f.read()

    marker = "## 已同步镜像"
    if marker in content:
        idx = content.index(marker)
        head = content[:idx].rstrip() + "\n"
    else:
        head = content.rstrip() + "\n"

    with open(readme_path, "w", encoding="utf-8") as f:
        f.write(head + section)
    return len(images)
```

在 `main()` 中新增分支（放在 `image = sys.argv[1]` 之前）：

```python
    if len(sys.argv) > 1 and sys.argv[1] == "--update-readme":
        count = update_readme()
        write_github_output("msg", f"📋 README 已更新，共 {count} 个镜像")
        return
```

- [ ] **Step 3: 本地验证（构造临时 README）**

Run:
```
Copy-Item README.md README.bak.md; python sync.py --update-readme; Get-Content README.md
```
（真实 registry 可用则能拉到 tags；不可用则输出空列表，README 重建为仅小节头）
Expected: README 显示新嵌套列表格式（或空列表头）
Run 后恢复：`Move-Item README.bak.md README.md -Force`

- [ ] **Step 4: 提交**

```bash
git add sync.py
git commit -m "feat: README 由 registry 自动生成嵌套列表"
```

### Task 5: 更新 auto-sync-job.yml

**Files:**
- Modify: `.github/workflows/auto-sync-job.yml`

**Interfaces:**
- Consumes: `sync.py --update-readme`（Task 4）
- Produces: 无

- [ ] **Step 1: 修改 workflow**

在「执行同步」之后插入 README 更新步骤，并将同步失败时的跳过逻辑考虑在内。完整文件替换为：

```yaml
name: Docker Image Sync

env:
  sys.registry.pwd: ${{ secrets.REGISTRY_PWD }}
  GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
  GHCR_REPO: ${{ github.repository }}

on:
  issues:
    types: [opened]

jobs:
  sync:
    runs-on: ubuntu-latest
    permissions:
      issues: write
      packages: write
      contents: write

    steps:
    - name: 检查标题是否包含冒号
      if: contains(github.event.issue.title, ':')
      run: echo "Issue 标题中包含冒号，继续执行"

    - name: 回复issue开始
      run: gh issue comment ${{ github.event.issue.number }} --body "🤖 任务已收到，正在执行中..." --repo ${{ github.repository }}

    - uses: actions/checkout@v4

    - name: 执行同步
      id: sync
      run: python sync.py "${{ github.event.issue.title }}"

    - name: 更新README
      if: success()
      run: python sync.py --update-readme

    - name: 提交README变更
      if: success()
      run: |
        if git diff --quiet README.md; then
          echo "README.md 无变更"
        else
          git config user.name "github-actions[bot]"
          git config user.email "github-actions[bot]@users.noreply.github.com"
          git add README.md
          git commit -m "chore: 自动更新已同步镜像列表"
          git push
        fi

    - name: 回复issue完成
      if: always()
      run: gh issue comment ${{ github.event.issue.number }} --body "${{ steps.sync.outputs.msg }}" --repo ${{ github.repository }}

    - name: 关闭issue
      if: success()
      run: gh issue close ${{ github.event.issue.number }} --repo ${{ github.repository }}
```

- [ ] **Step 2: 验证 YAML 语法**

Run: `python -c "import yaml,sys; yaml.safe_load(open('.github/workflows/auto-sync-job.yml')); print('OK')"`
（若无 pyyaml 则跳过；用 `python -m py_compile` 无法校验 yaml，可改用记事本目测或 `python -c "import json; import yaml; yaml.safe_load(...)"` 需要装包。若未安装则跳过此步）

- [ ] **Step 3: 提交**

```bash
git add .github/workflows/auto-sync-job.yml
git commit -m "feat: auto-sync 同步后自动更新 README 并关闭 issue"
```

### Task 6: 删除 sync-maintenance.yml

**Files:**
- Delete: `.github/workflows/sync-maintenance.yml`

- [ ] **Step 1: 删除文件**

Run: `Remove-Item .github/workflows/sync-maintenance.yml`

- [ ] **Step 2: 确认删除**

Run: `Test-Path .github/workflows/sync-maintenance.yml`
Expected: `False`

- [ ] **Step 3: 提交**

```bash
git add -A
git commit -m "chore: 删除 sync-maintenance.yml，维护逻辑移入 auto-sync"
```
