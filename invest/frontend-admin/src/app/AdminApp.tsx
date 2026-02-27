"use client";

import AccountBalanceWalletIcon from "@mui/icons-material/AccountBalanceWallet";
import AutoGraphIcon from "@mui/icons-material/AutoGraph";
import CandlestickChartIcon from "@mui/icons-material/CandlestickChart";
import GroupIcon from "@mui/icons-material/Group";
import ReceiptLongIcon from "@mui/icons-material/ReceiptLong";
import SecurityIcon from "@mui/icons-material/Security";
import { Box, Button, Card, CardContent, Divider, Stack, Typography } from "@mui/material";
import { useEffect, useState } from "react";
import {
  Admin,
  BooleanField,
  BooleanInput,
  Create,
  Datagrid,
  DateField,
  Edit,
  FormDataConsumer,
  FunctionField,
  List,
  NumberField,
  NumberInput,
  PasswordInput,
  ReferenceManyField,
  Resource,
  SelectArrayInput,
  SelectInput,
  SimpleForm,
  TextField,
  TextInput,
  required,
  useGetList,
  useNotify,
  useRefresh,
  useRecordContext
} from "react-admin";
import { AuthPermissions, authProvider, hasMenuPermission } from "../lib/authProvider";
import { dataProvider } from "../lib/dataProvider";
import Dashboard from "./dashboard/Dashboard";
import AppLayout from "./layout/AppLayout";
import LocalLoginPage from "./LocalLoginPage";
import { darkTheme, lightTheme } from "./theme";
import {
  isTradeTransaction,
  normalizeCreateTransactionPayload,
  normalizeUpdateTransactionPayload
} from "../lib/transactionForm";
import { executeQuantSignal, getQuantSignalExecution, QuantSignalExecutionInfo } from "../lib/quantSignalExecution";
import { nonNegativeNumber, positiveNumber } from "../lib/formValidators";
import { parseNullableNumber } from "../lib/numberParsers";

const currencyChoices = [
  { id: "KRW", name: "KRW" },
  { id: "USD", name: "USD" }
];

const marketChoices = [
  { id: "US", name: "US" },
  { id: "KR", name: "KR" }
];

const transactionTypeChoices = [
  { id: "BUY", name: "BUY" },
  { id: "SELL", name: "SELL" },
  { id: "DEPOSIT", name: "DEPOSIT" },
  { id: "WITHDRAW", name: "WITHDRAW" },
  { id: "DIVIDEND", name: "DIVIDEND" },
  { id: "FEE_ADJUST", name: "FEE_ADJUST" }
];

const quantStyleChoices = [
  { id: "MOMENTUM", name: "MOMENTUM" },
  { id: "VALUE", name: "VALUE" },
  { id: "QUALITY", name: "QUALITY" },
  { id: "LOW_VOLATILITY", name: "LOW_VOLATILITY" },
  { id: "MEAN_REVERSION", name: "MEAN_REVERSION" },
  { id: "TREND_FOLLOWING", name: "TREND_FOLLOWING" },
  { id: "MACRO_ROTATION", name: "MACRO_ROTATION" },
  { id: "CUSTOM", name: "CUSTOM" }
];

const strategyStatusChoices = [
  { id: "DRAFT", name: "DRAFT" },
  { id: "ACTIVE", name: "ACTIVE" },
  { id: "PAUSED", name: "PAUSED" },
  { id: "ARCHIVED", name: "ARCHIVED" }
];

const signalTypeChoices = [
  { id: "BUY", name: "BUY" },
  { id: "SELL", name: "SELL" },
  { id: "HOLD", name: "HOLD" },
  { id: "OVERWEIGHT", name: "OVERWEIGHT" },
  { id: "UNDERWEIGHT", name: "UNDERWEIGHT" }
];

const macroRegionChoices = [
  { id: "KR", name: "KR" },
  { id: "US", name: "US" },
  { id: "GLOBAL", name: "GLOBAL" }
];

const roleCodeChoices = [
  { id: "ROLE_ADMIN", name: "ROLE_ADMIN" },
  { id: "ROLE_USER", name: "ROLE_USER" }
];

const menuKeyChoices = [
  { id: "dashboard", name: "dashboard" },
  { id: "portfolios", name: "portfolios" },
  { id: "instruments", name: "instruments" },
  { id: "transactions", name: "transactions" },
  { id: "holdings", name: "holdings" },
  { id: "users", name: "users" },
  { id: "quant-strategies", name: "quant-strategies" },
  { id: "quant-signals", name: "quant-signals" },
  { id: "macro-indicators", name: "macro-indicators" },
  { id: "menu-permissions", name: "menu-permissions" }
];

