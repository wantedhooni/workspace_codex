import type { Validator } from "react-admin";

export function positiveNumber(message = "Must be greater than zero"): Validator {
  return (value: unknown) => {
    if (value === null || value === undefined || value === "") {
      return undefined;
    }
    const numberValue = Number(value);
    if (Number.isNaN(numberValue) || numberValue <= 0) {
      return message;
    }
    return undefined;
  };
}

export function nonNegativeNumber(message = "Must be greater than or equal to zero"): Validator {
  return (value: unknown) => {
    if (value === null || value === undefined || value === "") {
      return undefined;
    }
    const numberValue = Number(value);
    if (Number.isNaN(numberValue) || numberValue < 0) {
      return message;
    }
    return undefined;
  };
}
