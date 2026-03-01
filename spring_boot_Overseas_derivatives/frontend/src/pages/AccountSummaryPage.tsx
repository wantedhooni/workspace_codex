import { useEffect, useState } from "react";
import {
  Card,
  CardContent,
  Grid,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import { useSearchParams, useParams } from "react-router-dom";
import { api } from "../api/client";
import { PageTitle } from "../components/PageTitle";
import { pageDescriptions } from "../utils/pageDescriptions";

type Summary = {
  accountId: number;
  accountNo: string;
  broker: string;
  status: string;
  snapshotDate: string;
  balances: { currency: string; amount: number }[];
  positions: { symbol: string; quantity: number; avgPrice: number }[];
  margin?: {
    initialMargin: number;
    maintenanceMargin: number;
    availableMargin: number;
  };
};

export function AccountSummaryPage() {
  const { id } = useParams();
  const [params] = useSearchParams();
  const [summary, setSummary] = useState<Summary | null>(null);

  useEffect(() => {
    if (!id) return;
    const unmask = params.get("unmask") === "true";
    api.get<Summary>(`/accounts/${id}/summary${unmask ? "?unmask=true" : ""}`)
      .then(setSummary)
      .catch(console.error);
  }, [id, params]);

  if (!summary) return <Typography>Loading...</Typography>;

  return (
    <Grid container spacing={2}>
      <Grid item xs={12}>
        <Card>
          <CardContent>
            <PageTitle title={`Account Summary #${summary.accountId}`} description={pageDescriptions.accountSummary} variant="h6" />
            <Typography>
              {summary.accountNo} / {summary.broker} / {summary.status} / snapshot: {summary.snapshotDate}
            </Typography>
          </CardContent>
        </Card>
      </Grid>
      <Grid item xs={12} md={4}>
        <Card>
          <CardContent>
            <Typography variant="subtitle1" fontWeight={700}>
              Balances
            </Typography>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Currency</TableCell>
                  <TableCell>Amount</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {summary.balances.map((b) => (
                  <TableRow key={b.currency}>
                    <TableCell>{b.currency}</TableCell>
                    <TableCell>{b.amount}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      </Grid>
      <Grid item xs={12} md={4}>
        <Card>
          <CardContent>
            <Typography variant="subtitle1" fontWeight={700}>
              Positions
            </Typography>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Symbol</TableCell>
                  <TableCell>Qty</TableCell>
                  <TableCell>Avg Price</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {summary.positions.map((p) => (
                  <TableRow key={p.symbol}>
                    <TableCell>{p.symbol}</TableCell>
                    <TableCell>{p.quantity}</TableCell>
                    <TableCell>{p.avgPrice}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      </Grid>
      <Grid item xs={12} md={4}>
        <Card>
          <CardContent>
            <Typography variant="subtitle1" fontWeight={700}>
              Margin
            </Typography>
            <Typography>Initial: {summary.margin?.initialMargin ?? "-"}</Typography>
            <Typography>Maintenance: {summary.margin?.maintenanceMargin ?? "-"}</Typography>
            <Typography>Available: {summary.margin?.availableMargin ?? "-"}</Typography>
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );
}
