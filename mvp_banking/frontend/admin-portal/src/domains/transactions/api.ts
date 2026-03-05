import { request, withQuery } from "../../shared/api/http";
import type { AdminListParams, PageResponse, Transaction } from "../../types";

export const adminTransactionsApi = {
  transactions(token: string, params: AdminListParams) {
    return request<PageResponse<Transaction>>(withQuery("/api/admin/transactions", params), {}, token);
  },
};