function PortfolioList() {
  return (
    <List>
      <Datagrid rowClick="edit">
        <NumberField source="id" />
        <TextField source="name" />
        <TextField source="baseCurrency" />
        <NumberField source="cashBalance" />
        <NumberField source="eodValuationAmount" label="EOD Valuation" />
        <NumberField source="eodProfitLoss" label="EOD P/L" />
        <NumberField source="eodReturnRate" label="EOD Return (%)" />
        <DateField source="eodPriceDate" label="EOD Price Date" />
      </Datagrid>
    </List>
  );
}

function PortfolioCreate() {
  return (
    <Create>
      <SimpleForm>
        <TextInput source="name" validate={required()} />
        <SelectInput source="baseCurrency" choices={currencyChoices} defaultValue="KRW" />
      </SimpleForm>
    </Create>
  );
}

function PortfolioEdit() {
  return (
    <Edit>
      <SimpleForm>
        <TextInput source="name" validate={required()} />
        <PortfolioRelatedDomains />
      </SimpleForm>
    </Edit>
  );
}

type PortfolioRecord = {
  id: number;
  name: string;
  baseCurrency: string;
  cashBalance: number | string;
  investedAmount?: number | string | null;
  marketValue?: number | string | null;
  eodValuationAmount?: number | string | null;
  eodProfitLoss?: number | string | null;
  eodReturnRate?: number | string | null;
  eodPriceDate?: string | null;
  pricedHoldings?: number | null;
  totalHoldings?: number | null;
};

type TransactionSummaryRecord = {
  id: number;
  tradeDate: string;
};

function formatNumber(value: unknown): string {
  const numberValue = typeof value === "number" ? value : Number(value);
  if (!Number.isFinite(numberValue)) {
    return "-";
  }
  return numberValue.toLocaleString(undefined, { maximumFractionDigits: 4 });
}

function formatPercent(value: unknown): string {
  const numberValue = typeof value === "number" ? value : Number(value);
  if (!Number.isFinite(numberValue)) {
    return "-";
  }
  return `${numberValue.toLocaleString(undefined, { maximumFractionDigits: 2 })}%`;
}

function PortfolioRelatedDomains() {
  const record = useRecordContext<PortfolioRecord>();

  const holdings = useGetList("holdings", {
    pagination: { page: 1, perPage: 1 },
    sort: { field: "id", order: "DESC" },
    filter: { portfolioId: record?.id }
  });
  const transactions = useGetList<TransactionSummaryRecord>("transactions", {
    pagination: { page: 1, perPage: 1 },
    sort: { field: "tradeDate", order: "DESC" },
    filter: { portfolioId: record?.id }
  });

  if (!record) {
    return null;
  }

  return (
    <Box sx={{ mt: 2 }}>
      <Divider sx={{ mb: 2 }} />
      <Typography variant="h6" sx={{ mb: 1 }}>
        Related Domains
      </Typography>
      <Box
        sx={{
          display: "grid",
          gap: 2,
          mb: 2,
          gridTemplateColumns: {
            xs: "1fr",
            sm: "repeat(2, minmax(0, 1fr))",
            md: "repeat(4, minmax(0, 1fr))"
          }
        }}
      >
        <Card variant="outlined">
          <CardContent>
            <Typography variant="caption" color="text.secondary">
              Cash Balance
            </Typography>
            <Typography variant="h6">{formatNumber(record.cashBalance)} {record.baseCurrency}</Typography>
          </CardContent>
        </Card>
        <Card variant="outlined">
          <CardContent>
            <Typography variant="caption" color="text.secondary">
              Holdings
            </Typography>
            <Typography variant="h6">{holdings.total ?? 0}</Typography>
          </CardContent>
        </Card>
        <Card variant="outlined">
          <CardContent>
            <Typography variant="caption" color="text.secondary">
              Transactions
            </Typography>
            <Typography variant="h6">{transactions.total ?? 0}</Typography>
          </CardContent>
        </Card>
        <Card variant="outlined">
          <CardContent>
            <Typography variant="caption" color="text.secondary">
              EOD Return
            </Typography>
            <Typography variant="h6">{formatPercent(record.eodReturnRate)}</Typography>
          </CardContent>
        </Card>
        <Card variant="outlined">
          <CardContent>
            <Typography variant="caption" color="text.secondary">
              EOD Profit / Loss
            </Typography>
            <Typography variant="h6">{formatNumber(record.eodProfitLoss)} {record.baseCurrency}</Typography>
          </CardContent>
        </Card>
        <Card variant="outlined">
          <CardContent>
            <Typography variant="caption" color="text.secondary">
              EOD Price Date
            </Typography>
            <Typography variant="h6">{record.eodPriceDate ?? "-"}</Typography>
          </CardContent>
        </Card>
        <Card variant="outlined">
          <CardContent>
            <Typography variant="caption" color="text.secondary">
              Pricing Coverage
            </Typography>
            <Typography variant="h6">
              {record.pricedHoldings ?? 0} / {record.totalHoldings ?? 0}
            </Typography>
          </CardContent>
        </Card>
        <Card variant="outlined">
          <CardContent>
            <Typography variant="caption" color="text.secondary">
              Latest Trade Date
            </Typography>
            <Typography variant="h6">{transactions.data?.[0]?.tradeDate ?? "-"}</Typography>
          </CardContent>
        </Card>
      </Box>

      <Stack spacing={2}>
        <Box>
          <Typography variant="subtitle1" sx={{ mb: 1 }}>
            Holdings (Top Quantity)
          </Typography>
          <ReferenceManyField reference="holdings" target="portfolioId" perPage={10} sort={{ field: "quantity", order: "DESC" }}>
            <Datagrid bulkActionButtons={false}>
              <TextField source="ticker" />
              <NumberField source="instrumentId" label="Instrument ID" />
              <NumberField source="quantity" />
              <NumberField source="averageCost" />
            </Datagrid>
          </ReferenceManyField>
        </Box>

        <Box>
          <Typography variant="subtitle1" sx={{ mb: 1 }}>
            Recent Transactions
          </Typography>
          <ReferenceManyField
            reference="transactions"
            target="portfolioId"
            perPage={10}
            sort={{ field: "tradeDate", order: "DESC" }}
          >
            <Datagrid bulkActionButtons={false} rowClick="edit">
              <DateField source="tradeDate" />
              <TextField source="transactionType" />
              <NumberField source="instrumentId" label="Instrument ID" />
              <NumberField source="quantity" />
              <NumberField source="unitPrice" />
              <NumberField source="amount" />
              <NumberField source="cashImpact" />
              <FunctionField
                label="Memo"
                render={(row: { memo?: string }) => {
                  const memo = row.memo?.trim();
                  return memo ? memo : "-";
                }}
              />
            </Datagrid>
          </ReferenceManyField>
        </Box>
      </Stack>
    </Box>
  );
}

