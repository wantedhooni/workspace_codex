import { useEffect, useState } from "react";
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Grid,
  MenuItem,
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
import type { ExchangeRate, PagedResponse } from "../types/models";
import { pageDescriptions } from "../utils/pageDescriptions";

export function ExchangeRatesPage() {
  const isAdmin = getRole() === "OPS_ADMIN";
  const today = new Date().toISOString().slice(0, 10);
  const [rows, setRows] = useState<ExchangeRate[]>([]);
  const [fromCurrency, setFromCurrency] = useState("");
  const [toCurrency, setToCurrency] = useState("");
  const [rateDate, setRateDate] = useState("");

  const [open, setOpen] = useState(false);
  const [createFromCurrency, setCreateFromCurrency] = useState("USD");
  const [createToCurrency, setCreateToCurrency] = useState("KRW");
  const [createRateDate, setCreateRateDate] = useState(today);
  const [createRate, setCreateRate] = useState("1328.45000000");
  const [createSource, setCreateSource] = useState("OPS_MANUAL");

  const load = async () => {
    const params = new URLSearchParams();
    params.set("size", "100");
    if (fromCurrency.trim()) params.set("fromCurrency", fromCurrency.trim().toUpperCase());
    if (toCurrency.trim()) params.set("toCurrency", toCurrency.trim().toUpperCase());
    if (rateDate.trim()) params.set("rateDate", rateDate.trim());
    const res = await api.get<PagedResponse<ExchangeRate>>(`/exchange-rates?${params.toString()}`);
    setRows(res.content);
  };

  const reset = async () => {
    setFromCurrency("");
    setToCurrency("");
    setRateDate("");
    const res = await api.get<PagedResponse<ExchangeRate>>("/exchange-rates?size=100");
    setRows(res.content);
  };

  useEffect(() => {
    load().catch(console.error);
  }, []);

  const save = async () => {
    await api.post("/exchange-rates", {
      fromCurrency: createFromCurrency,
      toCurrency: createToCurrency,
      rateDate: createRateDate,
      rate: Number(createRate),
      source: createSource,
    });
    setOpen(false);
    await load();
  };

  return (
    <Stack spacing={2}>
      <PageTitle title="Exchange Rates" description={pageDescriptions.exchangeRates} />

      <Stack direction={{ xs: "column", md: "row" }} spacing={1} alignItems={{ xs: "stretch", md: "center" }}>
        <Grid container spacing={2}>
          <Grid item xs={12} md={4}>
            <TextField label="From Currency" value={fromCurrency} onChange={(event) => setFromCurrency(event.target.value.toUpperCase())} fullWidth />
          </Grid>
          <Grid item xs={12} md={4}>
            <TextField label="To Currency" value={toCurrency} onChange={(event) => setToCurrency(event.target.value.toUpperCase())} fullWidth />
          </Grid>
          <Grid item xs={12} md={4}>
            <TextField type="date" label="Rate Date" value={rateDate} onChange={(event) => setRateDate(event.target.value)} fullWidth InputLabelProps={{ shrink: true }} />
          </Grid>
        </Grid>
        <Stack direction="row" spacing={1}>
          {isAdmin && (
            <Button variant="contained" onClick={() => setOpen(true)}>
              Add Rate
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

      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>ID</TableCell>
            <TableCell>From</TableCell>
            <TableCell>To</TableCell>
            <TableCell>Rate Date</TableCell>
            <TableCell>Rate</TableCell>
            <TableCell>Source</TableCell>
            <TableCell>Created At</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((row) => (
            <TableRow key={row.id}>
              <TableCell>{row.id}</TableCell>
              <TableCell>{row.fromCurrency}</TableCell>
              <TableCell>{row.toCurrency}</TableCell>
              <TableCell>{row.rateDate}</TableCell>
              <TableCell>{row.rate}</TableCell>
              <TableCell>{row.source}</TableCell>
              <TableCell>{row.createdAt}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>

      <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>환율 등록</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField select label="From Currency" value={createFromCurrency} onChange={(event) => setCreateFromCurrency(event.target.value)} fullWidth>
              {["USD", "EUR", "KRW", "JPY", "GBP", "CHF", "SGD", "HKD"].map((item) => (
                <MenuItem key={item} value={item}>{item}</MenuItem>
              ))}
            </TextField>
            <TextField select label="To Currency" value={createToCurrency} onChange={(event) => setCreateToCurrency(event.target.value)} fullWidth>
              {["USD", "EUR", "KRW", "JPY", "GBP", "CHF", "SGD", "HKD"].map((item) => (
                <MenuItem key={item} value={item}>{item}</MenuItem>
              ))}
            </TextField>
            <TextField type="date" label="Rate Date" value={createRateDate} onChange={(event) => setCreateRateDate(event.target.value)} fullWidth InputLabelProps={{ shrink: true }} />
            <TextField label="Rate" value={createRate} onChange={(event) => setCreateRate(event.target.value)} fullWidth />
            <TextField label="Source" value={createSource} onChange={(event) => setCreateSource(event.target.value)} fullWidth />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={() => save().catch(console.error)}>Save</Button>
        </DialogActions>
      </Dialog>
    </Stack>
  );
}
