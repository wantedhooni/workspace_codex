import { request } from "../../shared/api/http";
import type { CancelFundingRequestPayload, CreateFundingRequestPayload, FundingRequest } from "./types";

export const fundingApi = {
  list(token: string) {
    return request<FundingRequest[]>("/api/user/funding-requests", {}, token);
  },
  create(token: string, payload: CreateFundingRequestPayload) {
    return request<FundingRequest>(
      "/api/user/funding-requests",
      {
        method: "POST",
        body: JSON.stringify(payload),
      },
      token,
    );
  },
  cancel(token: string, requestId: string, payload: CancelFundingRequestPayload) {
    return request<FundingRequest>(
      `/api/user/funding-requests/${requestId}/cancel`,
      {
        method: "POST",
        body: JSON.stringify(payload),
      },
      token,
    );
  },
};
