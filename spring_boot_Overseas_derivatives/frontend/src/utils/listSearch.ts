import type { KeyboardEvent } from "react";

export type RsqlOperator = "==" | "!=" | "=like=" | "=ge=" | "=le=" | "=gt=" | "=lt=";

export type RsqlCriterion = {
  field: string;
  operator?: RsqlOperator;
  value?: string | number | boolean | null;
};

function quoteIfNeeded(value: string | number | boolean): string {
  if (typeof value === "number" || typeof value === "boolean") {
    return String(value);
  }

  if (/^-?\d+(\.\d+)?$/.test(value)) {
    return value;
  }

  const escaped = value.replace(/'/g, "\\'");
  return `'${escaped}'`;
}

export function buildRsqlFilter(criteria: RsqlCriterion[]): string {
  const parts = criteria
    .filter((item) => item.value !== undefined && item.value !== null && String(item.value).trim() !== "")
    .map((item) => `${item.field}${item.operator ?? "=="}${quoteIfNeeded(item.value as string | number | boolean)}`);

  return parts.join(";");
}

export function createEnterSearchHandler(onSearch: () => void) {
  return (event: KeyboardEvent<HTMLElement>) => {
    if (event.key === "Enter") {
      event.preventDefault();
      onSearch();
    }
  };
}
