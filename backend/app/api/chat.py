import asyncio
import logging

from fastapi import APIRouter
from fastapi import Depends
from fastapi import Query
from fastapi import WebSocket
from fastapi import WebSocketDisconnect
from pydantic import ValidationError
from sqlalchemy.orm import Session
from sqlalchemy.orm import joinedload
from starlette.concurrency import run_in_threadpool

from app.api.auth import get_current_user
from app.database import SessionLocal
from app.database import get_db
from app.models.message import MessageModel
from app.models.user import UserModel
from app.schemas.create_message import CreateMessageRequest
from app.schemas.message import Message
from app.schemas.user import User
from app.schemas.websocket import ClientTypingEvent
from app.security import InvalidTokenError
from app.security import decode_access_token


logger = logging.getLogger(__name__)
router = APIRouter(tags=["Chat"])


def to_message(message: MessageModel) -> Message:
    return Message(
        id=message.id,
        sender=User(
            id=message.user.id,
            username=message.user.username,
        ),
        text=message.text,
        created_at=message.created_at,
    )


class ConnectionManager:
    def __init__(self) -> None:
        self._active_connections: dict[WebSocket, int] = {}
        self._lock = asyncio.Lock()

    async def connect(
        self,
        websocket: WebSocket,
        user_id: int,
    ) -> None:
        await websocket.accept()
        async with self._lock:
            self._active_connections[websocket] = user_id

    async def disconnect(self, websocket: WebSocket) -> None:
        async with self._lock:
            self._active_connections.pop(websocket, None)

    async def online_count(self) -> int:
        async with self._lock:
            return len(set(self._active_connections.values()))

    async def broadcast_json(self, payload: dict) -> None:
        async with self._lock:
            connections = list(self._active_connections)

        if not connections:
            return

        results = await asyncio.gather(
            *(connection.send_json(payload) for connection in connections),
            return_exceptions=True,
        )

        failed_connections = [
            connection
            for connection, result in zip(connections, results)
            if isinstance(result, Exception)
        ]
        for connection in failed_connections:
            await self.disconnect(connection)

    async def broadcast_online_count(self) -> None:
        await self.broadcast_json({
            "type": "online_count",
            "count": await self.online_count(),
        })


manager = ConnectionManager()


@router.get("/messages", response_model=list[Message])
def get_messages(
    skip: int = Query(default=0, ge=0),
    limit: int = Query(default=100, ge=1, le=200),
    _: UserModel = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> list[Message]:
    db_messages = (
        db.query(MessageModel)
        .options(joinedload(MessageModel.user))
        .order_by(
            MessageModel.created_at.desc(),
            MessageModel.id.desc(),
        )
        .offset(skip)
        .limit(limit)
        .all()
    )
    return [
        to_message(message)
        for message in reversed(db_messages)
    ]


def save_message(user_id: int, text: str) -> Message:
    with SessionLocal() as db:
        db_message = MessageModel(
            user_id=user_id,
            text=text,
        )
        db.add(db_message)
        db.commit()
        db.refresh(db_message)

        message = (
            db.query(MessageModel)
            .options(joinedload(MessageModel.user))
            .filter(MessageModel.id == db_message.id)
            .one()
        )
        return to_message(message)


@router.post("/messages", response_model=Message)
async def create_message(
    request: CreateMessageRequest,
    current_user: UserModel = Depends(get_current_user),
) -> Message:
    message = await run_in_threadpool(
        save_message,
        current_user.id,
        request.text,
    )

    await manager.broadcast_json({
        "type": "message",
        "data": message.model_dump(mode="json"),
    })
    return message


def websocket_access_token(websocket: WebSocket) -> str | None:
    authorization = websocket.headers.get("authorization")
    if authorization is None:
        return None

    scheme, _, token = authorization.partition(" ")
    if scheme.lower() != "bearer" or not token:
        return None
    return token


def websocket_user(token: str) -> tuple[int, str] | None:
    try:
        user_id = decode_access_token(token)
    except InvalidTokenError:
        return None

    with SessionLocal() as db:
        user = db.get(UserModel, user_id)
        if user is None:
            return None
        return user.id, user.username


@router.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket) -> None:
    token = websocket_access_token(websocket)
    user = (
        await run_in_threadpool(websocket_user, token)
        if token is not None
        else None
    )

    if user is None:
        await websocket.close(code=1008)
        return

    user_id, username = user
    await manager.connect(websocket, user_id)
    await manager.broadcast_online_count()

    try:
        while True:
            raw_event = await websocket.receive_text()
            if len(raw_event) > 1024:
                await websocket.close(code=1009)
                break

            try:
                event = ClientTypingEvent.model_validate_json(raw_event)
            except ValidationError:
                await websocket.send_json({
                    "type": "error",
                    "message": "Invalid WebSocket event",
                })
                continue

            if event.type == "typing":
                await manager.broadcast_json({
                    "type": "typing",
                    "username": username,
                })
    except WebSocketDisconnect:
        pass
    except Exception:
        logger.exception("Unexpected WebSocket failure")
    finally:
        await manager.disconnect(websocket)
        await manager.broadcast_online_count()
