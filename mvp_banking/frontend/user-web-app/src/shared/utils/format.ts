export function formatAmount(value: number, currency: string) {
  return `${Number(value).toLocaleString()} ${currency}`;
}
