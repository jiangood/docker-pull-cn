# docker-pull-cn
由于网络原因无法拉取镜像, 利用 github action 同步到阿里云免费镜像仓库

项目使用issue触发
<img width="1466" height="1070" alt="image" src="https://github.com/user-attachments/assets/b633b84d-0cd1-43e1-a4ea-698dc15134d2" />


# 使用方式 
假设需要使用镜像 python:3
## 步骤1 
  创建issue，标题就写 python:3
## 步骤2 
等待任务执行，执行成功会自动回复到issue
  
得到镜像地址：registry.cn-hangzhou.aliyuncs.com/jian41/images:python_3

## 步骤3 
  拉取镜像并重命名
```shell
docker pull registry.cn-hangzhou.aliyuncs.com/jian41/images:python_3
docker tag registry.cn-hangzhou.aliyuncs.com/jian41/images:python_3 python:3
```
然后就可以在主机上使用python:3 这个镜像了

## 步骤3 简化版
执行如下命令
```shell
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock registry.cn-hangzhou.aliyuncs.com/jian41/docker-pull  python:3
```
