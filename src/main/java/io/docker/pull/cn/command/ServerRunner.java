package io.docker.pull.cn.command;

import io.docker.pull.cn.docker.DockerService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ServerRunner implements CommandLineRunner {

    @Resource
    DockerService dockerService;

    @Override
    public void run(String... args) throws Exception {
        if(args.length != 2){
            return;
        }
        String image = args[0];
        log.info("参数 {}", image);

        dockerService.pullAndPush(image);

    }
}
