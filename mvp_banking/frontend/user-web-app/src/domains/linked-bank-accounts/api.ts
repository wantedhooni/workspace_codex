import { request } from "../../shared/api/http";
import type { CreateLinkedBankAccountPayload, LinkedBankAccount, VerifyLinkedBankAccountPayload } from "./types";

export const linkedBankAccountApi = {
  list(token: string) {
    return request<LinkedBankAccount[]>("/api/user/linked-bank-accounts", {}, token);
  },
  create(token: string, payload: CreateLinkedBankAccountPayload) {
    return request<LinkedBankAccount>(
      "/api/user/linked-bank-accounts",
      {
        method: "POST",
        body: JSON.stringify(payload),
      },
      token,
    );
  },
  markPrimary(token: string, linkedBankAccountId: string) {
    return request<LinkedBankAccount>(
      `/api/user/linked-bank-accounts/${linkedBankAccountId}/primary`,
      {
        method: "POST",
      },
      token,
    );
  },
  verify(token: string, linkedBankAccountId: string, payload: VerifyLinkedBankAccountPayload) {
    return request<LinkedBankAccount>(
      `/api/user/linked-bank-accounts/${linkedBankAccountId}/verify`,
      {
        method: "POST",
        body: JSON.stringify(payload),
      },
      token,
    );
  },
  resendVerification(token: string, linkedBankAccountId: string) {
    return request<LinkedBankAccount>(
      `/api/user/linked-bank-accounts/${linkedBankAccountId}/resend-verification`,
      {
        method: "POST",
      },
      token,
    );
  },
};
