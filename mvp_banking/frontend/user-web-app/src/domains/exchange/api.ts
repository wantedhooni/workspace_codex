import { request } from "../../shared/api/http";
import type { CancelExchangeRequestPayload, CreateExchangeRequestPayload, ExchangeRequest } from "./types";

export const exchangeApi = {
  list(token: string) {
    return request<ExchangeRequest[]>("/api/user/exchange-requests", {}, token);
  },
  create(token: string, payload: CreateExchangeRequestPayload) {
    return request<ExchangeRequest>(
      "/api/user/exchange-requests",
      {
        method: "POST",
        body: JSON.stringify(payload),
      },
      token,
    );
  },
  cancel(token: string, requestId: string, payload: CancelExchangeRequestPayload) {
    return request<ExchangeRequest>(
      `/api/user/exchange-requests/${requestId}/cancel`,
      {
        method: "POST",
        body: JSON.stringify(payload),
      },
      token,
    );
  },
};
