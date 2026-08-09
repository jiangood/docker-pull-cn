# 设计：auto-sync 增加 README 同步，移除 sync-maintenance

日期：2026-08-09

## 背景

当前 `sync.py` 同步镜像到阿里云和 ghcr 两个 registry，成功后通过 issue 评论回复地址。`sync-maintenance.yml` 每周运行，验证 README 表格条目、清理失效项并关闭成功 issue。

目标：把 README 维护功能移入 `auto-sync-job.yml`（每次同步触发），并删除 `sync-maintenance.yml`。

## README 格式

使用嵌套列表而非表格，便于阅读：

```
## 已同步镜像

- python:3.12
  - 阿里云: `registry.cn-hangzhou.aliyuncs.com/jiangood/images:python_3.12`
  - ghcr: `ghcr.io/jiangood/images:python_3.12`
```

- 镜像下有哪个 registry 地址就显示哪一行，缺失的不显示
- 无 `## 已同步镜像` 小节时自动重建
- README 是 image → 地址映射的唯一来源（tag 无法可靠反推原始镜像名）

## sync.py 新增 `--update-readme` 子命令

`python sync.py --update-readme <image>`，纯标准库（urllib），无新依赖。

流程：
1. 读取 README.md 的 `## 已同步镜像` 小节现有条目
2. 拉取两个 registry 的 tag 集合：
   - 阿里云：`GET /v2/jiangood/images/tags/list`（Basic Auth，REGISTRY_USER/REGISTRY_PWD）
   - ghcr：`GET /v2/<owner>/<repo>/tags/list`（`Bearer GITHUB_TOKEN`，ghcr 未配置则跳过）
3. 逐条目验证：
   - 阿里云 tag 在集合中 → 保留地址；否则该地址移除
   - ghcr tag 在集合中 → 保留地址；否则该地址移除
   - 两个地址都无 → 删除整条
   - 网络异常 → 保留（避免瞬时故障误删）
4. 加入新条目（image + 阿里云地址 + ghcr 地址，ghcr 未配置则只加阿里云），按 image 去重
5. 重写 README 小节

注意：验证规则「任一个 registry 有该 tag 就保留整条，缺失的地址留空」是既定的旧条目处理策略。

## auto-sync-job.yml 修改

完整流程：
1. 回复「任务已收到」
2. checkout
3. `python sync.py "<title>"`
4. `python sync.py --update-readme "<title>"`
5. 有变更则 git commit + push（需新增 `contents: write` 权限）
6. 回复 msg
7. `gh issue close` 关闭当前 issue

## 删除 sync-maintenance.yml

## 测试

- 本地构造带列表/无小节/多 registry 的 README 副本，运行 `--update-readme` 验证重写结果
- 验证 tag 集合拉取（mock 或真实 registry）
