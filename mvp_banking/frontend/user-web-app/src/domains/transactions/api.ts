import { request, withQuery } from "../../shared/api/http";
import type { PageResponse, PaginationState } from "../../shared/types/page";
import type { Transaction } from "./types";

export const transactionApi = {
  list(token: string, pagination: PaginationState) {
    return request<PageResponse<Transaction>>(withQuery("/api/user/transactions", pagination), {}, token);
  },
};
