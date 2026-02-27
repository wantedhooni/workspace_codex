"use client";

import { getAuthorizationHeader } from "./authProvider";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api/v1";

type ApiSingleResponse<T> = {
  data: T;
};

type ApiErrorEnvelope = {
  error?: {
    message?: string;
  };
};

export type QuantSignalExecutionPayload = {
  portfolioId: number;
  quantity: number;
  unitPrice: number;
  tradeDate?: string;
  fee?: number;
  tax?: number;
  currencyCode?: "KRW" | "USD";
  memo?: string;
};

export type ExecutedTransaction = {
  id: number;
  transactionType: string;
};

export type QuantSignalExecutionInfo = {
  id: number;
  signalId: number;
  transactionId: number;
  executedAt: string;
  executedBy: string;
};

export async function executeQuantSignal(
  signalId: number,
  payload: QuantSignalExecutionPayload
): Promise<ExecutedTransaction> {
  const authorizationHeader = getAuthorizationHeader();
  if (!authorizationHeader) {
    throw new Error("Not authenticated");
  }

  const response = await fetch(`${API_BASE_URL}/quant-signals/${signalId}/execute`, {
    method: "POST",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
      Authorization: authorizationHeader
    },
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    const body = (await response.json().catch(() => null)) as ApiErrorEnvelope | null;
    throw new Error(body?.error?.message ?? "Failed to execute quant signal");
  }

  const body = (await response.json()) as ApiSingleResponse<ExecutedTransaction>;
  return body.data;
}

export async function getQuantSignalExecution(
  signalId: number
): Promise<QuantSignalExecutionInfo | null> {
  const authorizationHeader = getAuthorizationHeader();
  if (!authorizationHeader) {
    throw new Error("Not authenticated");
  }

  const response = await fetch(`${API_BASE_URL}/quant-signals/${signalId}/execution`, {
    method: "GET",
    headers: {
      Accept: "application/json",
      Authorization: authorizationHeader
    }
  });

  if (!response.ok) {
    const body = (await response.json().catch(() => null)) as ApiErrorEnvelope | null;
    throw new Error(body?.error?.message ?? "Failed to load quant signal execution");
  }

  const body = (await response.json()) as ApiSingleResponse<QuantSignalExecutionInfo | null>;
  return body.data;
}