function InstrumentList() {
  return (
    <List>
      <Datagrid rowClick="edit">
        <NumberField source="id" />
        <TextField source="ticker" />
        <TextField source="name" />
        <TextField source="marketCode" />
        <TextField source="currencyCode" />
      </Datagrid>
    </List>
  );
}

function InstrumentCreate() {
  return (
    <Create>
      <SimpleForm>
        <TextInput source="ticker" validate={required()} />
        <TextInput source="name" validate={required()} />
        <SelectInput source="marketCode" choices={marketChoices} defaultValue="US" validate={required()} />
        <SelectInput source="currencyCode" choices={currencyChoices} defaultValue="USD" validate={required()} />
      </SimpleForm>
    </Create>
  );
}

function InstrumentEdit() {
  return (
    <Edit>
      <SimpleForm>
        <TextInput source="name" validate={required()} />
      </SimpleForm>
    </Edit>
  );
}

const transactionFilters = [
  <TextInput key="portfolioId" source="portfolioId" label="Portfolio ID" alwaysOn />,
  <TextInput key="instrumentId" source="instrumentId" label="Instrument ID" />,
  <SelectInput key="transactionType" source="transactionType" choices={transactionTypeChoices} />,
  <TextInput key="fromDate" source="fromDate" label="From Date (YYYY-MM-DD)" />,
  <TextInput key="toDate" source="toDate" label="To Date (YYYY-MM-DD)" />
];

function TransactionList() {
  return (
    <List filters={transactionFilters}>
      <Datagrid rowClick="edit">
        <NumberField source="id" />
        <NumberField source="portfolioId" />
        <NumberField source="instrumentId" />
        <TextField source="transactionType" />
        <DateField source="tradeDate" />
        <NumberField source="quantity" />
        <NumberField source="unitPrice" />
        <NumberField source="amount" />
        <NumberField source="cashImpact" />
        <NumberField source="realizedPnl" />
        <TextField source="currencyCode" />
      </Datagrid>
    </List>
  );
}

function TransactionCreate() {
  return (
    <Create transform={normalizeCreateTransactionPayload}>
      <SimpleForm>
        <NumberInput source="portfolioId" parse={parseNullableNumber} validate={required()} />
        <SelectInput source="transactionType" choices={transactionTypeChoices} validate={required()} />
        <TextInput source="tradeDate" type="date" validate={required()} />
        <FormDataConsumer>
          {({ formData }) =>
            isTradeTransaction(formData.transactionType as string | null | undefined) ? (
              <>
                <NumberInput source="instrumentId" parse={parseNullableNumber} validate={required()} />
                <NumberInput source="quantity" parse={parseNullableNumber} validate={[required(), positiveNumber()]} />
                <NumberInput source="unitPrice" parse={parseNullableNumber} validate={[required(), positiveNumber()]} />
              </>
            ) : (
              <NumberInput source="amount" parse={parseNullableNumber} validate={[required(), positiveNumber()]} />
            )
          }
        </FormDataConsumer>
        <NumberInput source="fee" parse={parseNullableNumber} validate={nonNegativeNumber()} />
        <NumberInput source="tax" parse={parseNullableNumber} validate={nonNegativeNumber()} />
        <SelectInput source="currencyCode" choices={currencyChoices} defaultValue="KRW" />
        <TextInput source="memo" multiline />
      </SimpleForm>
    </Create>
  );
}

