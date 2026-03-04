export type Transaction = {
  id: string;
  accountId: string;
  transactionNumber: string;
  transactionType: string;
  status: string;
  amount: number;
  currency: string;
  description: string | null;
  occurredAt: string;
};
