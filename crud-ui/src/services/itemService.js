import api from "./api";

export const getItems = () => api.get("/items");
export const getItem = (id) => api.get(`/items/${encodeURIComponent(id)}`);
export const createItem = (item) => api.post("/items", item);
export const deleteItem = (id) => api.delete(`/items/${encodeURIComponent(id)}`);

