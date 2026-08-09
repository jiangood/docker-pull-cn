# 设计：auto-sync 增加 README 同步，移除 sync-maintenance

日期：2026-08-09

## 背景

当前 `sync.py` 同步镜像到阿里云和 ghcr 两个 registry，成功后通过 issue 评论回复地址。`sync-maintenance.yml` 每周运行，验证 README 表格条目、清理失效项并关闭成功 issue。

目标：把 README 维护功能移入 `auto-sync-job.yml`（每次同步触发），并删除 `sync-maintenance.yml`。

## Tag 格式变更

`_format_tag` 从单下划线改为双/三下划线分隔，使 tag 可无歧义反推原始镜像名：

- `/` → `___`（三个下划线）
- `:` → `__`（两个下划线）
- 单下划线保留在镜像名中

例：`python:3.12` → `python__3.12`；`1186258278/openclaw-zh:latest` → `1186258278___openclaw-zh__latest`

反推规则：最后一个 `__` 是 `:`，其余 `___` 是 `/`。

## README 格式

嵌套列表，由 registry 的 tags/list 完全生成，README 不再存映射：

```
## 已同步镜像

- python:3.12
  - 阿里云: `registry.cn-hangzhou.aliyuncs.com/jiangood/images:python__3.12`
  - ghcr: `ghcr.io/jiangood/images:python__3.12`
```

- 地址由 tag 直接推导，无需存储
- 镜像在哪个 registry 有该 tag 就显示哪一行，缺失的不显示
- 无 `## 已同步镜像` 小节时自动重建

## sync.py 新增 `--update-readme` 子命令

`python sync.py --update-readme`（无需 image 参数，因为 README 完全由 registry 生成），纯标准库（urllib），无新依赖。

流程：
1. 拉取两个 registry 的 tag 集合：
   - 阿里云：`GET /v2/jiangood/images/tags/list`（Basic Auth，REGISTRY_USER/REGISTRY_PWD）
   - ghcr：`GET /v2/<owner>/<repo>/tags/list`（`Bearer GITHUB_TOKEN`，ghcr 未配置则跳过）
2. 反推每个 tag 的镜像名，按镜像名归并（同名 tag 在哪个 registry 存在就保留哪个地址）
3. 生成嵌套列表并重写 README 小节

## auto-sync-job.yml 修改

完整流程：
1. 回复「任务已收到」
2. checkout
3. `python sync.py "<title>"`
4. `python sync.py --update-readme`
5. 有变更则 git commit + push（需新增 `contents: write` 权限）
6. 回复 msg
7. `gh issue close` 关闭当前 issue

## 一次性迁移：migrate-legacy.yml（临时 workflow）

跑完后手动删除。用途：清理旧单下划线格式的数据，使新格式 README 纯净。

1. 关闭所有 open issues：`gh issue close`
2. 清理 README：删除旧 `## 已同步镜像` 小节
3. 删除阿里云旧 tag：`tags/list` 筛出单下划线格式 tag → `GET manifests/<tag>` 拿 digest → `DELETE manifests/<digest>`（Basic Auth）
4. 删除 ghcr 旧 tag：同理（`Bearer GITHUB_TOKEN`）

## 删除 sync-maintenance.yml

## 测试

- 本地构造带列表/无小节/多 registry 的 README 副本，运行 `--update-readme` 验证重写结果
- 验证 tag 集合拉取与反推（mock 或真实 registry）
