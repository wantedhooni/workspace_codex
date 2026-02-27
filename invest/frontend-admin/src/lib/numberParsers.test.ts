import { describe, expect, it } from "vitest";
import { parseNullableNumber } from "./numberParsers";

describe("numberParsers", () => {
  it("should parse numeric values", () => {
    expect(parseNullableNumber(1)).toBe(1);
    expect(parseNullableNumber("2")).toBe(2);
    expect(parseNullableNumber("3.14")).toBe(3.14);
  });

  it("should return null for empty values", () => {
    expect(parseNullableNumber(null)).toBeNull();
    expect(parseNullableNumber(undefined)).toBeNull();
    expect(parseNullableNumber("")).toBeNull();
  });

  it("should return null for invalid numeric string", () => {
    expect(parseNullableNumber("abc")).toBeNull();
  });
});
