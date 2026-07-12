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
  
得到镜像地址：registry.cn-hangzhou.aliyuncs.com/jiangood/images:python_3



---

## 已同步镜像

| 镜像 | 阿里云地址 |
|------|-----------|
| python:3.12 | `registry.cn-hangzhou.aliyuncs.com/jiangood/images:python_3.12` |
| ollama/ollama:latest | `registry.cn-hangzhou.aliyuncs.com/jiangood/images:ollama_ollama_latest` |
| dustynv/llama_cpp:r35.4.1 | `registry.cn-hangzhou.aliyuncs.com/jiangood/images:dustynv_llama_cpp_r35.4.1` |
| 1186258278/openclaw-zh:latest | `registry.cn-hangzhou.aliyuncs.com/jiangood/images:1186258278_openclaw-zh_latest` |
| node:24 | `registry.cn-hangzhou.aliyuncs.com/jiangood/images:node_24` |
| node:22 | `registry.cn-hangzhou.aliyuncs.com/jiangood/images:node_22` |
| redis:latest | `registry.cn-hangzhou.aliyuncs.com/jiangood/images:redis_latest` |
| maven:3-openjdk-17 | `registry.cn-hangzhou.aliyuncs.com/jiangood/images:maven_3-openjdk-17` |
| python:3 | `registry.cn-hangzhou.aliyuncs.com/jiangood/images:python_3` |
| elasticsearch:7.17.19 | `registry.cn-hangzhou.aliyuncs.com/jian41/images:elasticsearch_7.17.19` |
| node:16-alpine | `registry.cn-hangzhou.aliyuncs.com/jian41/images:node_16-alpine` |
| maven:alpine | `registry.cn-hangzhou.aliyuncs.com/jian41/images:maven_alpine` |
| maven:3-openjdk-8 | `registry.cn-hangzhou.aliyuncs.com/jian41/images:maven_3-openjdk-8` |
| python:3.11 | `registry.cn-hangzhou.aliyuncs.com/jian41/images:python_3.11` |
| openjdk:17.0.2-oracle | `registry.cn-hangzhou.aliyuncs.com/jian41/images:openjdk_17.0.2-oracle` |
| maven:3-amazoncorretto-17 | `registry.cn-hangzhou.aliyuncs.com/jian41/images:maven_3-amazoncorretto-17` |
| nginx:alpine | `registry.cn-hangzhou.aliyuncs.com/jian41/images:nginx_alpine` |
| mysql:8.0-oraclelinux8 | `registry.cn-hangzhou.aliyuncs.com/jiangood/images:mysql_8.0-oraclelinux8` |
| mysql:8.0.46 | `registry.cn-hangzhou.aliyuncs.com/jiangood/images:mysql_8.0.46` |
