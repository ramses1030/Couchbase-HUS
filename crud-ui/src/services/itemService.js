import api from "./api";

export const getItems = () => api.get("/items");
export const createItem = (item) => api.post("/items", item);
export const deleteItem = (id) => api.delete(`/items/${id}`);
