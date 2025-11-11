# docker-pull-cn
由于网络原因无法拉取镜像, 利用 github action 同步到阿里云免费镜像仓库

# 使用方式 
假设需要同步镜像 python:3
## 步骤1 
  创建issue，标题就写 python:3
## 步骤2 
  等待任务执行，执行成功后的镜像地址会自动回复到该issue， 得到示例的镜像地址：registry.cn-hangzhou.aliyuncs.com/jian41/docker-pull-cn-target:python_3
## 步骤3 
  拉取镜像并重命名
  ```shell
  docker pull registry.cn-hangzhou.aliyuncs.com/jian41/docker-pull-cn-target:python_3
  docker tag registry.cn-hangzhou.aliyuncs.com/jian41/docker-pull-cn-target:python_3 python:3
  ```

## 步骤3 简化版
执行如下命令
```shell
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock registry.cn-hangzhou.aliyuncs.com/jian41/docker-pull-cn  python:3
```
