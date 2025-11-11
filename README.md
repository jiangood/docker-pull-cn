# docker-pull-cn
由于网络原因无法拉取镜像，使用该命令行。主要原理是利用 github action 同步到阿里云免费镜像仓库，客户端pull阿里云镜像，最后修改名称


# 使用方式

```shell
docker run --rm registry.cn-hangzhou.aliyuncs.com/jian41/docker-pull-cn  <IMAGE_NAME> 
```



例如想下载 python:3 这个镜像
```shell
docker run --rm registry.cn-hangzhou.aliyuncs.com/jian41/docker-pull-cn  python:3
```
