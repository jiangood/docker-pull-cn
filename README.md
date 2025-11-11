# docker-pull-cn
使用Github Action将国外的Docker镜像转存到阿里云私有仓库，供国内服务器使用，免费易用
- 支持DockerHub, gcr.io, k8s.io, ghcr.io等任意仓库<
- 支持最大40GB的大型镜像
- 使用阿里云的官方线路，速度快
  
项目使用issue触发

<img width="1466" height="1070" alt="image" src="https://github.com/user-attachments/assets/b633b84d-0cd1-43e1-a4ea-698dc15134d2" />

# 快速使用
执行如下命令
```shell
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock registry.cn-hangzhou.aliyuncs.com/jian41/docker-pull  python:3
```
其中python:3 可替换为实际镜像

如果报错提示镜像不存，需要创建issue触发Github Action同步

# 创建issue触发后使用

## 步骤1 
  创建issue，标题就写就像名称，如python:3 （如果该issue已存在，可查看到镜像地址）
## 步骤2 
等待任务执行，执行成功会自动回复到issue
  
得到镜像地址：registry.cn-hangzhou.aliyuncs.com/jian41/images:python_3


