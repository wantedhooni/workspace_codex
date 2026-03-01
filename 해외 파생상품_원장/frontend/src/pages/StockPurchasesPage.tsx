import { useEffect, useMemo, useState } from "react";
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Grid,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
} from "@mui/material";
import { api, getRole } from "../api/client";
import { PageTitle } from "../components/PageTitle";
import type { PagedResponse, StockPurchase } from "../types/models";
import { pageDescriptions } from "../utils/pageDescriptions";
import { buildRsqlFilter, createEnterSearchHandler } from "../utils/listSearch";

export function StockPurchasesPage() {
  const isAdmin = getRole() === "OPS_ADMIN";
  const today = new Date().toISOString().slice(0, 10);
  const [rows, setRows] = useState<StockPurchase[]>([]);
  const [keyword, setKeyword] = useState("");
  const [symbolFilter, setSymbolFilter] = useState("");
  const [accountIdFilter, setAccountIdFilter] = useState("");
  const [open, setOpen] = useState(false);

  const [accountId, setAccountId] = useState("1");
  const [symbol, setSymbol] = useState("AAPL");
  const [market, setMarket] = useState("NASDAQ");
  const [currency, setCurrency] = useState("USD");
  const [tradeDate, setTradeDate] = useState(today);
  const [settlementDate, setSettlementDate] = useState(today);
  const [quantity, setQuantity] = useState("10");
  const [price, setPrice] = useState("150");
  const [feeAmount, setFeeAmount] = useState("5");

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
    if (keyword.trim()) params.set("keyword", keyword.trim());
    if (symbolFilter.trim()) params.set("symbol", symbolFilter.trim());
    if (accountIdFilter.trim()) params.set("accountId", accountIdFilter.trim());
    if (rsqlFilter) params.set("filter", rsqlFilter);
    const res = await api.get<PagedResponse<StockPurchase>>(`/stock-purchases?${params.toString()}`);
    setRows(res.content);
  };

  const onEnterSearch = createEnterSearchHandler(() => {
    void load();
  });

  const reset = async () => {
    setKeyword("");
    setSymbolFilter("");
    setAccountIdFilter("");
    const res = await api.get<PagedResponse<StockPurchase>>("/stock-purchases?size=100");
    setRows(res.content);
  };

  useEffect(() => {
    load().catch(console.error);
  }, []);

  const createPurchase = async () => {
    await api.post("/stock-purchases", {
      accountId: Number(accountId),
      symbol,
      market,
      currency,
      tradeDate,
      settlementDate,
      quantity: Number(quantity),
      price: Number(price),
      feeAmount: Number(feeAmount),
    });
    setOpen(false);
    await load();
  };

  return (
    <Stack spacing={2}>
      <PageTitle title="Stock Purchases" description={pageDescriptions.stockPurchases} />

      <Stack direction={{ xs: "column", md: "row" }} spacing={1} alignItems={{ xs: "stretch", md: "center" }}>
        <TextField
          label="Keyword (symbol/market/currency/order/accountNo)"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={onEnterSearch}
          fullWidth
        />
        <Stack direction="row" spacing={1}>
          {isAdmin && (
            <Button variant="contained" onClick={() => setOpen(true)}>
              Create Purchase
            </Button>
          )}
          <Button variant="contained" onClick={() => load().catch(console.error)}>
            Search
          </Button>
          <Button variant="outlined" onClick={() => reset().catch(console.error)}>
            Reset
          </Button>
        </Stack>
      </Stack>

      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <TextField label="Symbol" value={symbolFilter} onChange={(e) => setSymbolFilter(e.target.value)} onKeyDown={onEnterSearch} fullWidth />
        </Grid>
        <Grid item xs={12} md={6}>
          <TextField label="Account ID" value={accountIdFilter} onChange={(e) => setAccountIdFilter(e.target.value)} onKeyDown={onEnterSearch} fullWidth />
        </Grid>
      </Grid>

      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>ID</TableCell>
            <TableCell>Trade Date</TableCell>
            <TableCell>Account</TableCell>
            <TableCell>Symbol</TableCell>
            <TableCell>Market</TableCell>
            <TableCell>Qty</TableCell>
            <TableCell>Price</TableCell>
            <TableCell>Gross</TableCell>
            <TableCell>Fee</TableCell>
            <TableCell>Net</TableCell>
            <TableCell>Order No</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((row) => (
            <TableRow key={row.id}>
              <TableCell>{row.id}</TableCell>
              <TableCell>{row.tradeDate}</TableCell>
              <TableCell>
                {row.accountId} ({row.accountNo})
              </TableCell>
              <TableCell>{row.symbol}</TableCell>
              <TableCell>{row.market}</TableCell>
              <TableCell>{row.quantity}</TableCell>
              <TableCell>{row.price}</TableCell>
              <TableCell>{row.grossAmount}</TableCell>
              <TableCell>{row.feeAmount}</TableCell>
              <TableCell>{row.netAmount}</TableCell>
              <TableCell>{row.brokerOrderNo}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>

      <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>주식 매수 등록</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <Grid container spacing={2}>
              <Grid item xs={12} md={6}>
                <TextField label="Account ID" value={accountId} onChange={(e) => setAccountId(e.target.value)} fullWidth />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField label="Symbol" value={symbol} onChange={(e) => setSymbol(e.target.value.toUpperCase())} fullWidth />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField label="Market" value={market} onChange={(e) => setMarket(e.target.value.toUpperCase())} fullWidth />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField label="Currency" value={currency} onChange={(e) => setCurrency(e.target.value.toUpperCase())} fullWidth />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField type="date" label="Trade Date" value={tradeDate} onChange={(e) => setTradeDate(e.target.value)} fullWidth InputLabelProps={{ shrink: true }} />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField type="date" label="Settlement Date" value={settlementDate} onChange={(e) => setSettlementDate(e.target.value)} fullWidth InputLabelProps={{ shrink: true }} />
              </Grid>
              <Grid item xs={12} md={4}>
                <TextField label="Quantity" value={quantity} onChange={(e) => setQuantity(e.target.value)} fullWidth />
              </Grid>
              <Grid item xs={12} md={4}>
                <TextField label="Price" value={price} onChange={(e) => setPrice(e.target.value)} fullWidth />
              </Grid>
              <Grid item xs={12} md={4}>
                <TextField label="Fee Amount" value={feeAmount} onChange={(e) => setFeeAmount(e.target.value)} fullWidth />
              </Grid>
            </Grid>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={() => createPurchase().catch(console.error)}>
            Save
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  );
}
