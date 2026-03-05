import { request, withQuery } from "../../shared/api/http";
import type { Account, AdminListParams, PageResponse } from "../../types";

export const adminAccountsApi = {
  accounts(token: string, params: AdminListParams) {
    return request<PageResponse<Account>>(withQuery("/api/admin/accounts", params), {}, token);
  },
};
