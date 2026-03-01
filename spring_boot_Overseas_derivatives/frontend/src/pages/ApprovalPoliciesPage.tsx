import { useEffect, useMemo, useState } from "react";
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
import type { ApprovalDomain, ApprovalPolicy, PagedResponse } from "../types/models";
import { pageDescriptions } from "../utils/pageDescriptions";
import { buildRsqlFilter, createEnterSearchHandler } from "../utils/listSearch";

type PolicyForm = {
  id?: number;
  brokerCode: string;
  domain: ApprovalDomain;
  highThreshold: string;
  urgentThreshold: string;
  manualReviewThreshold: string;
  sameDayAutoReview: "true" | "false";
  enabled: "true" | "false";
  effectiveFrom: string;
  effectiveTo: string;
  description: string;
};

function defaultForm(): PolicyForm {
  return {
    brokerCode: "GLOBAL",
    domain: "FX",
    highThreshold: "250000",
    urgentThreshold: "1000000",
    manualReviewThreshold: "300000",
    sameDayAutoReview: "true",
    enabled: "true",
    effectiveFrom: new Date().toISOString().slice(0, 10),
    effectiveTo: "",
    description: "",
  };
}

export function ApprovalPoliciesPage() {
  const isAdmin = getRole() === "OPS_ADMIN";

  const [rows, setRows] = useState<ApprovalPolicy[]>([]);
  const [keyword, setKeyword] = useState("");
  const [brokerFilter, setBrokerFilter] = useState("");
  const [domainFilter, setDomainFilter] = useState("");
  const [enabledFilter, setEnabledFilter] = useState("");

  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<PolicyForm>(defaultForm());

  const rsqlFilter = useMemo(
    () =>
      buildRsqlFilter([
        { field: "brokerCode", operator: "==", value: brokerFilter.trim() || null },
        { field: "domain", operator: "==", value: domainFilter || null },
        { field: "enabled", operator: "==", value: enabledFilter === "" ? null : enabledFilter },
      ]),
    [brokerFilter, domainFilter, enabledFilter],
  );

  const load = async () => {
    const params = new URLSearchParams();
    params.set("size", "100");
    if (keyword.trim()) params.set("keyword", keyword.trim());
    if (rsqlFilter) params.set("filter", rsqlFilter);
    const res = await api.get<PagedResponse<ApprovalPolicy>>(`/approval-policies?${params.toString()}`);
    setRows(res.content);
  };

  const onEnterSearch = createEnterSearchHandler(() => {
    void load();
  });

  const reset = async () => {
    setKeyword("");
    setBrokerFilter("");
    setDomainFilter("");
    setEnabledFilter("");
    const res = await api.get<PagedResponse<ApprovalPolicy>>("/approval-policies?size=100");
    setRows(res.content);
  };

  useEffect(() => {
    load().catch(console.error);
  }, []);

  const openCreate = () => {
    setForm(defaultForm());
    setOpen(true);
  };

  const openEdit = (row: ApprovalPolicy) => {
    setForm({
      id: row.id,
      brokerCode: row.brokerCode,
      domain: row.domain,
      highThreshold: String(row.highThreshold),
      urgentThreshold: String(row.urgentThreshold),
      manualReviewThreshold: String(row.manualReviewThreshold),
      sameDayAutoReview: String(row.sameDayAutoReview) as "true" | "false",
      enabled: String(row.enabled) as "true" | "false",
      effectiveFrom: row.effectiveFrom,
      effectiveTo: row.effectiveTo ?? "",
      description: row.description ?? "",
    });
    setOpen(true);
  };

  const submit = async () => {
    const payload = {
      brokerCode: form.brokerCode,
      domain: form.domain,
      highThreshold: Number(form.highThreshold),
      urgentThreshold: Number(form.urgentThreshold),
      manualReviewThreshold: Number(form.manualReviewThreshold),
      sameDayAutoReview: form.sameDayAutoReview === "true",
      enabled: form.enabled === "true",
      effectiveFrom: form.effectiveFrom,
      effectiveTo: form.effectiveTo || null,
      description: form.description || null,
    };

    if (form.id) {
      await api.put(`/approval-policies/${form.id}`, payload);
    } else {
      await api.post("/approval-policies", payload);
    }
    setOpen(false);
    await load();
  };

  return (
    <Stack spacing={2}>
      <PageTitle title="Approval Policies" description={pageDescriptions.approvalPolicies} />

      <Stack direction={{ xs: "column", md: "row" }} spacing={1} alignItems={{ xs: "stretch", md: "center" }}>
        <TextField
          label="Keyword (broker/domain/description)"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={onEnterSearch}
          fullWidth
        />
        <Stack direction="row" spacing={1}>
          {isAdmin && (
            <Button variant="contained" onClick={openCreate}>
              Create Policy
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
        <Grid item xs={12} md={4}>
          <TextField
            label="Broker"
            value={brokerFilter}
            onChange={(e) => setBrokerFilter(e.target.value)}
            onKeyDown={onEnterSearch}
            fullWidth
          />
        </Grid>
        <Grid item xs={12} md={4}>
          <TextField select label="Domain" value={domainFilter} onChange={(e) => setDomainFilter(e.target.value)} fullWidth>
            <MenuItem value="">ALL</MenuItem>
            <MenuItem value="CASH_DEPOSIT">CASH_DEPOSIT</MenuItem>
            <MenuItem value="CASH_WITHDRAW">CASH_WITHDRAW</MenuItem>
            <MenuItem value="FX">FX</MenuItem>
          </TextField>
        </Grid>
        <Grid item xs={12} md={4}>
          <TextField select label="Enabled" value={enabledFilter} onChange={(e) => setEnabledFilter(e.target.value)} fullWidth>
            <MenuItem value="">ALL</MenuItem>
            <MenuItem value="true">TRUE</MenuItem>
            <MenuItem value="false">FALSE</MenuItem>
          </TextField>
        </Grid>
      </Grid>

      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>ID</TableCell>
            <TableCell>Broker</TableCell>
            <TableCell>Domain</TableCell>
            <TableCell>High</TableCell>
            <TableCell>Urgent</TableCell>
            <TableCell>Manual Review</TableCell>
            <TableCell>Same-day Auto</TableCell>
            <TableCell>Enabled</TableCell>
            <TableCell>Effective</TableCell>
            <TableCell>Description</TableCell>
            <TableCell>Action</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((row) => (
            <TableRow key={row.id}>
              <TableCell>{row.id}</TableCell>
              <TableCell>{row.brokerCode}</TableCell>
              <TableCell>{row.domain}</TableCell>
              <TableCell>{row.highThreshold}</TableCell>
              <TableCell>{row.urgentThreshold}</TableCell>
              <TableCell>{row.manualReviewThreshold}</TableCell>
              <TableCell>{row.sameDayAutoReview ? "Y" : "N"}</TableCell>
              <TableCell>{row.enabled ? "Y" : "N"}</TableCell>
              <TableCell>
                {row.effectiveFrom} ~ {row.effectiveTo ?? "-"}
              </TableCell>
              <TableCell>{row.description ?? "-"}</TableCell>
              <TableCell>
                {isAdmin && (
                  <Button size="small" onClick={() => openEdit(row)}>
                    Edit
                  </Button>
                )}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>

      <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{form.id ? "Edit Approval Policy" : "Create Approval Policy"}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Broker Code" value={form.brokerCode} onChange={(e) => setForm((prev) => ({ ...prev, brokerCode: e.target.value }))} fullWidth />
            <TextField
              select
              label="Domain"
              value={form.domain}
              onChange={(e) => setForm((prev) => ({ ...prev, domain: e.target.value as ApprovalDomain }))}
              fullWidth
            >
              <MenuItem value="CASH_DEPOSIT">CASH_DEPOSIT</MenuItem>
              <MenuItem value="CASH_WITHDRAW">CASH_WITHDRAW</MenuItem>
              <MenuItem value="FX">FX</MenuItem>
            </TextField>
            <TextField label="High Threshold" value={form.highThreshold} onChange={(e) => setForm((prev) => ({ ...prev, highThreshold: e.target.value }))} fullWidth />
            <TextField label="Urgent Threshold" value={form.urgentThreshold} onChange={(e) => setForm((prev) => ({ ...prev, urgentThreshold: e.target.value }))} fullWidth />
            <TextField
              label="Manual Review Threshold"
              value={form.manualReviewThreshold}
              onChange={(e) => setForm((prev) => ({ ...prev, manualReviewThreshold: e.target.value }))}
              fullWidth
            />
            <TextField
              select
              label="Same-day Auto Review"
              value={form.sameDayAutoReview}
              onChange={(e) => setForm((prev) => ({ ...prev, sameDayAutoReview: e.target.value as "true" | "false" }))}
              fullWidth
            >
              <MenuItem value="true">TRUE</MenuItem>
              <MenuItem value="false">FALSE</MenuItem>
            </TextField>
            <TextField
              select
              label="Enabled"
              value={form.enabled}
              onChange={(e) => setForm((prev) => ({ ...prev, enabled: e.target.value as "true" | "false" }))}
              fullWidth
            >
              <MenuItem value="true">TRUE</MenuItem>
              <MenuItem value="false">FALSE</MenuItem>
            </TextField>
            <TextField
              type="date"
              label="Effective From"
              value={form.effectiveFrom}
              onChange={(e) => setForm((prev) => ({ ...prev, effectiveFrom: e.target.value }))}
              InputLabelProps={{ shrink: true }}
              fullWidth
            />
            <TextField
              type="date"
              label="Effective To"
              value={form.effectiveTo}
              onChange={(e) => setForm((prev) => ({ ...prev, effectiveTo: e.target.value }))}
              InputLabelProps={{ shrink: true }}
              fullWidth
            />
            <TextField
              label="Description"
              value={form.description}
              onChange={(e) => setForm((prev) => ({ ...prev, description: e.target.value }))}
              fullWidth
              multiline
              minRows={2}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={() => submit().catch(console.error)}>
            Save
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  );
}
