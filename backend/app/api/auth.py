from fastapi import APIRouter
from fastapi import Depends
from fastapi import HTTPException
from fastapi import status
from fastapi.security import HTTPAuthorizationCredentials
from fastapi.security import HTTPBearer
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.user import UserModel
from app.schemas.auth import LoginRequest
from app.schemas.auth import LoginResponse
from app.schemas.auth import RegisterRequest
from app.schemas.auth import RegisterResponse
from app.schemas.user import User
from app.security import InvalidTokenError
from app.security import create_access_token
from app.security import decode_access_token
from app.security import hash_password
from app.security import verify_password


security = HTTPBearer(auto_error=False)

router = APIRouter(
    prefix="/auth",
    tags=["Auth"]
)


def unauthorized_exception() -> HTTPException:
    return HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Invalid or expired access token",
        headers={"WWW-Authenticate": "Bearer"},
    )


def get_current_user(
    credentials: HTTPAuthorizationCredentials | None = Depends(security),
    db: Session = Depends(get_db),
) -> UserModel:
    if credentials is None:
        raise unauthorized_exception()

    try:
        user_id = decode_access_token(credentials.credentials)
    except InvalidTokenError as error:
        raise unauthorized_exception() from error

    user = db.get(UserModel, user_id)
    if user is None:
        raise unauthorized_exception()

    return user


@router.get("/me", response_model=User)
def me(
    current_user: UserModel = Depends(get_current_user)
) -> User:
    return User(
        id=current_user.id,
        username=current_user.username,
    )


@router.post(
    "/register",
    response_model=RegisterResponse,
    status_code=status.HTTP_201_CREATED,
)
def register(
    request: RegisterRequest,
    db: Session = Depends(get_db),
) -> RegisterResponse:
    existing_user = db.query(UserModel).filter(
        UserModel.username == request.username
    ).first()

    if existing_user is not None:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Username already exists",
        )

    user = UserModel(
        username=request.username,
        password_hash=hash_password(request.password),
    )
    db.add(user)

    try:
        db.commit()
    except IntegrityError as error:
        db.rollback()
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Username already exists",
        ) from error

    return RegisterResponse(message="User created")


@router.post("/login", response_model=LoginResponse)
def login(
    request: LoginRequest,
    db: Session = Depends(get_db),
) -> LoginResponse:
    user = db.query(UserModel).filter(
        UserModel.username == request.username
    ).first()

    if user is None or not verify_password(
        request.password,
        user.password_hash,
    ):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid username or password",
            headers={"WWW-Authenticate": "Bearer"},
        )

    return LoginResponse(
        access_token=create_access_token(user.id),
    )
