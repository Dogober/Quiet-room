from typing import Annotated

from pydantic import BaseModel
from pydantic import StringConstraints


class CreateMessageRequest(BaseModel):
    text: Annotated[
        str,
        StringConstraints(
            strip_whitespace=True,
            min_length=1,
            max_length=2000,
        ),
    ]
