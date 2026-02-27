import { describe, expect, it } from "vitest";
import {
  isTradeTransaction,
  normalizeCreateTransactionPayload,
  normalizeUpdateTransactionPayload
} from "./transactionForm";

describe("transactionForm", () => {
  it("should detect BUY and SELL as trade transactions", () => {
    expect(isTradeTransaction("BUY")).toBe(true);
    expect(isTradeTransaction("SELL")).toBe(true);
    expect(isTradeTransaction("DEPOSIT")).toBe(false);
    expect(isTradeTransaction(null)).toBe(false);
  });

  it("should normalize create payload for trade transaction", () => {
    const payload = normalizeCreateTransactionPayload({
      transactionType: "BUY",
      instrumentId: 10,
      quantity: 2,
      unitPrice: 1000,
      amount: 5000,
      memo: ""
    });

    expect(payload.instrumentId).toBe(10);
    expect(payload.quantity).toBe(2);
    expect(payload.unitPrice).toBe(1000);
    expect(payload.amount).toBeNull();
    expect(payload.memo).toBeNull();
  });

  it("should normalize create payload for cash transaction", () => {
    const payload = normalizeCreateTransactionPayload({
      transactionType: "DEPOSIT",
      instrumentId: 10,
      quantity: 2,
      unitPrice: 1000,
      amount: 5000
    });

    expect(payload.instrumentId).toBeNull();
    expect(payload.quantity).toBeNull();
    expect(payload.unitPrice).toBeNull();
    expect(payload.amount).toBe(5000);
  });

  it("should normalize update payload using transaction type", () => {
    const payload = normalizeUpdateTransactionPayload(
      {
        instrumentId: 10,
        quantity: "",
        unitPrice: "",
        amount: 5000
      },
      "SELL"
    );

    expect(payload.instrumentId).toBe(10);
    expect(payload.quantity).toBeNull();
    expect(payload.unitPrice).toBeNull();
    expect(payload.amount).toBeNull();
  });
});
