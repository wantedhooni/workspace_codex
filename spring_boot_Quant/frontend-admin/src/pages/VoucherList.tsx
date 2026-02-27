import {
  Button,
  Datagrid,
  FunctionField,
  List,
  NumberField,
  NumberInput,
  SelectInput,
  TextField,
  TextInput,
  TopToolbar,
  useDataProvider,
  minValue,
  useNotify,
  usePermissions,
  useRecordContext,
  useRefresh,
  required
} from "react-admin";
import { PortfolioSelectInput } from "../components/PortfolioSelectInput";
import { CreateDialogButton } from "../components/CreateDialogButton";
import { RecordDetailButton } from "../components/RecordDetailButton";
import type { AppPermissions } from "../providers/authProvider";
import { usePortfolioCatalog } from "../portfolio/portfolioCatalog";
import { useSelectedPortfolioId } from "../portfolio/portfolioSelection";

type VoucherStatus = "DRAFT" | "APPROVED" | "POSTED" | "CANCELED";
type VoucherEntryForm = {
  accountCode?: string;
  drCr?: string;
  amount?: number;
};

const money = new Intl.NumberFormat("ko-KR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });

const extractEntries = (values: Record<string, unknown>): VoucherEntryForm[] => {
  const entries = values.entries;
  if (!Array.isArray(entries)) {
    return [];
  }
  return entries as VoucherEntryForm[];
};

const voucherFormValidate = (values: Record<string, unknown>) => {
  const errors: Record<string, unknown> = {};
  const entries = extractEntries(values);
  const lineErrors: Record<number, Record<string, string>> = {};

  if (!values.portfolioId || Number(values.portfolioId) <= 0) {
    errors.portfolioId = "Portfolio는 1 이상이어야 합니다.";
  }

  if (entries.length < 2) {
    errors.entries = "최소 2개 분개 라인이 필요합니다.";
    return errors;
  }

  let totalDr = 0;
  let totalCr = 0;

  entries.forEach((entry, index) => {
    const amount = Number(entry?.amount ?? 0);
    const drCr = String(entry?.drCr ?? "");
    const accountCode = String(entry?.accountCode ?? "").trim();

    if (!accountCode) {
      lineErrors[index] = { ...(lineErrors[index] ?? {}), accountCode: "계정코드 필수" };
    }
    if (drCr !== "DR" && drCr !== "CR") {
      lineErrors[index] = { ...(lineErrors[index] ?? {}), drCr: "DR/CR 선택 필수" };
    }
    if (!Number.isFinite(amount) || amount <= 0) {
      lineErrors[index] = { ...(lineErrors[index] ?? {}), amount: "금액은 0보다 커야 합니다." };
    }

    if (drCr === "DR") totalDr += amount;
    if (drCr === "CR") totalCr += amount;
  });

  if (Object.keys(lineErrors).length > 0) {
    errors.entries = lineErrors;
  }

  if (Math.abs(totalDr - totalCr) > 0.000001) {
    errors.entries = "차변/대변 합계가 일치해야 합니다.";
  }

  return Object.keys(errors).length > 0 ? errors : undefined;
};

function VoucherListActions() {
  const { permissions } = usePermissions<AppPermissions>();
  const [selectedPortfolioId] = useSelectedPortfolioId(1);
  const canCreate = Boolean((permissions as AppPermissions | undefined)?.menuPermissions?.journalVouchers?.canCreate);

  return (
    <TopToolbar>
      {canCreate ? (
        <CreateDialogButton
          resource="journalVouchers"
          title="전표 생성"
          defaultValues={{
            portfolioId: selectedPortfolioId,
            description: "manual voucher",
            entries: [
              { accountCode: "STOCK_ASSET", drCr: "DR", amount: 100, symbol: "AAPL" },
              { accountCode: "CASH", drCr: "CR", amount: 100 }
            ]
          }}
          successMessage="전표가 생성되었습니다."
          maxWidth="lg"
          formValidate={voucherFormValidate}
        >
          <PortfolioSelectInput source="portfolioId" label="포트폴리오" validate={[required()]} />
          <NumberInput source="tradeId" label="Trade ID" />
          <TextInput source="description" label="설명" fullWidth validate={required()} />
          <TextInput source="entries[0].accountCode" label="라인1 계정" validate={required()} />
          <SelectInput
            source="entries[0].drCr"
            label="라인1 차대"
            validate={required()}
            choices={[
              { id: "DR", name: "DR" },
              { id: "CR", name: "CR" }
            ]}
          />
          <NumberInput source="entries[0].amount" label="라인1 금액" validate={[required(), minValue(0.01)]} />
          <TextInput source="entries[0].symbol" label="라인1 종목" />
          <TextInput source="entries[1].accountCode" label="라인2 계정" validate={required()} />
          <SelectInput
            source="entries[1].drCr"
            label="라인2 차대"
            validate={required()}
            choices={[
              { id: "DR", name: "DR" },
              { id: "CR", name: "CR" }
            ]}
          />
          <NumberInput source="entries[1].amount" label="라인2 금액" validate={[required(), minValue(0.01)]} />
        </CreateDialogButton>
      ) : null}
    </TopToolbar>
  );
}

function VoucherActionButtons() {
  const record = useRecordContext();
  const dp = useDataProvider();
  const notify = useNotify();
  const refresh = useRefresh();

  if (!record) return null;

  const status = (record.status as VoucherStatus | undefined) ?? "DRAFT";

  const run = async (action: "approve" | "post" | "cancel") => {
    try {
      await dp.update("journalVouchers", {
        id: record.voucherId,
        data: record,
        previousData: record,
        meta: { action }
      });
      notify(`${action} 성공`, { type: "info" });
      refresh();
    } catch (error) {
      notify(error instanceof Error ? error.message : `${action} 실패`, { type: "warning" });
    }
  };

  return (
    <>
      <Button label="승인" disabled={status !== "DRAFT"} onClick={() => run("approve")} />
      <Button label="전기" disabled={status !== "APPROVED"} onClick={() => run("post")} />
      <Button label="취소" disabled={status === "POSTED" || status === "CANCELED"} onClick={() => run("cancel")} />
    </>
  );
}

export function VoucherList() {
  const [selectedPortfolioId] = useSelectedPortfolioId(1);
  const { formatPortfolioLabel } = usePortfolioCatalog();

  return (
    <List
      title="전표"
      perPage={25}
      actions={<VoucherListActions />}
      filters={[
        <PortfolioSelectInput key="portfolioId" source="portfolioId" label="포트폴리오" alwaysOn />,
        <TextInput key="status" source="status" label="상태" />
      ]}
      filter={{ portfolioId: selectedPortfolioId }}
    >
      <Datagrid bulkActionButtons={false} rowClick={false}>
        <NumberField source="voucherId" label="Voucher ID" />
        <TextField source="voucherNo" label="전표번호" />
        <FunctionField label="포트폴리오" render={(record) => formatPortfolioLabel(record.portfolioId)} />
        <TextField source="status" label="상태" />
        <TextField source="createdAt" label="생성시각" />
        <FunctionField
          label="상세"
          render={() => (
            <RecordDetailButton
              title="전표 상세"
              summaryItems={[
                {
                  label: "라인 수",
                  value: (record) => String(Array.isArray(record.entries) ? record.entries.length : 0)
                },
                {
                  label: "차변 합계",
                  value: (record) => {
                    const entries = Array.isArray(record.entries) ? (record.entries as VoucherEntryForm[]) : [];
                    const dr = entries
                      .filter((line) => String(line.drCr ?? "").toUpperCase() === "DR")
                      .reduce((sum, line) => sum + Number(line.amount ?? 0), 0);
                    return money.format(dr);
                  }
                },
                {
                  label: "대변 합계",
                  value: (record) => {
                    const entries = Array.isArray(record.entries) ? (record.entries as VoucherEntryForm[]) : [];
                    const cr = entries
                      .filter((line) => String(line.drCr ?? "").toUpperCase() === "CR")
                      .reduce((sum, line) => sum + Number(line.amount ?? 0), 0);
                    return money.format(cr);
                  }
                }
              ]}
              fields={[
                { source: "voucherId", label: "Voucher ID" },
                { source: "voucherNo", label: "전표번호" },
                { source: "portfolioId", label: "Portfolio" },
                { source: "tradeId", label: "Trade ID" },
                { source: "status", label: "상태" },
                { source: "description", label: "설명" },
                { source: "createdAt", label: "생성시각" },
                { source: "approvedAt", label: "승인시각" },
                { source: "postedAt", label: "전기시각" },
                { source: "entries", label: "분개라인" }
              ]}
            />
          )}
        />
        <FunctionField label="액션" render={() => <VoucherActionButtons />} />
      </Datagrid>
    </List>
  );
}
