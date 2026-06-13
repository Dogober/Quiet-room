from typing import Annotated

from pydantic import BaseModel
from pydantic import StringConstraints
from pydantic import field_validator


Username = Annotated[
    str,
    StringConstraints(
        strip_whitespace=True,
        min_length=3,
        max_length=32,
        pattern=r"^[A-Za-z0-9_]+$",
    ),
]


class RegisterRequest(BaseModel):
    username: Username
    password: Annotated[
        str,
        StringConstraints(min_length=8, max_length=72),
    ]

    @field_validator("password")
    @classmethod
    def password_must_fit_bcrypt(
        cls,
        password: str,
    ) -> str:
        if len(password.encode("utf-8")) > 72:
            raise ValueError(
                "Password is too long"
            )
        return password


class LoginRequest(BaseModel):
    username: Username
    password: Annotated[
        str,
        StringConstraints(min_length=1, max_length=72),
    ]


class LoginResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"


class RegisterResponse(BaseModel):
    message: str
