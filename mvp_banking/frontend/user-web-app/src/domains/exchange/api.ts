import { request } from "../../shared/api/http";
import type { CreateExchangeRequestPayload, ExchangeRequest } from "./types";

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
};
