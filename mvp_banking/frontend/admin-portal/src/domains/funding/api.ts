import { request } from "../../shared/api/http";
import type { FundingRequest } from "../../types";

export const adminFundingApi = {
  fundingRequests(token: string) {
    return request<FundingRequest[]>("/api/admin/funding-requests", {}, token);
  },
};
