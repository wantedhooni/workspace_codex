import { CrudFilters } from "@refinedev/core";

const hasFilterValue = (value: unknown) => {
  if (value === null || value === undefined) {
    return false;
  }
  if (typeof value === "string") {
    return value.trim().length > 0;
  }
  return true;
};

export const toCrudFilters = (values: Record<string, unknown>): CrudFilters => {
  return Object.entries(values)
    .filter(([, value]) => hasFilterValue(value))
    .map(([field, value]) => ({
      field,
      operator: "eq",
      value: typeof value === "string" ? value.trim() : value,
    }));
};
