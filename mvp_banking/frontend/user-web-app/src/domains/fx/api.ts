import { request } from "../../shared/api/http";
import type { FxRate } from "./types";

export const fxApi = {
  list(token: string) {
    return request<FxRate[]>("/api/user/fx-rates", {}, token);
  },
};
