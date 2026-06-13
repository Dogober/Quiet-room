from typing import Literal

from pydantic import BaseModel
from pydantic import ConfigDict


class ClientTypingEvent(BaseModel):
    model_config = ConfigDict(extra="forbid")

    type: Literal["typing"]
