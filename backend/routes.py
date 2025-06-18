from fastapi import APIRouter
from models import Route

router = APIRouter()

# Simulando um banco de dados em memória
routes_db = {}

@router.post("/routes/")
def create_route(route: Route):
    route_id = len(routes_db) + 1
    routes_db[route_id] = route
    return {"id": route_id, "message": "Route created successfully"}

@router.get("/routes/{route_id}")
def get_route(route_id: int):
    route = routes_db.get(route_id)
    if route:
        return route
    return {"error": "Route not found"}