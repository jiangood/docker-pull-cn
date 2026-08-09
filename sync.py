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
        if "\n" in value:
            f.write(f"{name}<<EOF\n{value}\nEOF\n")
        else:
            f.write(f"{name}={value}\n")


import base64
import json
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError

from docker_service import reverse_tag


def _basic_auth(user, pwd):
    token = base64.b64encode(f"{user}:{pwd}".encode()).decode()
    return f"Basic {token}"


def fetch_tags(registry_url, repository, user, pwd, token=None):
    url = f"https://{registry_url}/v2/{repository}/tags/list"
    headers = {"Accept": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    else:
        headers["Authorization"] = _basic_auth(user, pwd)
    req = Request(url, headers=headers)
    with urlopen(req, timeout=15) as resp:
        data = json.load(resp)
    return data.get("tags") or []


def collect_images():
    config = get_config()
    result = {}

    target_repo = config["target_repository"]
    if "/" in target_repo:
        aliyun_repo = target_repo.split("/", 1)[1]
    else:
        aliyun_repo = None
    try:
        if aliyun_repo:
            tags = fetch_tags(
                config["registry_url"],
                aliyun_repo,
                config["registry_user"],
                config["registry_pwd"],
            )
            for tag in tags:
                image = reverse_tag(tag)
                if image:
                    result.setdefault(image, {})["阿里云"] = f"{config['target_repository']}:{tag}"
    except (URLError, OSError, ValueError) as e:
        logger.error("阿里云 tags/list 失败: %s", e)

    ghcr_repo = config["ghcr_repository"]
    ghcr_token = config["ghcr_token"]
    if ghcr_repo and ghcr_token:
        try:
            tags = fetch_tags("ghcr.io", ghcr_repo, "", "", ghcr_token)
            for tag in tags:
                image = reverse_tag(tag)
                if image:
                    result.setdefault(image, {})["ghcr"] = f"ghcr.io/{ghcr_repo}:{tag}"
        except (URLError, OSError, ValueError) as e:
            logger.error("ghcr tags/list 失败: %s", e)

    return result


def generate_readme(images):
    lines = ["", "## 已同步镜像", ""]
    for image in sorted(images):
        lines.append(f"- {image}")
        for name, addr in images[image].items():
            lines.append(f"  - {name}: `{addr}`")
    return "\n".join(lines) + "\n"


def update_readme():
    images = collect_images()
    section = generate_readme(images)

    readme_path = "README.md"
    with open(readme_path, encoding="utf-8") as f:
        content = f.read()

    marker = "## 已同步镜像"
    if marker in content:
        idx = content.index(marker)
        head = content[:idx].rstrip() + "\n"
    else:
        head = content.rstrip() + "\n"

    with open(readme_path, "w", encoding="utf-8") as f:
        f.write(head + section)
    return len(images)


def main():
    if len(sys.argv) > 1 and sys.argv[1] == "--update-readme":
        count = update_readme()
        write_github_output("msg", f"📋 README 已更新，共 {count} 个镜像")
        return

    if len(sys.argv) < 2:
        logger.error("用法: python sync.py <image>")
        sys.exit(1)

    image = sys.argv[1]
    logger.info("参数 %s", image)

    config = get_config()
    service = DockerService(config)

    try:
        ali_target = service.pull_and_push(image)

        ghcr_target = service.push_to_ghcr(image)

        msg = f"✅ 阿里云：{ali_target}"
        if ghcr_target:
            msg += f"\n✅ ghcr：{ghcr_target}"
        write_github_output("msg", msg)
    except Exception as e:
        logger.error("拉取或推送时错误", exc_info=True)
        msg = f"❌ 任务执行异常：{e}"
        write_github_output("msg", msg)
        sys.exit(1)


if __name__ == "__main__":
    main()