function TransactionEdit() {
  return (
    <Edit
      transform={(data, context) =>
        normalizeUpdateTransactionPayload(
          data as Record<string, unknown>,
          (context?.previousData?.transactionType as string | null | undefined) ??
            (data?.transactionType as string | null | undefined)
        )
      }
    >
      <SimpleForm>
        <TextInput source="transactionType" disabled />
        <TextInput source="tradeDate" type="date" validate={required()} />
        <FormDataConsumer>
          {({ formData }) =>
            isTradeTransaction(formData.transactionType as string | null | undefined) ? (
              <>
                <NumberInput source="instrumentId" parse={parseNullableNumber} validate={required()} />
                <NumberInput source="quantity" parse={parseNullableNumber} validate={[required(), positiveNumber()]} />
                <NumberInput source="unitPrice" parse={parseNullableNumber} validate={[required(), positiveNumber()]} />
              </>
            ) : (
              <NumberInput source="amount" parse={parseNullableNumber} validate={[required(), positiveNumber()]} />
            )
          }
        </FormDataConsumer>
        <NumberInput source="fee" parse={parseNullableNumber} validate={nonNegativeNumber()} />
        <NumberInput source="tax" parse={parseNullableNumber} validate={nonNegativeNumber()} />
        <SelectInput source="currencyCode" choices={currencyChoices} defaultValue="KRW" />
        <TextInput source="memo" multiline />
      </SimpleForm>
    </Edit>
  );
}

function HoldingList() {
  return (
    <List>
      <Datagrid>
        <NumberField source="id" />
        <NumberField source="portfolioId" />
        <NumberField source="instrumentId" />
        <TextField source="ticker" />
        <NumberField source="quantity" />
        <NumberField source="averageCost" />
      </Datagrid>
    </List>
  );
}

const adminUserFilters = [
  <TextInput key="keyword" source="keyword" label="Keyword" alwaysOn />,
  <SelectInput key="roleCode" source="roleCode" label="Role" choices={roleCodeChoices} />
];

function AdminUserList() {
  return (
    <List filters={adminUserFilters}>
      <Datagrid rowClick="edit">
        <NumberField source="id" />
        <TextField source="username" />
        <TextField source="displayName" />
        <FunctionField
          label="Roles"
          render={(row: { roleCodes?: string[] }) => (row.roleCodes ?? []).join(", ")}
        />
        <BooleanField source="enabled" />
      </Datagrid>
    </List>
  );
}

function normalizeAdminUserCreatePayload(data: Record<string, unknown>) {
  return {
    username: data.username,
    password: data.password,
    displayName: data.displayName,
    roleCodes: data.roleCodes,
    enabled: data.enabled
  };
}

function AdminUserCreate() {
  return (
    <Create transform={(data) => normalizeAdminUserCreatePayload(data as Record<string, unknown>)}>
      <SimpleForm>
        <TextInput source="username" validate={required()} />
        <TextInput source="displayName" validate={required()} />
        <PasswordInput source="password" validate={required()} />
        <SelectArrayInput source="roleCodes" choices={roleCodeChoices} validate={required()} defaultValue={["ROLE_USER"]} />
        <BooleanInput source="enabled" defaultValue />
      </SimpleForm>
    </Create>
  );
}

function normalizeAdminUserUpdatePayload(data: Record<string, unknown>) {
  const rawPassword = typeof data.newPassword === "string" ? data.newPassword.trim() : "";
  return {
    displayName: data.displayName,
    roleCodes: data.roleCodes,
    enabled: data.enabled,
    newPassword: rawPassword.length > 0 ? rawPassword : null
  };
}

function AdminUserEdit() {
  return (
    <Edit transform={(data) => normalizeAdminUserUpdatePayload(data as Record<string, unknown>)}>
      <SimpleForm>
        <TextInput source="username" disabled />
        <TextInput source="displayName" validate={required()} />
        <PasswordInput source="newPassword" helperText="Leave blank to keep current password" />
        <SelectArrayInput source="roleCodes" choices={roleCodeChoices} validate={required()} />
        <BooleanInput source="enabled" />
      </SimpleForm>
    </Edit>
  );
}

