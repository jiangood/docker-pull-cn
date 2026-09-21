import logging
import subprocess

logger = logging.getLogger(__name__)


def reverse_tag(tag):
    if "__" not in tag:
        return None
    head, _, tail = tag.rpartition("__")
    if not head:
        return None
    return head.replace("___", "/") + ":" + tail


class DockerError(Exception):
    pass


class DockerService:
    def __init__(self, config):
        self.config = config

    def _format_tag(self, image):
        return image.replace("/", "___").replace(":", "__")

    def _get_target_image(self, image):
        repo = self.config["target_repository"]
        return f"{repo}:{self._format_tag(image)}"

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
        subprocess.run(["docker", "pull", image], check=True)

    def tag(self, image, target):
        subprocess.run(["docker", "tag", image, target], check=True)

    def push(self, target):
        registry_url = self.config["registry_url"]
        user = self.config["registry_user"]
        pwd = self.config["registry_pwd"]
        if user and pwd:
            proc = subprocess.run(
                ["docker", "login", "-u", user, "--password-stdin", f"https://{registry_url}"],
                input=pwd,
                text=True,
                capture_output=True,
            )
            logger.info("login: %s", proc.stdout.strip())
            if proc.returncode != 0:
                raise DockerError(f"登录失败: {proc.stderr.strip()}")
        subprocess.run(["docker", "push", target], check=True)
