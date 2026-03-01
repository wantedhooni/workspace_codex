import { useEffect, useMemo, useState } from "react";
import {
  Button,
  Grid,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
} from "@mui/material";
import { api } from "../api/client";
import { PageTitle } from "../components/PageTitle";
import type { PagedResponse, StockPosition } from "../types/models";
import { pageDescriptions } from "../utils/pageDescriptions";
import { buildRsqlFilter, createEnterSearchHandler } from "../utils/listSearch";

export function StockPositionsPage() {
  const [rows, setRows] = useState<StockPosition[]>([]);
  const [symbolFilter, setSymbolFilter] = useState("");
  const [accountIdFilter, setAccountIdFilter] = useState("");

  const rsqlFilter = useMemo(
    () =>
      buildRsqlFilter([
        { field: "symbol", operator: "==", value: symbolFilter.trim() || null },
        { field: "accountId", operator: "==", value: accountIdFilter.trim() || null },
      ]),
    [symbolFilter, accountIdFilter],
  );

  const load = async () => {
    const params = new URLSearchParams();
    params.set("size", "100");
    if (symbolFilter.trim()) params.set("symbol", symbolFilter.trim());
    if (accountIdFilter.trim()) params.set("accountId", accountIdFilter.trim());
    if (rsqlFilter) params.set("filter", rsqlFilter);
    const res = await api.get<PagedResponse<StockPosition>>(`/stock-positions?${params.toString()}`);
    setRows(res.content);
  };

  const onEnterSearch = createEnterSearchHandler(() => {
    void load();
  });

  useEffect(() => {
    load().catch(console.error);
  }, []);

  return (
    <Stack spacing={2}>
      <PageTitle title="Stock Positions" description={pageDescriptions.stockPositions} />

      <Stack direction={{ xs: "column", md: "row" }} spacing={1} alignItems={{ xs: "stretch", md: "center" }}>
        <TextField label="Symbol" value={symbolFilter} onChange={(e) => setSymbolFilter(e.target.value)} onKeyDown={onEnterSearch} fullWidth />
        <TextField label="Account ID" value={accountIdFilter} onChange={(e) => setAccountIdFilter(e.target.value)} onKeyDown={onEnterSearch} fullWidth />
        <Stack direction="row" spacing={1}>
          <Button variant="contained" onClick={() => load().catch(console.error)}>
            Search
          </Button>
          <Button
            variant="outlined"
            onClick={() => {
              setSymbolFilter("");
              setAccountIdFilter("");
              void api.get<PagedResponse<StockPosition>>("/stock-positions?size=100").then((res) => setRows(res.content));
            }}
          >
            Reset
          </Button>
        </Stack>
      </Stack>

      <Grid container spacing={2}>
        <Grid item xs={12}>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Account</TableCell>
                <TableCell>Symbol</TableCell>
                <TableCell>Market</TableCell>
                <TableCell>Currency</TableCell>
                <TableCell>Quantity</TableCell>
                <TableCell>Average Price</TableCell>
                <TableCell>Total Cost</TableCell>
                <TableCell>Last Trade</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.map((row) => (
                <TableRow key={row.id}>
                  <TableCell>{row.id}</TableCell>
                  <TableCell>
                    {row.accountId} ({row.accountNo})
                  </TableCell>
                  <TableCell>{row.symbol}</TableCell>
                  <TableCell>{row.market}</TableCell>
                  <TableCell>{row.currency}</TableCell>
                  <TableCell>{row.quantity}</TableCell>
                  <TableCell>{row.averagePrice}</TableCell>
                  <TableCell>{row.totalCost}</TableCell>
                  <TableCell>{row.lastTradeDate}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Grid>
      </Grid>
    </Stack>
  );
}
