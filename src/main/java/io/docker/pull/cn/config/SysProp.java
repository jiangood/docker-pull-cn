package io.docker.pull.cn.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("sys")
public class SysProp {

    Registry registry;

    String targetRepository;

    @Data
    public static class Registry{
        String url;
        String user;
        String pwd;
    }
}
