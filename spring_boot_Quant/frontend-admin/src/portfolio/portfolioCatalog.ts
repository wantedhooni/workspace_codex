import { useMemo } from "react";
import { useGetList } from "react-admin";

type PortfolioRow = {
  portfolioId?: number;
  portfolioCode?: string;
  portfolioName?: string;
  strategyTag?: string;
  benchmark?: string;
  baseCurrency?: string;
  active?: boolean;
};

export type PortfolioChoice = {
  id: number;
  code: string;
  name: string;
  label: string;
  strategyTag: string;
  benchmark: string;
  baseCurrency: string;
  active: boolean;
};

const fallbackChoices: PortfolioChoice[] = [
  {
    id: 1,
    code: "US_ALPHA_CORE",
    name: "US Alpha Core",
    label: "US_ALPHA_CORE · US Alpha Core",
    strategyTag: "L/S Momentum",
    benchmark: "S&P 500",
    baseCurrency: "USD",
    active: true
  }
];

export function usePortfolioCatalog() {
  const query = useGetList("portfolios", {
    pagination: { page: 1, perPage: 100 },
    sort: { field: "portfolioId", order: "ASC" },
    filter: {}
  });

  const choices = useMemo<PortfolioChoice[]>(() => {
    const rows = (query.data ?? []) as PortfolioRow[];
    if (!rows.length) {
      return fallbackChoices;
    }

    return rows
      .map((row) => {
        const id = Number(row.portfolioId ?? 0);
        if (!Number.isFinite(id) || id <= 0) {
          return null;
        }
        const code = String(row.portfolioCode ?? `PF-${id}`).trim();
        const name = String(row.portfolioName ?? `Portfolio ${id}`).trim();
        const strategyTag = String(row.strategyTag ?? "").trim();
        const benchmark = String(row.benchmark ?? "").trim();
        const baseCurrency = String(row.baseCurrency ?? "").trim();
        const active = Boolean(row.active ?? true);
        return {
          id,
          code,
          name,
          label: `${code} · ${name}`,
          strategyTag,
          benchmark,
          baseCurrency,
          active
        };
      })
      .filter((choice): choice is PortfolioChoice => choice !== null);
  }, [query.data]);

  const labelMap = useMemo(() => {
    const map = new Map<number, string>();
    choices.forEach((choice) => map.set(choice.id, choice.label));
    return map;
  }, [choices]);

  const formatPortfolioLabel = (portfolioId: unknown) => {
    const id = Number(portfolioId ?? 0);
    if (!Number.isFinite(id) || id <= 0) {
      return "미지정";
    }
    return labelMap.get(id) ?? `Portfolio ${id}`;
  };

  return {
    choices,
    formatPortfolioLabel,
    isPending: query.isPending,
    isFetching: query.isFetching
  };
}
