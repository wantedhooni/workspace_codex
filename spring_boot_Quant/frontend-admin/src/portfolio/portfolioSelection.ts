import { useCallback, useEffect, useState } from "react";

const PORTFOLIO_STORAGE_KEY = "quant.admin.selectedPortfolioId";
const PORTFOLIO_EVENT = "quant:portfolio-changed";

function normalizePortfolioId(value: unknown, fallback: number): number {
  const parsed = Number(value);
  if (!Number.isFinite(parsed) || parsed <= 0) {
    return fallback;
  }
  return Math.trunc(parsed);
}

export function getSelectedPortfolioId(fallback = 1): number {
  if (typeof window === "undefined") {
    return fallback;
  }
  const raw = window.localStorage.getItem(PORTFOLIO_STORAGE_KEY);
  return normalizePortfolioId(raw, fallback);
}

export function setSelectedPortfolioId(nextPortfolioId: number) {
  if (typeof window === "undefined") {
    return;
  }
  const normalized = normalizePortfolioId(nextPortfolioId, 1);
  window.localStorage.setItem(PORTFOLIO_STORAGE_KEY, String(normalized));
  window.dispatchEvent(new Event(PORTFOLIO_EVENT));
}

export function useSelectedPortfolioId(defaultPortfolioId = 1) {
  const [portfolioId, setPortfolioId] = useState<number>(() => getSelectedPortfolioId(defaultPortfolioId));

  useEffect(() => {
    if (typeof window === "undefined") {
      return;
    }

    const sync = () => setPortfolioId(getSelectedPortfolioId(defaultPortfolioId));
    window.addEventListener(PORTFOLIO_EVENT, sync);
    window.addEventListener("storage", sync);
    return () => {
      window.removeEventListener(PORTFOLIO_EVENT, sync);
      window.removeEventListener("storage", sync);
    };
  }, [defaultPortfolioId]);

  const update = useCallback((next: number) => {
    const normalized = normalizePortfolioId(next, defaultPortfolioId);
    setSelectedPortfolioId(normalized);
    setPortfolioId(normalized);
  }, [defaultPortfolioId]);

  return [portfolioId, update] as const;
}
