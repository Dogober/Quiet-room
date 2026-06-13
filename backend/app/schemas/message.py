from pydantic import BaseModel
from app.schemas.user import User

from datetime import datetime

class Message(BaseModel):

    id: int
    sender: User
    text: str
    created_at: datetime