const quantStrategyFilters = [
  <TextInput key="keyword" source="keyword" label="Keyword" alwaysOn />,
  <SelectInput key="style" source="style" label="Style" choices={quantStyleChoices} />,
  <SelectInput key="status" source="status" label="Status" choices={strategyStatusChoices} />
];

function QuantStrategyList() {
  return (
    <List filters={quantStrategyFilters}>
      <Datagrid rowClick="edit">
        <NumberField source="id" />
        <TextField source="name" />
        <TextField source="style" />
        <TextField source="status" />
        <NumberField source="rebalanceCycleDays" />
      </Datagrid>
    </List>
  );
}

function QuantStrategyCreate() {
  return (
    <Create>
      <SimpleForm>
        <TextInput source="name" validate={required()} />
        <SelectInput source="style" choices={quantStyleChoices} validate={required()} />
        <SelectInput source="status" choices={strategyStatusChoices} defaultValue="DRAFT" />
        <NumberInput
          source="rebalanceCycleDays"
          parse={parseNullableNumber}
          validate={[required(), positiveNumber()]}
          defaultValue={20}
        />
        <TextInput source="description" multiline minRows={3} />
      </SimpleForm>
    </Create>
  );
}

function QuantStrategyEdit() {
  return (
    <Edit>
      <SimpleForm>
        <TextInput source="name" validate={required()} />
        <SelectInput source="style" choices={quantStyleChoices} validate={required()} />
        <SelectInput source="status" choices={strategyStatusChoices} validate={required()} />
        <NumberInput source="rebalanceCycleDays" parse={parseNullableNumber} validate={[required(), positiveNumber()]} />
        <TextInput source="description" multiline minRows={3} />
      </SimpleForm>
    </Edit>
  );
}

const quantSignalFilters = [
  <TextInput key="strategyId" source="strategyId" label="Strategy ID" alwaysOn />,
  <TextInput key="instrumentId" source="instrumentId" label="Instrument ID" />,
  <SelectInput key="signalType" source="signalType" label="Signal Type" choices={signalTypeChoices} />,
  <TextInput key="fromDate" source="fromDate" label="From Date (YYYY-MM-DD)" />,
  <TextInput key="toDate" source="toDate" label="To Date (YYYY-MM-DD)" />
];

function QuantSignalList() {
  return (
    <List filters={quantSignalFilters}>
      <Datagrid rowClick="edit">
        <NumberField source="id" />
        <NumberField source="strategyId" />
        <TextField source="strategyName" />
        <NumberField source="instrumentId" />
        <TextField source="ticker" />
        <TextField source="signalType" />
        <DateField source="signalDate" />
        <NumberField source="score" />
        <NumberField source="confidence" />
      </Datagrid>
    </List>
  );
}

function QuantSignalCreate() {
  return (
    <Create>
      <SimpleForm>
        <NumberInput source="strategyId" parse={parseNullableNumber} validate={[required(), positiveNumber()]} />
        <NumberInput source="instrumentId" parse={parseNullableNumber} validate={[required(), positiveNumber()]} />
        <SelectInput source="signalType" choices={signalTypeChoices} validate={required()} />
        <TextInput source="signalDate" type="date" validate={required()} />
        <NumberInput source="score" parse={parseNullableNumber} validate={required()} />
        <NumberInput
          source="confidence"
          parse={parseNullableNumber}
          validate={[required(), nonNegativeNumber()]}
          helperText="0.0 ~ 1.0"
        />
        <TextInput source="rationale" multiline minRows={3} />
      </SimpleForm>
    </Create>
  );
}

function normalizeQuantSignalUpdatePayload(data: Record<string, unknown>) {
  return {
    instrumentId: data.instrumentId,
    signalType: data.signalType,
    signalDate: data.signalDate,
    score: data.score,
    confidence: data.confidence,
    rationale: data.rationale
  };
}

