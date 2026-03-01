import { useEffect, useMemo, useState } from "react";
import {
  Alert,
  Button,
  Card,
  CardContent,
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
import { useNavigate } from "react-router-dom";
import { api, getRole } from "../api/client";
import { PageTitle } from "../components/PageTitle";
import type { ExchangeRateQuote, PagedResponse, RequestRow } from "../types/models";
import { pageDescriptions } from "../utils/pageDescriptions";
import { buildRsqlFilter, createEnterSearchHandler } from "../utils/listSearch";

type DecisionState = {
  open: boolean;
  requestId: string;
  action: "approve" | "reject";
  reason: string;
};

export function FxRequestsPage() {
  const defaultValueDate = new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString().slice(0, 10);
  const navigate = useNavigate();
  const isAdmin = getRole() === "OPS_ADMIN";

  const [rows, setRows] = useState<RequestRow[]>([]);
  const [keyword, setKeyword] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [priorityFilter, setPriorityFilter] = useState("");
  const [accountIdFilter, setAccountIdFilter] = useState("");

  const [createOpen, setCreateOpen] = useState(false);
  const [createAccountId, setCreateAccountId] = useState("1");
  const [fromCurrency, setFromCurrency] = useState("USD");
  const [toCurrency, setToCurrency] = useState("KRW");
  const [createPriority, setCreatePriority] = useState("NORMAL");
  const [createValueDate, setCreateValueDate] = useState(defaultValueDate);
  const [amount, setAmount] = useState("10000");
  const [reason, setReason] = useState("Intraday FX conversion");
  const [quote, setQuote] = useState<ExchangeRateQuote | null>(null);
  const [quoteError, setQuoteError] = useState<string | null>(null);

  const [decision, setDecision] = useState<DecisionState>({
    open: false,
    requestId: "",
    action: "approve",
    reason: "",
  });

  const rsqlFilter = useMemo(
    () =>
      buildRsqlFilter([
        { field: "status", operator: "==", value: statusFilter || null },
        { field: "priority", operator: "==", value: priorityFilter || null },
        { field: "accountId", operator: "==", value: accountIdFilter.trim() || null },
      ]),
    [statusFilter, priorityFilter, accountIdFilter],
  );

  const load = async () => {
    const params = new URLSearchParams();
    params.set("size", "100");
    if (keyword.trim()) params.set("keyword", keyword.trim());
    if (rsqlFilter) params.set("filter", rsqlFilter);

    const res = await api.get<PagedResponse<RequestRow>>(`/fx-requests?${params.toString()}`);
    setRows(res.content);
  };

  const onEnterSearch = createEnterSearchHandler(() => {
    void load();
  });

  const reset = async () => {
    setKeyword("");
    setStatusFilter("");
    setPriorityFilter("");
    setAccountIdFilter("");
    const res = await api.get<PagedResponse<RequestRow>>("/fx-requests?size=100");
    setRows(res.content);
  };

  useEffect(() => {
    load().catch(console.error);
  }, []);

  useEffect(() => {
    if (!createOpen) {
      return;
    }
    if (!fromCurrency.trim() || !toCurrency.trim() || !amount.trim()) {
      setQuote(null);
      setQuoteError(null);
      return;
    }

    const numericAmount = Number(amount);
    if (!Number.isFinite(numericAmount) || numericAmount <= 0) {
      setQuote(null);
      setQuoteError(null);
      return;
    }

    const params = new URLSearchParams();
    params.set("fromCurrency", fromCurrency.trim().toUpperCase());
    params.set("toCurrency", toCurrency.trim().toUpperCase());
    params.set("amount", amount);
    if (createValueDate) {
      params.set("rateDate", createValueDate);
    }

    api.get<ExchangeRateQuote>(`/exchange-rates/quote?${params.toString()}`)
      .then((res) => {
        setQuote(res);
        setQuoteError(null);
      })
      .catch((err: Error) => {
        setQuote(null);
        setQuoteError(err.message);
      });
  }, [createOpen, fromCurrency, toCurrency, amount, createValueDate]);

  const createRequest = async () => {
    await api.post("/fx-requests", {
      accountId: Number(createAccountId),
      fromCurrency,
      toCurrency,
      amount: Number(amount),
      reason,
      priority: createPriority,
      valueDate: createValueDate,
    });
    setCreateOpen(false);
    await load();
  };

  const openDecisionModal = (requestId: string, action: "approve" | "reject") => {
    setDecision({ open: true, requestId, action, reason: "checked" });
  };

  const submitDecision = async () => {
    if (!decision.reason.trim()) {
      return;
    }
    await api.post(`/requests/${decision.requestId}/${decision.action}`, { reason: decision.reason });
    setDecision((prev) => ({ ...prev, open: false, reason: "" }));
    await load();
  };

  return (
    <Stack spacing={2}>
      <PageTitle title="FX Requests" description={pageDescriptions.fxRequests} />

      <Stack direction={{ xs: "column", md: "row" }} spacing={1} alignItems={{ xs: "stretch", md: "center" }}>
        <TextField
          label="Keyword (reason/requestedBy/currency/pair/accountNo)"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={onEnterSearch}
          fullWidth
        />
        <Stack direction="row" spacing={1}>
          {isAdmin && (
            <Button variant="contained" onClick={() => setCreateOpen(true)}>
              Create Request
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
        <Grid item xs={12} md={3}>
          <TextField select label="Status" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} fullWidth>
            <MenuItem value="">ALL</MenuItem>
            <MenuItem value="PENDING">PENDING</MenuItem>
            <MenuItem value="APPROVED">APPROVED</MenuItem>
            <MenuItem value="REJECTED">REJECTED</MenuItem>
            <MenuItem value="FAILED">FAILED</MenuItem>
          </TextField>
        </Grid>
        <Grid item xs={12} md={3}>
          <TextField select label="Priority" value={priorityFilter} onChange={(e) => setPriorityFilter(e.target.value)} fullWidth>
            <MenuItem value="">ALL</MenuItem>
            <MenuItem value="LOW">LOW</MenuItem>
            <MenuItem value="NORMAL">NORMAL</MenuItem>
            <MenuItem value="HIGH">HIGH</MenuItem>
            <MenuItem value="URGENT">URGENT</MenuItem>
          </TextField>
        </Grid>
        <Grid item xs={12} md={3}>
          <TextField
            label="Account ID"
            value={accountIdFilter}
            onChange={(e) => setAccountIdFilter(e.target.value)}
            onKeyDown={onEnterSearch}
            fullWidth
          />
        </Grid>
      </Grid>

      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>ID</TableCell>
            <TableCell>Type</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Account</TableCell>
            <TableCell>Amount</TableCell>
            <TableCell>Priority</TableCell>
            <TableCell>Value Date</TableCell>
            <TableCell>Rate</TableCell>
            <TableCell>Expected Receive</TableCell>
            <TableCell>Manual</TableCell>
            <TableCell>Policy</TableCell>
            <TableCell>Risk</TableCell>
            <TableCell>Requested By</TableCell>
            <TableCell>Reason</TableCell>
            <TableCell>Action</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((row) => (
            <TableRow key={row.id}>
              <TableCell>{row.id}</TableCell>
              <TableCell>{row.requestType}</TableCell>
              <TableCell>{row.status}</TableCell>
              <TableCell>
                {row.accountId} ({row.accountNo})
              </TableCell>
              <TableCell>
                {row.amount} {row.currency}
              </TableCell>
              <TableCell>{row.priority ?? "-"}</TableCell>
              <TableCell>{row.valueDate ?? "-"}</TableCell>
              <TableCell>{row.exchangeRate ?? "-"}</TableCell>
              <TableCell>{row.expectedToAmount ?? "-"}</TableCell>
              <TableCell>{row.manualReviewRequired ? "Y" : "N"}</TableCell>
              <TableCell>{row.controlPolicySource ?? "-"}</TableCell>
              <TableCell>
                {row.controlLimitPolicySource ?? "-"}
                {row.projectedDailyExposure !== undefined ? ` (${row.projectedDailyExposure})` : ""}
              </TableCell>
              <TableCell>{row.requestedBy}</TableCell>
              <TableCell>{row.reason}</TableCell>
              <TableCell>
                <Button size="small" onClick={() => navigate(`/fx-requests/${row.id}`)}>
                  Detail
                </Button>
                {isAdmin && row.status === "PENDING" && (
                  <>
                    <Button size="small" onClick={() => openDecisionModal(row.id, "approve")}>
                      Approve
                    </Button>
                    <Button size="small" color="error" onClick={() => openDecisionModal(row.id, "reject")}>
                      Reject
                    </Button>
                  </>
                )}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>

      <Dialog open={createOpen} onClose={() => setCreateOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>Create FX Request</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              label="Account ID"
              value={createAccountId}
              onChange={(e) => setCreateAccountId(e.target.value)}
              fullWidth
            />
            <TextField label="From Currency" value={fromCurrency} onChange={(e) => setFromCurrency(e.target.value)} fullWidth />
            <TextField label="To Currency" value={toCurrency} onChange={(e) => setToCurrency(e.target.value)} fullWidth />
            <TextField select label="Priority" value={createPriority} onChange={(e) => setCreatePriority(e.target.value)} fullWidth>
              <MenuItem value="LOW">LOW</MenuItem>
              <MenuItem value="NORMAL">NORMAL</MenuItem>
              <MenuItem value="HIGH">HIGH</MenuItem>
              <MenuItem value="URGENT">URGENT</MenuItem>
            </TextField>
            <TextField
              label="Value Date"
              type="date"
              value={createValueDate}
              onChange={(e) => setCreateValueDate(e.target.value)}
              InputLabelProps={{ shrink: true }}
              fullWidth
            />
            <TextField label="Amount" value={amount} onChange={(e) => setAmount(e.target.value)} fullWidth />
            <TextField label="Reason" value={reason} onChange={(e) => setReason(e.target.value)} fullWidth multiline minRows={2} />
            {quote && (
              <Card variant="outlined">
                <CardContent>
                  <Stack spacing={0.75}>
                    <Alert severity="info">
                      기준 환율 {quote.exchangeRate} / 예상 수취금액 {quote.convertedAmount} {quote.toCurrency}
                    </Alert>
                    <div>환율일자: {quote.rateDate}</div>
                    <div>소스: {quote.source} ({quote.quoteMode})</div>
                  </Stack>
                </CardContent>
              </Card>
            )}
            {quoteError && <Alert severity="warning">{quoteError}</Alert>}
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={() => createRequest().catch(console.error)}>
            Create
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog
        open={decision.open}
        onClose={() => setDecision((prev) => ({ ...prev, open: false }))}
        fullWidth
        maxWidth="sm"
      >
        <DialogTitle>{decision.action === "approve" ? "Approve Request" : "Reject Request"}</DialogTitle>
        <DialogContent>
          <TextField
            sx={{ mt: 1 }}
            label="Reason"
            value={decision.reason}
            onChange={(e) => setDecision((prev) => ({ ...prev, reason: e.target.value }))}
            fullWidth
            multiline
            minRows={3}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDecision((prev) => ({ ...prev, open: false }))}>Cancel</Button>
          <Button variant="contained" onClick={() => submitDecision().catch(console.error)}>
            Confirm
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  );
}
