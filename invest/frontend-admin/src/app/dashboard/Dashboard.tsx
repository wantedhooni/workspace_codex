"use client";

import { Alert, Box, Card, CardContent, CardHeader, List, ListItem, ListItemText, Skeleton, Typography } from "@mui/material";
import { useGetList } from "react-admin";

type TransactionRecord = {
  id: number;
  transactionType?: string;
  tradeDate?: string;
  amount?: number;
};

type StatCardProps = {
  title: string;
  total?: number;
  isPending: boolean;
  error?: unknown;
};

function StatCard({ title, total, isPending, error }: StatCardProps) {
  return (
    <Card>
      <CardHeader title={title} />
      <CardContent>
        {isPending ? (
          <Skeleton variant="text" width={96} />
        ) : error ? (
          <Typography color="error">Failed to load</Typography>
        ) : (
          <Typography variant="h4" component="p">
            {(total ?? 0).toLocaleString()}
          </Typography>
        )}
      </CardContent>
    </Card>
  );
}

export default function Dashboard() {
  const portfolios = useGetList("portfolios", {
    pagination: { page: 1, perPage: 1 },
    sort: { field: "id", order: "ASC" }
  });

  const instruments = useGetList("instruments", {
    pagination: { page: 1, perPage: 1 },
    sort: { field: "id", order: "ASC" }
  });

  const transactions = useGetList<TransactionRecord>("transactions", {
    pagination: { page: 1, perPage: 5 },
    sort: { field: "tradeDate", order: "DESC" }
  });

  const holdings = useGetList("holdings", {
    pagination: { page: 1, perPage: 1 },
    sort: { field: "id", order: "ASC" }
  });

  const quantStrategies = useGetList("quant-strategies", {
    pagination: { page: 1, perPage: 1 },
    sort: { field: "createdAt", order: "DESC" }
  });

  const quantSignals = useGetList("quant-signals", {
    pagination: { page: 1, perPage: 1 },
    sort: { field: "signalDate", order: "DESC" }
  });

  const macroIndicators = useGetList("macro-indicators", {
    pagination: { page: 1, perPage: 1 },
    sort: { field: "observedDate", order: "DESC" }
  });

  const hasError =
    portfolios.error ||
    instruments.error ||
    transactions.error ||
    holdings.error ||
    quantStrategies.error ||
    quantSignals.error ||
    macroIndicators.error;

  return (
    <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
      <Box>
        <Typography variant="h5" gutterBottom>
          Quant Portal Dashboard
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Portfolio, instrument, transaction, and holding snapshots from the current API.
        </Typography>
      </Box>

      {hasError ? (
        <Box>
          <Alert severity="warning">Some dashboard widgets could not be loaded. Check API and auth status.</Alert>
        </Box>
      ) : null}

      <Box
        sx={{
          display: "grid",
          gap: 2,
          gridTemplateColumns: {
            xs: "1fr",
            sm: "repeat(2, minmax(0, 1fr))",
            lg: "repeat(4, minmax(0, 1fr))"
          }
        }}
      >
        <StatCard
          title="Portfolios"
          total={portfolios.total}
          isPending={portfolios.isPending}
          error={portfolios.error}
        />
        <StatCard
          title="Instruments"
          total={instruments.total}
          isPending={instruments.isPending}
          error={instruments.error}
        />
        <StatCard
          title="Transactions"
          total={transactions.total}
          isPending={transactions.isPending}
          error={transactions.error}
        />
        <StatCard title="Holdings" total={holdings.total} isPending={holdings.isPending} error={holdings.error} />
        <StatCard
          title="Quant Strategies"
          total={quantStrategies.total}
          isPending={quantStrategies.isPending}
          error={quantStrategies.error}
        />
        <StatCard
          title="Quant Signals"
          total={quantSignals.total}
          isPending={quantSignals.isPending}
          error={quantSignals.error}
        />
        <StatCard
          title="Macro Indicators"
          total={macroIndicators.total}
          isPending={macroIndicators.isPending}
          error={macroIndicators.error}
        />
      </Box>

      <Box
        sx={{
          display: "grid",
          gap: 2,
          gridTemplateColumns: {
            xs: "1fr",
            xl: "2fr 1fr"
          }
        }}
      >
        <Card>
          <CardHeader title="Recent Transactions" />
          <CardContent>
            {transactions.isPending ? (
              <>
                <Skeleton height={24} />
                <Skeleton height={24} />
                <Skeleton height={24} />
              </>
            ) : (
              <List dense>
                {(transactions.data ?? []).map((transaction) => (
                  <ListItem key={transaction.id} divider>
                    <ListItemText
                      primary={`#${transaction.id} ${transaction.transactionType ?? "UNKNOWN"}`}
                      secondary={`${transaction.tradeDate ?? "-"} | amount: ${transaction.amount ?? 0}`}
                    />
                  </ListItem>
                ))}
              </List>
            )}
          </CardContent>
        </Card>
        <Card>
          <CardHeader title="Quick Start" />
          <CardContent>
            <Typography variant="body2" paragraph>
              1. Create portfolio
            </Typography>
            <Typography variant="body2" paragraph>
              2. Register instruments
            </Typography>
            <Typography variant="body2" paragraph>
              3. Record transactions and holdings
            </Typography>
            <Typography variant="body2" paragraph>
              4. Add quant strategies and daily signals
            </Typography>
            <Typography variant="body2">5. Register macro indicators and review cross-domain context</Typography>
          </CardContent>
        </Card>
      </Box>
    </Box>
  );
}
