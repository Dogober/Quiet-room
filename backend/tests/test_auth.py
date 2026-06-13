from jose import jwt

from app.config import settings
from app.security import ALGORITHM


def register_user(
    client,
    username: str = "alice",
    password: str = "password123",
):
    return client.post(
        "/auth/register",
        json={
            "username": username,
            "password": password,
        },
    )


def login_user(
    client,
    username: str = "alice",
    password: str = "password123",
):
    return client.post(
        "/auth/login",
        json={
            "username": username,
            "password": password,
        },
    )


def test_register_login_and_current_user(client):
    assert register_user(client).status_code == 201

    login_response = login_user(client)
    assert login_response.status_code == 200
    token = login_response.json()["access_token"]

    me_response = client.get(
        "/auth/me",
        headers={"Authorization": f"Bearer {token}"},
    )
    assert me_response.status_code == 200
    assert me_response.json()["username"] == "alice"


def test_registration_validation_and_duplicate_username(client):
    invalid_response = register_user(
        client,
        username="x",
        password="short",
    )
    assert invalid_response.status_code == 422

    assert register_user(client).status_code == 201
    assert register_user(client).status_code == 409


def test_invalid_and_expired_tokens_are_rejected(client):
    invalid_response = client.get(
        "/auth/me",
        headers={"Authorization": "Bearer invalid"},
    )
    assert invalid_response.status_code == 401

    expired_token = jwt.encode(
        {
            "sub": "1",
            "type": "access",
            "exp": 1,
        },
        settings.secret_key,
        algorithm=ALGORITHM,
    )
    expired_response = client.get(
        "/auth/me",
        headers={
            "Authorization": f"Bearer {expired_token}"
        },
    )
    assert expired_response.status_code == 401

