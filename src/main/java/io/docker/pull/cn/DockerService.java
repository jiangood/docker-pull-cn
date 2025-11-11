package io.docker.pull.cn;

import cn.hutool.crypto.SecureUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.PushResponseItem;
import io.docker.pull.cn.config.SysProp;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Slf4j
@Component
@AllArgsConstructor
public class DockerService {

    DockerClient cli;

    @Resource
    SysProp sysProp;

    public String md5(String image) {
        String s = SecureUtil.md5(image);
        return s;
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
        String repository = sysProp.getRegistry().getUrl() + "/" + sysProp.getTargetRepository();
        String targetImage = repository + ":" + image.replace("/", "_").replace(":", "_");
        return targetImage;
    }


    /**
     * @param image 如 nginx:latest
     */
    public void pull(String image) throws InterruptedException {
        cli.pullImageCmd(image).exec(getPullImageResultCallback()).awaitCompletion();

    }

    private static PullImageResultCallback getPullImageResultCallback() {
        return new PullImageResultCallback() {
            @Override
            public void onNext(PullResponseItem item) {
                // 打印拉取过程中的状态信息
                if (item.getStatus() != null) {
                    log.info("状态: {}", item.getStatus());
                } else if (item.getProgressDetail() != null) {
                    // 可以添加更详细的进度显示逻辑
                    log.info("进度: {}", item.getProgressDetail().getCurrent() + " / " + item.getProgressDetail().getTotal());
                }
                super.onNext(item);
            }
        };
    }

    public void push(String image) throws InterruptedException {
        cli.pushImageCmd(image).exec(getResultCallback()).awaitCompletion();
    }

    private static ResultCallback.Adapter<PushResponseItem> getResultCallback() {
        return new ResultCallback.Adapter<>() {
            @Override
            public void onNext(PushResponseItem item) {

                if (item.getStatus() != null) {
                    log.info("状态: {}", item.getStatus());
                } else if (item.getProgressDetail() != null) {
                    // 可以添加更详细的进度显示逻辑
                    log.info("进度: {}", item.getProgressDetail().getCurrent() + " / " + item.getProgressDetail().getTotal());
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
        String imageId = getImageId(image);
        Assert.notNull(imageId, "获取imageId失败");

        String[] arr = target.split(":");

        cli.tagImageCmd(imageId, arr[0], arr[1]).exec();
    }

}
