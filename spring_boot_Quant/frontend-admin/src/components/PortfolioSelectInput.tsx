import { SelectInput, type SelectInputProps } from "react-admin";
import { usePortfolioCatalog } from "../portfolio/portfolioCatalog";

type Props = Omit<SelectInputProps, "choices" | "optionText" | "optionValue">;

export function PortfolioSelectInput(props: Props) {
  const { choices } = usePortfolioCatalog();

  return (
    <SelectInput
      {...props}
      choices={choices}
      optionText="label"
      optionValue="id"
      emptyText={props.alwaysOn ? undefined : "전체"}
    />
  );
}
