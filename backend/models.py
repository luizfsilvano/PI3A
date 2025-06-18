from pydantic import BaseModel

class Route(BaseModel):
    id: int
    start_lat: float
    start_lng: float
    end_lat: float
    end_lng: float
    user_id: str