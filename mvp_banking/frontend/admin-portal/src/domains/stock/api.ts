import { request } from "../../shared/api/http";
import type { StockOrder, StockPosition } from "../../types";

export const adminStockApi = {
  stockOrders(token: string) {
    return request<StockOrder[]>("/api/admin/stock-orders", {}, token);
  },
  completeStockOrderFill(token: string, orderId: string) {
    return request<void>(
      `/api/admin/stock-orders/${orderId}/complete-fill`,
      {
        method: "POST",
      },
      token,
    );
  },
  stockPositions(token: string) {
    return request<StockPosition[]>("/api/admin/stock-positions", {}, token);
  },
};
