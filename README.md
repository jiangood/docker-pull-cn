# docker-pull-cn
使用Github Action将国外的Docker镜像转存到阿里云私有仓库，供国内服务器使用，免费易用
- 支持DockerHub, gcr.io, k8s.io, ghcr.io等任意仓库
- 支持最大40GB的大型镜像
- 使用阿里云的官方线路，速度快
  
# 原理
项目使用 Issues 触发 GitHub Action，执行镜像同步任务

<img width="1466" height="1070" alt="image" src="https://github.com/user-attachments/assets/b633b84d-0cd1-43e1-a4ea-698dc15134d2" />

# 快速使用 （创建issue触发后使用）

## 步骤1 
  创建issue，标题就写就像名称，如python:3 （必须包含冒号。）
## 步骤2 
等待任务执行，执行成功会自动回复到issue
  
得到镜像地址：registry.cn-hangzhou.aliyuncs.com/jiangood/images:python__3



---

## 已同步镜像

- alpine:3.20
  - 阿里云: `registry.cn-hangzhou.aliyuncs.com/jiangood/images:alpine__3.20`
  - ghcr: `ghcr.io/jiangood/docker-pull-cn:alpine__3.20`
- busybox:1.36
  - 阿里云: `registry.cn-hangzhou.aliyuncs.com/jiangood/images:busybox__1.36`
  - ghcr: `ghcr.io/jiangood/docker-pull-cn:busybox__1.36`
- gitlab/gitlab-runner:latest
  - 阿里云: `registry.cn-hangzhou.aliyuncs.com/jiangood/images:gitlab___gitlab-runner__latest`
  - ghcr: `ghcr.io/jiangood/docker-pull-cn:gitlab___gitlab-runner__latest`
- maven:3.9.9
  - 阿里云: `registry.cn-hangzhou.aliyuncs.com/jiangood/images:maven__3.9.9`
  - ghcr: `ghcr.io/jiangood/docker-pull-cn:maven__3.9.9`
- mysql:8.0
  - 阿里云: `registry.cn-hangzhou.aliyuncs.com/jiangood/images:mysql__8.0`
  - ghcr: `ghcr.io/jiangood/docker-pull-cn:mysql__8.0`
- nginx:1.27
  - 阿里云: `registry.cn-hangzhou.aliyuncs.com/jiangood/images:nginx__1.27`
  - ghcr: `ghcr.io/jiangood/docker-pull-cn:nginx__1.27`
- nginx:latest
  - 阿里云: `registry.cn-hangzhou.aliyuncs.com/jiangood/images:nginx__latest`
  - ghcr: `ghcr.io/jiangood/docker-pull-cn:nginx__latest`
- ollama/ollama:latest
  - 阿里云: `registry.cn-hangzhou.aliyuncs.com/jiangood/images:ollama___ollama__latest`
  - ghcr: `ghcr.io/jiangood/docker-pull-cn:ollama___ollama__latest`
- python:3
  - 阿里云: `registry.cn-hangzhou.aliyuncs.com/jiangood/images:python__3`
  - ghcr: `ghcr.io/jiangood/docker-pull-cn:python__3`
- quay.io/coreos/etcd:v3.5.0
  - 阿里云: `registry.cn-hangzhou.aliyuncs.com/jiangood/images:quay.io___coreos___etcd__v3.5.0`
  - ghcr: `ghcr.io/jiangood/docker-pull-cn:quay.io___coreos___etcd__v3.5.0`
- redis:latest
  - 阿里云: `registry.cn-hangzhou.aliyuncs.com/jiangood/images:redis__latest`
  - ghcr: `ghcr.io/jiangood/docker-pull-cn:redis__latest`
