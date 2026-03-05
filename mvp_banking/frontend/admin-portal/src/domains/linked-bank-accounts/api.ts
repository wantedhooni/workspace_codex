import { request } from "../../shared/api/http";
import type { LinkedBankAccount } from "../../types";

export const adminLinkedBankAccountsApi = {
  linkedBankAccounts(token: string) {
    return request<LinkedBankAccount[]>("/api/admin/linked-bank-accounts", {}, token);
  },
  activateLinkedBankAccount(token: string, linkedBankAccountId: string) {
    return request<LinkedBankAccount>(
      `/api/admin/linked-bank-accounts/${linkedBankAccountId}/activate`,
      {
        method: "POST",
      },
      token,
    );
  },
  blockLinkedBankAccount(token: string, linkedBankAccountId: string) {
    return request<LinkedBankAccount>(
      `/api/admin/linked-bank-accounts/${linkedBankAccountId}/block`,
      {
        method: "POST",
      },
      token,
    );
  },
};
