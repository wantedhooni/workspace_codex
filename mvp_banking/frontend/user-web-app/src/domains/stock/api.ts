import { request, withQuery } from "../../shared/api/http";
import type { PageResponse, PaginationState } from "../../shared/types/page";
import type { CancelStockOrderPayload, CreateStockOrderPayload, StockOrder, StockPosition } from "./types";

export const stockApi = {
  orders(token: string, pagination: PaginationState) {
    return request<PageResponse<StockOrder>>(withQuery("/api/user/stock-orders", pagination), {}, token);
  },
  positions(token: string, pagination: PaginationState) {
    return request<PageResponse<StockPosition>>(withQuery("/api/user/stock-positions", pagination), {}, token);
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
  cancel(token: string, orderId: string, payload: CancelStockOrderPayload) {
    return request<StockOrder>(
      `/api/user/stock-orders/${orderId}/cancel`,
      {
        method: "POST",
        body: JSON.stringify(payload),
      },
      token,
    );
  },
};
