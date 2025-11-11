package io.docker.pull.cn.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Slf4j
@Configuration
public class Config {

    @Bean
    public DockerClient dockerClient() {
        boolean windows = SystemUtil.getOsInfo().isWindows();
        String dockerHost = windows ? "tcp://localhost:2375" : "unix:///var/run/docker.sock";
        log.info("docker主机路径：{}", dockerHost);

        DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost);

        DockerClientConfig config = builder.build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();


        return DockerClientImpl.getInstance(config, httpClient);
    }

    @Bean
    public DockerClient dockerClientRemote(SysProp sysProp) {
        SysProp.Registry registry = sysProp.getRegistry();
        if (StrUtil.isEmpty(registry.getUser())) {
            log.trace("注册中心用户为空");
            return null;
        }
        if (StrUtil.isEmpty(registry.getPwd())) {
            log.trace("注册中心密码为空");
            return null;
        }


        boolean windows = SystemUtil.getOsInfo().isWindows();
        String dockerHost = windows ? "tcp://localhost:2375" : "unix:///var/run/docker.sock";
        log.info("docker主机路径：{}", dockerHost);

        DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost);
        builder.withRegistryUrl(registry.getUrl())
                .withRegistryUsername(registry.getUser())
                .withRegistryPassword(registry.getPwd());


        DockerClientConfig config = builder.build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();


        return DockerClientImpl.getInstance(config, httpClient);
    }
}
