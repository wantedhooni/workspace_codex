import { request } from "../../shared/api/http";
import type { Account } from "./types";

export const accountApi = {
  list(token: string) {
    return request<Account[]>("/api/user/accounts", {}, token);
  },
};
