# docker-pull-cn
使用 Github Action 将国外的 Docker 镜像转存到阿里云私有仓库，供国内服务器使用，免费易用
- 支持 DockerHub, gcr.io, k8s.io, ghcr.io 等任意仓库
- 支持最大 40GB 的大型镜像
- 使用阿里云的官方线路，速度快

# 原理
仓库中有一条置顶的「镜像同步请求」Issue，在该 Issue 下评论 `/pull 镜像名` 即可触发 GitHub Action 执行镜像同步，结果会以评论形式回复在该 Issue 中。

# 快速使用

## 步骤 1
打开置顶的「镜像同步请求」Issue，发表评论：

```
/pull python:3
```

镜像名必须包含冒号后的 tag，例如 `python:3`、`nginx:1.27`、`nvidia/cuda:12.4.1-base-ubuntu22.04`。

## 步骤 2
等待任务执行，执行成功会自动在 Issue 中回复镜像地址。

得到镜像地址：`registry.cn-hangzhou.aliyuncs.com/jiangood/images:python__3`

## 步骤 3 拉取使用

```bash
docker pull registry.cn-hangzhou.aliyuncs.com/jiangood/images:python__3
docker tag registry.cn-hangzhou.aliyuncs.com/jiangood/images:python__3 python:3
```

# 说明

- 只有在置顶的「镜像同步请求」Issue（带 `image-sync` 标签）下的 `/pull` 评论才会触发同步
- 命令必须以 `/pull ` 开头，且镜像名包含 `:`
- 同步历史集中在这一条 Issue 中，不再为每个镜像新建 Issue
- 同步失败时同样会回复失败原因，可查看 Actions 日志定位问题