function QuantSignalEdit() {
  const [executionRefreshKey, setExecutionRefreshKey] = useState(0);

  return (
    <Edit transform={(data) => normalizeQuantSignalUpdatePayload(data as Record<string, unknown>)}>
      <SimpleForm>
        <NumberInput source="strategyId" disabled />
        <TextInput source="strategyName" disabled />
        <NumberInput source="instrumentId" parse={parseNullableNumber} validate={[required(), positiveNumber()]} />
        <SelectInput source="signalType" choices={signalTypeChoices} validate={required()} />
        <TextInput source="signalDate" type="date" validate={required()} />
        <NumberInput source="score" parse={parseNullableNumber} validate={required()} />
        <NumberInput
          source="confidence"
          parse={parseNullableNumber}
          validate={[required(), nonNegativeNumber()]}
          helperText="0.0 ~ 1.0"
        />
        <TextInput source="rationale" multiline minRows={3} />
        <SignalExecutionHistory refreshKey={executionRefreshKey} />
        <Divider sx={{ width: "100%", mt: 2, mb: 1 }} />
        <Typography variant="subtitle1">Execute Signal</Typography>
        <NumberInput source="executionPortfolioId" parse={parseNullableNumber} />
        <NumberInput source="executionQuantity" parse={parseNullableNumber} />
        <NumberInput source="executionUnitPrice" parse={parseNullableNumber} />
        <TextInput source="executionTradeDate" type="date" helperText="Optional: default is signal date" />
        <NumberInput source="executionFee" parse={parseNullableNumber} validate={nonNegativeNumber()} />
        <NumberInput source="executionTax" parse={parseNullableNumber} validate={nonNegativeNumber()} />
        <SelectInput source="executionCurrencyCode" choices={currencyChoices} />
        <TextInput source="executionMemo" multiline minRows={2} />
        <SignalExecutionAction onExecuted={() => setExecutionRefreshKey((previous) => previous + 1)} />
      </SimpleForm>
    </Edit>
  );
}

type QuantSignalRecord = {
  id: number;
  signalType: string;
};

type QuantSignalExecutionFormData = {
  executionPortfolioId?: unknown;
  executionQuantity?: unknown;
  executionUnitPrice?: unknown;
  executionTradeDate?: unknown;
  executionFee?: unknown;
  executionTax?: unknown;
  executionCurrencyCode?: unknown;
  executionMemo?: unknown;
};

type SignalExecutionHistoryProps = {
  refreshKey: number;
};

function SignalExecutionHistory({ refreshKey }: SignalExecutionHistoryProps) {
  const record = useRecordContext<QuantSignalRecord>();
  const notify = useNotify();
  const [execution, setExecution] = useState<QuantSignalExecutionInfo | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!record?.id) {
      setExecution(null);
      return;
    }

    let active = true;
    setLoading(true);
    getQuantSignalExecution(record.id)
      .then((result) => {
        if (active) {
          setExecution(result);
        }
      })
      .catch((error) => {
        const message = error instanceof Error ? error.message : "Failed to load execution history";
        notify(message, { type: "warning" });
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [record?.id, refreshKey, notify]);

  if (!record?.id) {
    return null;
  }

  return (
    <Box sx={{ width: "100%", mt: 1 }}>
      <Typography variant="subtitle1">Execution History</Typography>
      {loading ? (
        <Typography variant="body2" color="text.secondary">Loading execution history...</Typography>
      ) : execution ? (
        <Stack spacing={0.5}>
          <Typography variant="body2">Transaction ID: {execution.transactionId}</Typography>
          <Typography variant="body2">Executed By: {execution.executedBy}</Typography>
          <Typography variant="body2">
            Executed At: {new Date(execution.executedAt).toLocaleString()}
          </Typography>
        </Stack>
      ) : (
        <Typography variant="body2" color="text.secondary">Not executed yet</Typography>
      )}
    </Box>
  );
}

function toRequiredNumber(value: unknown): number | null {
  const numberValue = typeof value === "number" ? value : Number(value);
  if (!Number.isFinite(numberValue) || numberValue <= 0) {
    return null;
  }
  return numberValue;
}

function toOptionalNonNegativeNumber(value: unknown): number | undefined {
  if (value === null || value === undefined || value === "") {
    return undefined;
  }
  const numberValue = typeof value === "number" ? value : Number(value);
  if (!Number.isFinite(numberValue) || numberValue < 0) {
    return undefined;
  }
  return numberValue;
}

function toOptionalString(value: unknown): string | undefined {
  if (typeof value !== "string") {
    return undefined;
  }
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : undefined;
}

type SignalExecutionActionProps = {
  onExecuted?: () => void;
};

