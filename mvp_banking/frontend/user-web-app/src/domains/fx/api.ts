import { request, withQuery } from "../../shared/api/http";
import type { PageResponse, PaginationState } from "../../shared/types/page";
import type { FxRate } from "./types";

export const fxApi = {
  list(token: string, pagination: PaginationState) {
    return request<PageResponse<FxRate>>(withQuery("/api/user/fx-rates", pagination), {}, token);
  },
};
