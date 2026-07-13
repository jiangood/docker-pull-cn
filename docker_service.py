import logging

import docker
from docker.errors import DockerException

logger = logging.getLogger(__name__)


class DockerService:
    def __init__(self, config):
        self.config = config
        self.client = docker.from_env()

    def _get_target_image(self, image):
        repo = self.config["target_repository"]
        tag = image.replace("/", "_").replace(":", "_")
        return f"{repo}:{tag}"

    def pull_and_push(self, image):
        target = self._get_target_image(image)
        logger.info("目标镜像: %s", target)

        self.pull(image)
        logger.info("拉取完成: %s", image)

        self.tag(image, target)
        logger.info("标记完成: %s", target)

        self.push(target)
        logger.info("推送完成")

        return target

    def pull(self, image):
        logger.info("拉取镜像: %s", image)
        self.client.images.pull(image)

    def tag(self, image, target):
        img = self.client.images.get(image)
        img.tag(target)

    def push(self, target):
        auth_config = {}
        if self.config["registry_user"] and self.config["registry_pwd"]:
            auth_config = {
                "username": self.config["registry_user"],
                "password": self.config["registry_pwd"],
                "serveraddress": self.config["registry_url"],
            }
        for line in self.client.images.push(
            target, auth_config=auth_config, stream=True, decode=True
        ):
            if "error" in line and line.get("error"):
                raise DockerException(line["error"])
            status = line.get("status")
            progress = line.get("progress")
            if progress:
                logger.info("进度 %s", progress)
            elif status:
                logger.info("状态 %s", status)
