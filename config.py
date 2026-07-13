import os


def get_config():
    return {
        "registry_url": os.getenv("REGISTRY_URL", "registry.cn-hangzhou.aliyuncs.com"),
        "registry_user": os.getenv("REGISTRY_USER", "hustme"),
        "registry_pwd": os.getenv("sys.registry.pwd", os.getenv("REGISTRY_PWD", "")),
        "target_repository": os.getenv(
            "TARGET_REPOSITORY",
            "registry.cn-hangzhou.aliyuncs.com/jiangood/images",
        ),
        "ghcr_repository": os.getenv("GHCR_REPO", ""),
        "ghcr_token": os.getenv("GITHUB_TOKEN", ""),
    }