function SignalExecutionAction({ onExecuted }: SignalExecutionActionProps) {
  const record = useRecordContext<QuantSignalRecord>();
  const notify = useNotify();
  const refresh = useRefresh();

  return (
    <FormDataConsumer<QuantSignalExecutionFormData>>
      {({ formData }) => {
        const handleExecute = async () => {
          if (!record?.id) {
            notify("Signal record not found", { type: "warning" });
            return;
          }

          const portfolioId = toRequiredNumber(formData.executionPortfolioId);
          const quantity = toRequiredNumber(formData.executionQuantity);
          const unitPrice = toRequiredNumber(formData.executionUnitPrice);
          if (!portfolioId || !quantity || !unitPrice) {
            notify("Portfolio ID, quantity, unit price are required", { type: "warning" });
            return;
          }

          try {
            const transaction = await executeQuantSignal(record.id, {
              portfolioId,
              quantity,
              unitPrice,
              tradeDate: toOptionalString(formData.executionTradeDate),
              fee: toOptionalNonNegativeNumber(formData.executionFee),
              tax: toOptionalNonNegativeNumber(formData.executionTax),
              currencyCode: toOptionalString(formData.executionCurrencyCode) as "KRW" | "USD" | undefined,
              memo: toOptionalString(formData.executionMemo)
            });
            notify(
              `Executed ${record.signalType} signal as ${transaction.transactionType} transaction #${transaction.id}`,
              { type: "info" }
            );
            refresh();
            onExecuted?.();
          } catch (error) {
            const message = error instanceof Error ? error.message : "Failed to execute signal";
            notify(message, { type: "error" });
          }
        };

        return (
          <Button type="button" variant="contained" startIcon={<CandlestickChartIcon />} onClick={handleExecute}>
            Execute Buy/Sell
          </Button>
        );
      }}
    </FormDataConsumer>
  );
}

const macroIndicatorFilters = [
  <TextInput key="keyword" source="keyword" label="Keyword" alwaysOn />,
  <SelectInput key="regionCode" source="regionCode" label="Region" choices={macroRegionChoices} />,
  <TextInput key="fromDate" source="fromDate" label="From Date (YYYY-MM-DD)" />,
  <TextInput key="toDate" source="toDate" label="To Date (YYYY-MM-DD)" />
];

function MacroIndicatorList() {
  return (
    <List filters={macroIndicatorFilters}>
      <Datagrid rowClick="edit">
        <NumberField source="id" />
        <TextField source="indicatorCode" />
        <TextField source="indicatorName" />
        <TextField source="regionCode" />
        <DateField source="observedDate" />
        <NumberField source="indicatorValue" />
        <TextField source="unit" />
      </Datagrid>
    </List>
  );
}

function MacroIndicatorCreate() {
  return (
    <Create>
      <SimpleForm>
        <TextInput source="indicatorCode" validate={required()} />
        <TextInput source="indicatorName" validate={required()} />
        <SelectInput source="regionCode" choices={macroRegionChoices} defaultValue="GLOBAL" validate={required()} />
        <TextInput source="observedDate" type="date" validate={required()} />
        <NumberInput source="indicatorValue" parse={parseNullableNumber} validate={required()} />
        <TextInput source="unit" />
        <TextInput source="source" />
      </SimpleForm>
    </Create>
  );
}

function normalizeMacroIndicatorUpdatePayload(data: Record<string, unknown>) {
  return {
    indicatorName: data.indicatorName,
    regionCode: data.regionCode,
    observedDate: data.observedDate,
    indicatorValue: data.indicatorValue,
    unit: data.unit,
    source: data.source
  };
}

function MacroIndicatorEdit() {
  return (
    <Edit transform={(data) => normalizeMacroIndicatorUpdatePayload(data as Record<string, unknown>)}>
      <SimpleForm>
        <TextInput source="indicatorCode" disabled />
        <TextInput source="indicatorName" validate={required()} />
        <SelectInput source="regionCode" choices={macroRegionChoices} validate={required()} />
        <TextInput source="observedDate" type="date" validate={required()} />
        <NumberInput source="indicatorValue" parse={parseNullableNumber} validate={required()} />
        <TextInput source="unit" />
        <TextInput source="source" />
      </SimpleForm>
    </Edit>
  );
}

const menuPermissionFilters = [
  <TextInput key="keyword" source="keyword" label="Menu Key" alwaysOn />,
  <SelectInput key="roleCode" source="roleCode" label="Role" choices={roleCodeChoices} />
];

function MenuPermissionList() {
  return (
    <List filters={menuPermissionFilters}>
      <Datagrid rowClick="edit">
        <NumberField source="id" />
        <TextField source="roleCode" />
        <TextField source="menuKey" />
        <BooleanField source="canList" />
        <BooleanField source="canCreate" />
        <BooleanField source="canEdit" />
        <BooleanField source="canDelete" />
      </Datagrid>
    </List>
  );
}

function MenuPermissionCreate() {
  return (
    <Create>
      <SimpleForm>
        <SelectInput source="roleCode" choices={roleCodeChoices} defaultValue="ROLE_USER" validate={required()} />
        <SelectInput source="menuKey" choices={menuKeyChoices} validate={required()} />
        <BooleanInput source="canList" defaultValue />
        <BooleanInput source="canCreate" />
        <BooleanInput source="canEdit" />
        <BooleanInput source="canDelete" />
      </SimpleForm>
    </Create>
  );
}

