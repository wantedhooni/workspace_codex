import { beforeEach, describe, expect, it, vi } from "vitest";
import { dataProvider } from "./dataProvider";

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

describe("dataProvider", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    Object.defineProperty(globalThis, "localStorage", {
      value: createLocalStorageMock(),
      configurable: true
    });
    localStorage.clear();
    localStorage.setItem("quant_admin_basic_token", btoa("user:user1234"));
  });

  it("should send react-admin compatible list query params", async () => {
    const fetchMock = vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(
        JSON.stringify({
          data: [],
          total: 0
        }),
        { status: 200, headers: { "Content-Type": "application/json" } }
      )
    );

    await dataProvider.getList("portfolios", {
      pagination: { page: 2, perPage: 25 },
      sort: { field: "createdAt", order: "DESC" },
      filter: { keyword: "Core" }
    });

    const requestUrl = new URL(fetchMock.mock.calls[0][0] as string);
    expect(requestUrl.pathname).toBe("/api/v1/portfolios");
    expect(requestUrl.searchParams.get("range")).toBe("[25,49]");
    expect(requestUrl.searchParams.get("sort")).toBe("[\"createdAt\",\"DESC\"]");
    expect(requestUrl.searchParams.get("filter")).toBe("{\"keyword\":\"Core\"}");
  });

  it("should map backend error to error message and status", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(
        JSON.stringify({
          error: {
            message: "Bad request from backend"
          }
        }),
        { status: 400, headers: { "Content-Type": "application/json" } }
      )
    );

    await expect(
      dataProvider.getOne("portfolios", {
        id: 999
      })
    ).rejects.toMatchObject({
      message: "Bad request from backend",
      status: 400,
      body: {}
    });
  });

  it("should use admin-prefixed path for menu permissions resource", async () => {
    const fetchMock = vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(
        JSON.stringify({
          data: [],
          total: 0
        }),
        { status: 200, headers: { "Content-Type": "application/json" } }
      )
    );

    await dataProvider.getList("menu-permissions", {
      pagination: { page: 1, perPage: 25 },
      sort: { field: "createdAt", order: "DESC" },
      filter: {}
    });

    const requestUrl = new URL(fetchMock.mock.calls[0][0] as string);
    expect(requestUrl.pathname).toBe("/api/v1/admin/menu-permissions");
  });

  it("should use admin-prefixed path for users resource", async () => {
    const fetchMock = vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(
        JSON.stringify({
          data: [],
          total: 0
        }),
        { status: 200, headers: { "Content-Type": "application/json" } }
      )
    );

    await dataProvider.getList("users", {
      pagination: { page: 1, perPage: 25 },
      sort: { field: "createdAt", order: "DESC" },
      filter: {}
    });

    const requestUrl = new URL(fetchMock.mock.calls[0][0] as string);
    expect(requestUrl.pathname).toBe("/api/v1/admin/users");
  });

  it("should expose field errors for react-admin form validation", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(
        JSON.stringify({
          error: {
            message: "Validation failed",
            details: {
              amount: "amount must be null for transaction type BUY"
            }
          }
        }),
        { status: 400, headers: { "Content-Type": "application/json" } }
      )
    );

    await expect(
      dataProvider.create("transactions", {
        data: {
          portfolioId: 1,
          transactionType: "BUY",
          tradeDate: "2026-02-11",
          amount: 1000
        }
      })
    ).rejects.toMatchObject({
      message: "Validation failed",
      status: 400,
      body: {
        errors: {
          amount: "amount must be null for transaction type BUY"
        }
      }
    });
  });

  it("should return previous data on delete 204 response", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(new Response(null, { status: 204 }));

    const result = await dataProvider.delete("instruments", {
      id: 1,
      previousData: { id: 1, ticker: "AAPL" }
    });

    expect(result.data).toEqual({ id: 1, ticker: "AAPL" });
  });
});
