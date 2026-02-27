import { Datagrid, FunctionField, List, NumberField, NumberInput, TextField, TextInput } from "react-admin";
import { PortfolioSelectInput } from "../components/PortfolioSelectInput";
import { RecordDetailButton } from "../components/RecordDetailButton";
import { usePortfolioCatalog } from "../portfolio/portfolioCatalog";
import { useSelectedPortfolioId } from "../portfolio/portfolioSelection";

export function LedgerEntryList() {
  const [selectedPortfolioId] = useSelectedPortfolioId(1);
  const { formatPortfolioLabel } = usePortfolioCatalog();

  return (
    <List
      title="원장 엔트리"
      perPage={25}
      filters={[
        <PortfolioSelectInput key="portfolioId" source="portfolioId" label="포트폴리오" alwaysOn />,
        <TextInput key="accountCode" source="accountCode" label="계정코드" />,
        <NumberInput key="voucherId" source="voucherId" label="Voucher ID" />
      ]}
      filter={{ portfolioId: selectedPortfolioId }}
    >
      <Datagrid bulkActionButtons={false} rowClick={false}>
        <TextField source="createdAt" label="생성시각" />
        <TextField source="accountCode" label="계정" />
        <TextField source="drCr" label="차/대" />
        <NumberField source="amount" label="금액" />
        <NumberField source="voucherId" label="Voucher ID" />
        <FunctionField label="포트폴리오" render={(record) => formatPortfolioLabel(record.portfolioId)} />
        <NumberField source="ledgerEntryId" label="Entry ID" />
        <FunctionField label="상세" render={() => <RecordDetailButton title="원장 엔트리 상세" />} />
      </Datagrid>
    </List>
  );
}
