import type { Transaction } from "../../domains/transactions/types";

export function sortTransactionsByRecent(left: Transaction, right: Transaction) {
  return new Date(right.occurredAt).getTime() - new Date(left.occurredAt).getTime();
}
