package io.docker.pull.cn.command;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import io.docker.pull.cn.docker.DockerService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class ServerCommand implements CommandLineRunner {

    @Resource
    DockerService dockerService;

    @Override
    public void run(String... args) throws Exception {
        if(args.length != 2){
            return;
        }
        String image = args[0];
        log.info("参数 {}", image);

        try {
            String targetImage = dockerService.pullAndPush(image);
            String msg = "✅ 任务已完成！ 镜像地址： " + targetImage ;
            writeGithubActionOutputVariable("msg", msg);
        }catch (Exception e){
            log.error("拉取或推送时错误",e);
            String msg = "❌ 任务执行错误，请删除issue后重试"  ;
            writeGithubActionOutputVariable("msg", msg);
        }
    }

    // 核心方法：将变量写入 $GITHUB_OUTPUT 文件
    private static void writeGithubActionOutputVariable(String name, String value) {
        // 1. 获取 $GITHUB_OUTPUT 环境变量的值（即文件路径）
        String githubOutputFilePath = System.getenv("GITHUB_OUTPUT");

        if (StrUtil.isBlank(githubOutputFilePath)) {
            log.warn("GITHUB_OUTPUT 环境变量不存在. Output ignored.");
            return;
        }

        // 2. 准备写入的内容，格式为：key=value\n
        // Hutool 的 StrUtil.format() 可以用来方便地格式化字符串
        String contentToWrite = StrUtil.format("{}={}{}", name, value, System.lineSeparator());

        try {
            // 3. 使用 Hutool 的 FileUtil.appendString() 简化操作
            // 参数1: 文件路径 (会自动创建文件如果不存在)
            // 参数2: 写入的内容
            // 参数3: 字符集 (这里使用默认的UTF-8)
            FileUtil.appendString(contentToWrite, githubOutputFilePath, StandardCharsets.UTF_8);

            System.out.println("✅ Successfully wrote output variable using Hutool: " + name + "=" + value);
        } catch (Exception e) {
            // FileUtil.appendString 内部处理了 IOException，这里可以捕获更一般的 Exception
            System.err.println("❌ Error writing to GITHUB_OUTPUT file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
