from tests.test_auth import login_user
from tests.test_auth import register_user


def authenticated_headers(client) -> dict[str, str]:
    assert register_user(client).status_code == 201
    token = login_user(client).json()["access_token"]
    return {"Authorization": f"Bearer {token}"}


def test_message_history_requires_authentication(client):
    response = client.get("/messages")
    assert response.status_code == 401


def test_create_and_list_messages(client):
    headers = authenticated_headers(client)

    create_response = client.post(
        "/messages",
        headers=headers,
        json={"text": "  Hello QuietRoom  "},
    )
    assert create_response.status_code == 200
    assert create_response.json()["text"] == "Hello QuietRoom"

    list_response = client.get(
        "/messages",
        headers=headers,
    )
    assert list_response.status_code == 200
    assert len(list_response.json()) == 1
    assert (
        list_response.json()[0]["sender"]["username"]
        == "alice"
    )


def test_websocket_uses_authenticated_server_username(client):
    headers = authenticated_headers(client)

    with client.websocket_connect(
        "/ws",
        headers=headers,
    ) as websocket:
        online_event = websocket.receive_json()
        assert online_event == {
            "type": "online_count",
            "count": 1,
        }

        websocket.send_json({"type": "typing"})
        typing_event = websocket.receive_json()
        assert typing_event == {
            "type": "typing",
            "username": "alice",
        }
