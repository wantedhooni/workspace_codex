import { request } from "../../shared/api/http";
import type { CreateStockOrderPayload, StockOrder, StockPosition } from "./types";

export const stockApi = {
  orders(token: string) {
    return request<StockOrder[]>("/api/user/stock-orders", {}, token);
  },
  positions(token: string) {
    return request<StockPosition[]>("/api/user/stock-positions", {}, token);
  },
  create(token: string, payload: CreateStockOrderPayload) {
    return request<StockOrder>(
      "/api/user/stock-orders",
      {
        method: "POST",
        body: JSON.stringify(payload),
      },
      token,
    );
  },
};
