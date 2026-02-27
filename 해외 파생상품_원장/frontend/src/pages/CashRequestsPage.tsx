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
  Typography,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import { api, getRole } from "../api/client";
import type { PagedResponse, RequestRow } from "../types/models";

type DecisionState = {
  open: boolean;
  requestId: string;
  action: "approve" | "reject";
  reason: string;
};

export function CashRequestsPage() {
  const navigate = useNavigate();
  const isAdmin = getRole() === "OPS_ADMIN";

  const [rows, setRows] = useState<RequestRow[]>([]);
  const [keyword, setKeyword] = useState("");
  const [filter, setFilter] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [accountIdFilter, setAccountIdFilter] = useState("");

  const [createOpen, setCreateOpen] = useState(false);
  const [createAccountId, setCreateAccountId] = useState("1");
  const [type, setType] = useState("DEPOSIT");
  const [amount, setAmount] = useState("10000");
  const [currency, setCurrency] = useState("USD");
  const [reason, setReason] = useState("Daily cash adjustment");

  const [decision, setDecision] = useState<DecisionState>({
    open: false,
    requestId: "",
    action: "approve",
    reason: "",
  });

  const load = async () => {
    const params = new URLSearchParams();
    params.set("size", "100");
    if (keyword.trim()) params.set("keyword", keyword.trim());
    if (filter.trim()) params.set("filter", filter.trim());
    if (statusFilter) params.set("status", statusFilter);
    if (accountIdFilter.trim()) params.set("accountId", accountIdFilter.trim());

    const res = await api.get<PagedResponse<RequestRow>>(`/cash-requests?${params.toString()}`);
    setRows(res.content);
  };

  const reset = async () => {
    setKeyword("");
    setFilter("");
    setStatusFilter("");
    setAccountIdFilter("");
    const res = await api.get<PagedResponse<RequestRow>>("/cash-requests?size=100");
    setRows(res.content);
  };

  useEffect(() => {
    load().catch(console.error);
  }, []);

  const createRequest = async () => {
    await api.post("/cash-requests", {
      accountId: Number(createAccountId),
      type,
      amount: Number(amount),
      currency,
      reason,
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
      <Stack direction="row" justifyContent="space-between" alignItems="center">
        <Typography variant="h5" fontWeight={700}>
          Cash Requests
        </Typography>
        {isAdmin && (
          <Button variant="contained" onClick={() => setCreateOpen(true)}>
            Create Request
          </Button>
        )}
      </Stack>

      <Grid container spacing={2}>
        <Grid item xs={12} md={4}>
          <TextField
            label="Keyword (reason/requestedBy/accountNo)"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            fullWidth
          />
        </Grid>
        <Grid item xs={12} md={4}>
          <TextField
            label="RSQL Filter"
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
            placeholder={"status==PENDING;amount=ge=10000"}
            fullWidth
          />
        </Grid>
        <Grid item xs={12} md={2}>
          <TextField select label="Status" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} fullWidth>
            <MenuItem value="">ALL</MenuItem>
            <MenuItem value="PENDING">PENDING</MenuItem>
            <MenuItem value="APPROVED">APPROVED</MenuItem>
            <MenuItem value="REJECTED">REJECTED</MenuItem>
            <MenuItem value="FAILED">FAILED</MenuItem>
          </TextField>
        </Grid>
        <Grid item xs={12} md={2}>
          <TextField
            label="Account ID"
            value={accountIdFilter}
            onChange={(e) => setAccountIdFilter(e.target.value)}
            fullWidth
          />
        </Grid>
        <Grid item xs={12} md={12}>
          <Stack direction="row" spacing={1}>
            <Button variant="contained" onClick={() => load().catch(console.error)}>
              Search
            </Button>
            <Button variant="outlined" onClick={() => reset().catch(console.error)}>
              Reset
            </Button>
          </Stack>
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
              <TableCell>{row.accountId}</TableCell>
              <TableCell>{row.amount}</TableCell>
              <TableCell>{row.requestedBy}</TableCell>
              <TableCell>{row.reason}</TableCell>
              <TableCell>
                <Button size="small" onClick={() => navigate(`/cash-requests/${row.id}`)}>
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
        <DialogTitle>Create Cash Request</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              label="Account ID"
              value={createAccountId}
              onChange={(e) => setCreateAccountId(e.target.value)}
              fullWidth
            />
            <TextField select label="Type" value={type} onChange={(e) => setType(e.target.value)} fullWidth>
              <MenuItem value="DEPOSIT">DEPOSIT</MenuItem>
              <MenuItem value="WITHDRAW">WITHDRAW</MenuItem>
            </TextField>
            <TextField label="Amount" value={amount} onChange={(e) => setAmount(e.target.value)} fullWidth />
            <TextField label="Currency" value={currency} onChange={(e) => setCurrency(e.target.value)} fullWidth />
            <TextField label="Reason" value={reason} onChange={(e) => setReason(e.target.value)} fullWidth multiline minRows={2} />
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
