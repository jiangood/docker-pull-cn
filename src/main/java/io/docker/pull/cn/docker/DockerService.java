package io.docker.pull.cn.docker;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.ResponseItem;
import io.docker.pull.cn.config.SysProp;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class DockerService {

    @Resource
    DockerClient dockerClientRemote;

    @Resource
    DockerClient dockerClient;

    @Resource
    SysProp sysProp;


    public void pullRemoteAndChangeTag(String image) {
        String targetImage = getChangedTargetImageName(image);
        log.info("转换镜像地址 {}", targetImage);
        this.changeTag(targetImage, image);
    }

    public void pullAndPush(String image) throws InterruptedException {
        this.pull(image);
        log.info("拉取镜像完成 ：{}", image);

        String targetImage = getChangedTargetImageName(image);
        this.changeTag(image, targetImage);
        log.info("修改为目标镜像: {}", targetImage);

        this.push(targetImage);
        log.info("推送完成");
    }

    private String getChangedTargetImageName(String image) {
        String repository =  sysProp.getTargetRepository();
        String targetImage = repository + ":" + image.replace("/", "_").replace(":", "_");
        return targetImage;
    }


    /**
     * @param image 如 nginx:latest
     */
    public void pull(String image) throws InterruptedException {
        log.info("开始拉取镜像: {}", image);
        dockerClient.pullImageCmd(image).exec(getCallback()).awaitCompletion();

    }


    public void push(String image) throws InterruptedException {
        log.info("开始推送镜像:{}",image);
        dockerClientRemote.pushImageCmd(image).exec(getCallback()).awaitCompletion();
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
                }else if(item.getErrorDetail() != null && item.getErrorDetail().getCode() != null) {
                    log.info("错误 {}",item.getErrorDetail().getMessage());
                }
                super.onNext(item);
            }
        };
    }

    public String getImageId(String image) {
        // 使用 inspectImageCmd 命令，传入完整的 REPO:TAG
        InspectImageResponse inspectResponse = dockerClient.inspectImageCmd(image).exec();

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

        dockerClient.tagImageCmd(imageId, arr[0], arr[1]).exec();
        log.info("修改结束");
    }

}
