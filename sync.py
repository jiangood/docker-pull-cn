#!/usr/bin/env python3
import logging
import os
import sys

from config import get_config
from docker_service import DockerService

logging.basicConfig(level=logging.INFO, format="%(levelname)s %(message)s")
logger = logging.getLogger(__name__)


def write_github_output(name, value):
    path = os.environ.get("GITHUB_OUTPUT")
    if not path:
        logger.warning("GITHUB_OUTPUT 环境变量不存在，跳过输出")
        return
    with open(path, "a", encoding="utf-8") as f:
        f.write(f"{name}={value}\n")


def main():
    if len(sys.argv) < 2:
        logger.error("用法: python sync.py <image>")
        sys.exit(1)

    image = sys.argv[1]
    logger.info("参数 %s", image)

    config = get_config()
    service = DockerService(config)

    try:
        target = service.pull_and_push(image)
        msg = f"✅ 任务已完成！ 镜像地址： {target}"
        write_github_output("msg", msg)
    except Exception as e:
        logger.error("拉取或推送时错误", exc_info=True)
        msg = f"❌ 任务执行异常：{e}"
        write_github_output("msg", msg)
        sys.exit(1)


if __name__ == "__main__":
    main()
