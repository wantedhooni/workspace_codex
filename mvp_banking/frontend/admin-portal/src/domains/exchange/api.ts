import { request } from "../../shared/api/http";
import type { ExchangeRequest } from "../../types";

export const adminExchangeApi = {
  exchangeRequests(token: string) {
    return request<ExchangeRequest[]>("/api/admin/exchange-requests", {}, token);
  },
};
