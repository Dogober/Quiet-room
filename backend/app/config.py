import os
from dataclasses import dataclass

from dotenv import load_dotenv


load_dotenv()


def _required_environment_value(name: str) -> str:
    value = os.getenv(name)
    if not value:
        raise RuntimeError(f"{name} must be configured")
    return value


def _secret_key() -> str:
    value = _required_environment_value("SECRET_KEY")
    if len(value) < 32:
        raise RuntimeError(
            "SECRET_KEY must contain at least 32 characters"
        )
    return value


def _environment_flag(name: str, default: bool) -> bool:
    value = os.getenv(name)
    if value is None:
        return default
    return value.lower() in {"1", "true", "yes", "on"}


@dataclass(frozen=True)
class Settings:
    database_url: str
    secret_key: str
    access_token_expire_minutes: int
    auto_create_tables: bool


settings = Settings(
    database_url=_required_environment_value("DATABASE_URL"),
    secret_key=_secret_key(),
    access_token_expire_minutes=int(
        os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", "1440")
    ),
    auto_create_tables=_environment_flag("AUTO_CREATE_TABLES", True),
)
