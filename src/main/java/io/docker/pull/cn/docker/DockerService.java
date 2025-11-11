package io.docker.pull.cn.docker;

import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.ResponseItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import io.docker.pull.cn.config.SysProp;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Slf4j
@Component
public class DockerService {

    private DockerClient cli;

    private DockerClient cliRemote;

    @Resource
    private SysProp sysProp;

    @PostConstruct
    public void init() {
        this.cli = this.getCli(false);
        this.cliRemote = this.getCli(true);
    }

    private DockerClient getCli(boolean useRemote) {
        SysProp.Registry registry = sysProp.getRegistry();


        boolean windows = SystemUtil.getOsInfo().isWindows();
        String dockerHost = windows ? "tcp://localhost:2375" : "unix:///var/run/docker.sock";
        log.info("docker主机路径：{}", dockerHost);

        DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost);

        if (useRemote) {
            if (StrUtil.isAllNotEmpty(registry.getUser(), registry.getPwd())) {
                builder.withRegistryUrl(registry.getUrl())
                        .withRegistryUsername(registry.getUser())
                        .withRegistryPassword(registry.getPwd());
            }
        }


        DockerClientConfig config = builder.build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();


        return DockerClientImpl.getInstance(config, httpClient);
    }


    public void pullRemoteAndChangeTag(String image) throws InterruptedException {
        String targetImage = getChangedTargetImageName(image);
        log.info("转换镜像地址 {}", targetImage);

        this.pull(targetImage);

        this.changeTag(targetImage, image);
    }

    public String pullAndPush(String image) throws InterruptedException {
        this.pull(image);
        log.info("拉取镜像完成 ：{}", image);

        String targetImage = getChangedTargetImageName(image);
        this.changeTag(image, targetImage);
        log.info("修改为目标镜像: {}", targetImage);

        this.push(targetImage);
        log.info("推送完成");
        return targetImage;
    }

    private String getChangedTargetImageName(String image) {
        String repository = sysProp.getTargetRepository();
        String targetImage = repository + ":" + image.replace("/", "_").replace(":", "_");
        return targetImage;
    }


    /**
     * @param image 如 nginx:latest
     */
    public void pull(String image) throws InterruptedException {
        log.info("开始拉取镜像: {}", image);
        cli.pullImageCmd(image).exec(getCallback()).awaitCompletion();

    }


    public void push(String image) throws InterruptedException {
        log.info("开始推送镜像:{}", image);
        cliRemote.pushImageCmd(image).exec(getCallback()).awaitCompletion();
    }

    private static <T extends ResponseItem> ResultCallback.Adapter<T> getCallback() {
        return new ResultCallback.Adapter<>() {
            @Override
            public void onNext(T item) {
                String status = item.getStatus();
                String progress = item.getProgress();

                if (progress != null) {
                    log.info("进度 {}", progress);
                } else if (status != null) {
                    log.info("状态 {}", status);
                } else if (item.getErrorDetail() != null && item.getErrorDetail().getMessage() != null) {
                    log.info("错误 {}", item.getErrorDetail().getMessage());
                }
                super.onNext(item);
            }
        };
    }

    public String getImageId(String image) {
        // 使用 inspectImageCmd 命令，传入完整的 REPO:TAG
        InspectImageResponse inspectResponse = cli.inspectImageCmd(image).exec();

        // Image ID 存储在 ID 字段中
        String imageId = inspectResponse.getId();
        return imageId;
    }

    public void changeTag(String image, String target) {
        log.info("准备修改镜像TAG");
        log.info("原始镜像：{}", image);
        log.info("改后镜像：{}", target);
        String imageId = getImageId(image);
        Assert.notNull(imageId, "获取imageId失败");

        String[] arr = target.split(":");

        cli.tagImageCmd(imageId, arr[0], arr[1]).exec();
        log.info("修改结束");
    }

}
