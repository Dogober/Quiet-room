import os

import pytest
from fastapi.testclient import TestClient


os.environ["DATABASE_URL"] = "sqlite://"
os.environ["SECRET_KEY"] = "test-secret-key-with-sufficient-length"
os.environ["ACCESS_TOKEN_EXPIRE_MINUTES"] = "60"
os.environ["AUTO_CREATE_TABLES"] = "false"

from app.database import Base
from app.database import engine
from app.main import app


Base.metadata.create_all(bind=engine)


@pytest.fixture(autouse=True)
def clean_database():
    with engine.begin() as connection:
        for table in reversed(Base.metadata.sorted_tables):
            connection.execute(table.delete())


@pytest.fixture
def client():
    with TestClient(app) as test_client:
        yield test_client
