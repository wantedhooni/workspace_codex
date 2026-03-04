import { request } from "../../shared/api/http";
import type { Transaction } from "./types";

export const transactionApi = {
  list(token: string) {
    return request<Transaction[]>("/api/user/transactions", {}, token);
  },
};
