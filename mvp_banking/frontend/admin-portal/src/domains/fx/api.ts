import { request } from "../../shared/api/http";
import type { FxRate } from "../../types";

export const adminFxApi = {
  fxRates(token: string) {
    return request<FxRate[]>("/api/admin/fx-rates", {}, token);
  },
};
