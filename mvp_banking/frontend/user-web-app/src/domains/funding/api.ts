import { request } from "../../shared/api/http";
import type { CreateFundingRequestPayload, FundingRequest } from "./types";

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
};
