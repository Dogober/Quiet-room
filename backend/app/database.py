from collections.abc import Generator

from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase
from sqlalchemy.orm import Session
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

from app.config import settings

engine_options: dict = {
    "pool_pre_ping": True,
}
if settings.database_url.startswith("sqlite"):
    engine_options.update({
        "connect_args": {
            "check_same_thread": False
        },
        "poolclass": StaticPool,
    })

engine = create_engine(
    settings.database_url,
    **engine_options,
)

SessionLocal = sessionmaker(
    autoflush=False,
    expire_on_commit=False,
    bind=engine
)


class Base(DeclarativeBase):
    pass


def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