function MenuPermissionEdit() {
  return (
    <Edit>
      <SimpleForm>
        <SelectInput source="roleCode" choices={roleCodeChoices} validate={required()} />
        <SelectInput source="menuKey" choices={menuKeyChoices} validate={required()} />
        <BooleanInput source="canList" />
        <BooleanInput source="canCreate" />
        <BooleanInput source="canEdit" />
        <BooleanInput source="canDelete" />
      </SimpleForm>
    </Edit>
  );
}

function renderResources(permissions?: AuthPermissions) {
  return (
    <>
      {hasMenuPermission(permissions, "portfolios", "list") ? (
        <Resource
          name="portfolios"
          options={{ label: "Portfolios" }}
          icon={AccountBalanceWalletIcon}
          list={PortfolioList}
          create={hasMenuPermission(permissions, "portfolios", "create") ? PortfolioCreate : undefined}
          edit={hasMenuPermission(permissions, "portfolios", "edit") ? PortfolioEdit : undefined}
        />
      ) : null}

      {hasMenuPermission(permissions, "instruments", "list") ? (
        <Resource
          name="instruments"
          options={{ label: "Instruments" }}
          icon={CandlestickChartIcon}
          list={InstrumentList}
          create={hasMenuPermission(permissions, "instruments", "create") ? InstrumentCreate : undefined}
          edit={hasMenuPermission(permissions, "instruments", "edit") ? InstrumentEdit : undefined}
        />
      ) : null}

      {hasMenuPermission(permissions, "transactions", "list") ? (
        <Resource
          name="transactions"
          options={{ label: "Transactions" }}
          icon={ReceiptLongIcon}
          list={TransactionList}
          create={hasMenuPermission(permissions, "transactions", "create") ? TransactionCreate : undefined}
          edit={hasMenuPermission(permissions, "transactions", "edit") ? TransactionEdit : undefined}
        />
      ) : null}

      {hasMenuPermission(permissions, "holdings", "list") ? (
        <Resource name="holdings" options={{ label: "Holdings" }} icon={AutoGraphIcon} list={HoldingList} />
      ) : null}

      {hasMenuPermission(permissions, "users", "list") ? (
        <Resource
          name="users"
          options={{ label: "Users" }}
          icon={GroupIcon}
          list={AdminUserList}
          create={hasMenuPermission(permissions, "users", "create") ? AdminUserCreate : undefined}
          edit={hasMenuPermission(permissions, "users", "edit") ? AdminUserEdit : undefined}
        />
      ) : null}

      {hasMenuPermission(permissions, "quant-strategies", "list") ? (
        <Resource
          name="quant-strategies"
          options={{ label: "Quant Strategies" }}
          icon={AutoGraphIcon}
          list={QuantStrategyList}
          create={hasMenuPermission(permissions, "quant-strategies", "create") ? QuantStrategyCreate : undefined}
          edit={hasMenuPermission(permissions, "quant-strategies", "edit") ? QuantStrategyEdit : undefined}
        />
      ) : null}

      {hasMenuPermission(permissions, "quant-signals", "list") ? (
        <Resource
          name="quant-signals"
          options={{ label: "Quant Signals" }}
          icon={CandlestickChartIcon}
          list={QuantSignalList}
          create={hasMenuPermission(permissions, "quant-signals", "create") ? QuantSignalCreate : undefined}
          edit={hasMenuPermission(permissions, "quant-signals", "edit") ? QuantSignalEdit : undefined}
        />
      ) : null}

      {hasMenuPermission(permissions, "macro-indicators", "list") ? (
        <Resource
          name="macro-indicators"
          options={{ label: "Macro Indicators" }}
          icon={ReceiptLongIcon}
          list={MacroIndicatorList}
          create={hasMenuPermission(permissions, "macro-indicators", "create") ? MacroIndicatorCreate : undefined}
          edit={hasMenuPermission(permissions, "macro-indicators", "edit") ? MacroIndicatorEdit : undefined}
        />
      ) : null}

      {hasMenuPermission(permissions, "menu-permissions", "list") ? (
        <Resource
          name="menu-permissions"
          options={{ label: "Menu Permissions" }}
          icon={SecurityIcon}
          list={MenuPermissionList}
          create={hasMenuPermission(permissions, "menu-permissions", "create") ? MenuPermissionCreate : undefined}
          edit={hasMenuPermission(permissions, "menu-permissions", "edit") ? MenuPermissionEdit : undefined}
        />
      ) : null}
    </>
  );
}

export default function AdminApp() {
  return (
    <Admin
      title="Quant Portal Admin"
      dataProvider={dataProvider}
      authProvider={authProvider}
      dashboard={Dashboard}
      layout={AppLayout}
      loginPage={LocalLoginPage}
      theme={lightTheme}
      darkTheme={darkTheme}
      defaultTheme="light"
      disableTelemetry
      requireAuth
    >
      {(permissions) => renderResources(permissions as AuthPermissions | undefined)}
    </Admin>
  );
}
