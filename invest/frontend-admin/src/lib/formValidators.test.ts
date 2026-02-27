import { describe, expect, it } from "vitest";
import { nonNegativeNumber, positiveNumber } from "./formValidators";

describe("formValidators", () => {
  it("positiveNumber should reject zero and negative values", () => {
    const validator = positiveNumber();
    expect(validator(1)).toBeUndefined();
    expect(validator("2")).toBeUndefined();
    expect(validator(0)).toBe("Must be greater than zero");
    expect(validator(-1)).toBe("Must be greater than zero");
    expect(validator("abc")).toBe("Must be greater than zero");
  });

  it("positiveNumber should allow empty values", () => {
    const validator = positiveNumber();
    expect(validator(undefined)).toBeUndefined();
    expect(validator(null)).toBeUndefined();
    expect(validator("")).toBeUndefined();
  });

  it("nonNegativeNumber should reject negative values only", () => {
    const validator = nonNegativeNumber();
    expect(validator(0)).toBeUndefined();
    expect(validator(1)).toBeUndefined();
    expect(validator("3")).toBeUndefined();
    expect(validator(-1)).toBe("Must be greater than or equal to zero");
    expect(validator("abc")).toBe("Must be greater than or equal to zero");
  });
});
