from datetime import datetime
from datetime import timedelta
from datetime import timezone

from passlib.context import CryptContext
from jose import jwt
from jose import JWTError

from app.config import settings

ALGORITHM = "HS256"


class InvalidTokenError(ValueError):
    pass


def decode_access_token(token: str) -> int:
    try:
        payload = jwt.decode(
            token,
            settings.secret_key,
            algorithms=[ALGORITHM]
        )
        user_id = payload.get("sub")
        token_type = payload.get("type")

        if user_id is None or token_type != "access":
            raise InvalidTokenError("Invalid access token")

        return int(user_id)
    except (JWTError, TypeError, ValueError) as error:
        raise InvalidTokenError("Invalid access token") from error


def create_access_token(user_id: int) -> str:
    now = datetime.now(timezone.utc)
    payload = {
        "sub": str(user_id),
        "type": "access",
        "iat": now,
        "exp": now + timedelta(
            minutes=settings.access_token_expire_minutes
        ),
    }

    return jwt.encode(
        payload,
        settings.secret_key,
        algorithm=ALGORITHM
    )

pwd_context = CryptContext(
    schemes=["bcrypt"],
    deprecated="auto"
)


def hash_password(password: str) -> str:
    return pwd_context.hash(password)


def verify_password(
    plain_password: str,
    hashed_password: str
) -> bool:
    return pwd_context.verify(
        plain_password,
        hashed_password
    )
