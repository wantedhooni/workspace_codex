import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { executeQuantSignal, getQuantSignalExecution } from "./quantSignalExecution";

const TOKEN_KEY = "quant_admin_basic_token";

function createLocalStorageMock() {
  const store = new Map<string, string>();
  return {
    getItem: (key: string) => store.get(key) ?? null,
    setItem: (key: string, value: string) => {
      store.set(key, value);
    },
    removeItem: (key: string) => {
      store.delete(key);
    },
    clear: () => {
      store.clear();
    }
  } as Storage;
}

describe("quantSignalExecution", () => {
  beforeEach(() => {
    Object.defineProperty(globalThis, "localStorage", {
      value: createLocalStorageMock(),
      configurable: true
    });
    localStorage.clear();
  });

  afterEach(() => {
    vi.restoreAllMocks();
    localStorage.clear();
  });

  it("should execute quant signal and return transaction data", async () => {
    localStorage.setItem(TOKEN_KEY, btoa("demo:demo1234"));
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({
        data: {
          id: 101,
          transactionType: "BUY"
        }
      })
    });
    vi.stubGlobal("fetch", fetchMock);

    const result = await executeQuantSignal(7, {
      portfolioId: 3,
      quantity: 2,
      unitPrice: 1000
    });

    expect(fetchMock).toHaveBeenCalledWith(
      expect.stringContaining("/quant-signals/7/execute"),
      expect.objectContaining({
        method: "POST",
        headers: expect.objectContaining({
          Authorization: `Basic ${btoa("demo:demo1234")}`
        })
      })
    );
    expect(result).toEqual({
      id: 101,
      transactionType: "BUY"
    });
  });

  it("should throw backend error message when execution fails", async () => {
    localStorage.setItem(TOKEN_KEY, btoa("demo:demo1234"));
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({
      ok: false,
      json: async () => ({
        error: {
          message: "Signal type HOLD cannot be executed as transaction"
        }
      })
    }));

    await expect(executeQuantSignal(8, {
      portfolioId: 3,
      quantity: 1,
      unitPrice: 100
    })).rejects.toThrow("Signal type HOLD cannot be executed as transaction");
  });

  it("should return quant signal execution info", async () => {
    localStorage.setItem(TOKEN_KEY, btoa("demo:demo1234"));
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({
        data: {
          id: 11,
          signalId: 7,
          transactionId: 101,
          executedAt: "2026-02-15T07:00:00Z",
          executedBy: "user"
        }
      })
    }));

    await expect(getQuantSignalExecution(7)).resolves.toEqual({
      id: 11,
      signalId: 7,
      transactionId: 101,
      executedAt: "2026-02-15T07:00:00Z",
      executedBy: "user"
    });
  });
});
