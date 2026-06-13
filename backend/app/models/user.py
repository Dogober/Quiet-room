from sqlalchemy import BigInteger
from sqlalchemy import Integer
from sqlalchemy import String
from sqlalchemy.orm import Mapped
from sqlalchemy.orm import mapped_column
from sqlalchemy.orm import relationship

from app.database import Base
from app.models.message import MessageModel

class UserModel(Base):

    __tablename__ = "users"

    id: Mapped[int] = mapped_column(
        BigInteger().with_variant(
            Integer,
            "sqlite"
        ),
        primary_key=True,
        autoincrement=True,
    )

    username: Mapped[str] = mapped_column(
        String(32),
        nullable=False,
        unique=True,
        index=True,
    )

    password_hash: Mapped[str] = mapped_column(
        String(255),
        nullable=False
    )

    messages: Mapped[list["MessageModel"]] = relationship(
        back_populates="user"
    )
