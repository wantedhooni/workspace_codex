import { request, withQuery } from "../../shared/api/http";
import type { PageResponse, PaginationState } from "../../shared/types/page";
import type { Account } from "./types";

export const accountApi = {
  list(token: string, pagination: PaginationState) {
    return request<PageResponse<Account>>(withQuery("/api/user/accounts", pagination), {}, token);
  },
};
