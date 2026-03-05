export type LinkedBankAccount = {
  id: string;
  customerId: string;
  customerEmail: string;
  bankName: string;
  accountAlias: string;
  accountHolderName: string;
  maskedAccountNumber: string;
  status: string;
  primaryWithdrawal: boolean;
  verifiedAt: string | null;
  verificationReference: string | null;
  verificationRequestedAt: string | null;
  verificationExpiresAt: string | null;
  verificationExpired: boolean;
  lastVerificationResentAt: string | null;
  verificationResendAvailableAt: string | null;
  verificationResendAllowed: boolean;
  verificationAttemptCount: number;
  blockReasonCode: string | null;
  blockedAt: string | null;
  createdAt: string;
};

export type CreateLinkedBankAccountPayload = {
  bankName: string;
  accountAlias: string;
  accountHolderName: string;
  accountNumber: string;
  primaryWithdrawal: boolean;
};

export type VerifyLinkedBankAccountPayload = {
  verificationReference: string;
};

export type LinkedBankPolicySummary = {
  activeCount: number;
  blockedCount: number;
  primaryCount: number;
};
