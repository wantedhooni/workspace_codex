const TRADE_TYPES = new Set(["BUY", "SELL"]);

type GenericPayload = Record<string, unknown>;

function emptyToNull(value: unknown): unknown {
  if (value === "" || value === undefined) {
    return null;
  }
  return value;
}

export function isTradeTransaction(transactionType: string | null | undefined): boolean {
  if (!transactionType) {
    return false;
  }
  return TRADE_TYPES.has(transactionType);
}

export function normalizeCreateTransactionPayload(data: GenericPayload): GenericPayload {
  const payload: GenericPayload = { ...data };
  const transactionType = typeof payload.transactionType === "string" ? payload.transactionType : null;
  const tradeType = isTradeTransaction(transactionType);

  payload.instrumentId = tradeType ? emptyToNull(payload.instrumentId) : null;
  payload.quantity = tradeType ? emptyToNull(payload.quantity) : null;
  payload.unitPrice = tradeType ? emptyToNull(payload.unitPrice) : null;
  payload.amount = tradeType ? null : emptyToNull(payload.amount);
  payload.memo = emptyToNull(payload.memo);
  payload.fee = emptyToNull(payload.fee);
  payload.tax = emptyToNull(payload.tax);

  return payload;
}

export function normalizeUpdateTransactionPayload(
  data: GenericPayload,
  transactionType: string | null | undefined
): GenericPayload {
  const payload: GenericPayload = { ...data };
  const tradeType = isTradeTransaction(transactionType);

  payload.instrumentId = tradeType ? emptyToNull(payload.instrumentId) : null;
  payload.quantity = tradeType ? emptyToNull(payload.quantity) : null;
  payload.unitPrice = tradeType ? emptyToNull(payload.unitPrice) : null;
  payload.amount = tradeType ? null : emptyToNull(payload.amount);
  payload.memo = emptyToNull(payload.memo);
  payload.fee = emptyToNull(payload.fee);
  payload.tax = emptyToNull(payload.tax);

  return payload;
}
