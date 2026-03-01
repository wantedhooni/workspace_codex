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
import type { ApprovalDomain, PagedResponse, RiskLimitPolicy } from "../types/models";
import { pageDescriptions } from "../utils/pageDescriptions";
import { buildRsqlFilter, createEnterSearchHandler } from "../utils/listSearch";

type RiskLimitForm = {
  id?: number;
  brokerCode: string;
  domain: ApprovalDomain;
  currencyCode: string;
  maxPerRequest: string;
  dailySoftLimit: string;
  dailyHardLimit: string;
  enabled: "true" | "false";
  effectiveFrom: string;
  effectiveTo: string;
  description: string;
};

function defaultForm(): RiskLimitForm {
  return {
    brokerCode: "GLOBAL",
    domain: "FX",
    currencyCode: "",
    maxPerRequest: "1500000",
    dailySoftLimit: "3000000",
    dailyHardLimit: "5000000",
    enabled: "true",
    effectiveFrom: new Date().toISOString().slice(0, 10),
    effectiveTo: "",
    description: "",
  };
}

export function RiskLimitsPage() {
  const isAdmin = getRole() === "OPS_ADMIN";

  const [rows, setRows] = useState<RiskLimitPolicy[]>([]);
  const [keyword, setKeyword] = useState("");
  const [brokerFilter, setBrokerFilter] = useState("");
  const [domainFilter, setDomainFilter] = useState("");
  const [currencyFilter, setCurrencyFilter] = useState("");
  const [enabledFilter, setEnabledFilter] = useState("");

  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<RiskLimitForm>(defaultForm());

  const rsqlFilter = useMemo(
    () =>
      buildRsqlFilter([
        { field: "brokerCode", operator: "==", value: brokerFilter.trim() || null },
        { field: "domain", operator: "==", value: domainFilter || null },
        { field: "currencyCode", operator: "==", value: currencyFilter.trim() || null },
        { field: "enabled", operator: "==", value: enabledFilter === "" ? null : enabledFilter },
      ]),
    [brokerFilter, domainFilter, currencyFilter, enabledFilter],
  );

  const load = async () => {
    const params = new URLSearchParams();
    params.set("size", "100");
    if (keyword.trim()) params.set("keyword", keyword.trim());
    if (rsqlFilter) params.set("filter", rsqlFilter);
    const res = await api.get<PagedResponse<RiskLimitPolicy>>(`/risk-limits?${params.toString()}`);
    setRows(res.content);
  };

  const onEnterSearch = createEnterSearchHandler(() => {
    void load();
  });

  const reset = async () => {
    setKeyword("");
    setBrokerFilter("");
    setDomainFilter("");
    setCurrencyFilter("");
    setEnabledFilter("");
    const res = await api.get<PagedResponse<RiskLimitPolicy>>("/risk-limits?size=100");
    setRows(res.content);
  };

  useEffect(() => {
    load().catch(console.error);
  }, []);

  const openCreate = () => {
    setForm(defaultForm());
    setOpen(true);
  };

  const openEdit = (row: RiskLimitPolicy) => {
    setForm({
      id: row.id,
      brokerCode: row.brokerCode,
      domain: row.domain,
      currencyCode: row.currencyCode ?? "",
      maxPerRequest: String(row.maxPerRequest),
      dailySoftLimit: String(row.dailySoftLimit),
      dailyHardLimit: String(row.dailyHardLimit),
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
      currencyCode: form.currencyCode || null,
      maxPerRequest: Number(form.maxPerRequest),
      dailySoftLimit: Number(form.dailySoftLimit),
      dailyHardLimit: Number(form.dailyHardLimit),
      enabled: form.enabled === "true",
      effectiveFrom: form.effectiveFrom,
      effectiveTo: form.effectiveTo || null,
      description: form.description || null,
    };

    if (form.id) {
      await api.put(`/risk-limits/${form.id}`, payload);
    } else {
      await api.post("/risk-limits", payload);
    }
    setOpen(false);
    await load();
  };

  return (
    <Stack spacing={2}>
      <PageTitle title="Risk Limits" description={pageDescriptions.riskLimits} />

      <Stack direction={{ xs: "column", md: "row" }} spacing={1} alignItems={{ xs: "stretch", md: "center" }}>
        <TextField
          label="Keyword (broker/domain/currency/description)"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={onEnterSearch}
          fullWidth
        />
        <Stack direction="row" spacing={1}>
          {isAdmin && (
            <Button variant="contained" onClick={openCreate}>
              Create Limit
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
          <TextField
            label="Broker"
            value={brokerFilter}
            onChange={(e) => setBrokerFilter(e.target.value)}
            onKeyDown={onEnterSearch}
            fullWidth
          />
        </Grid>
        <Grid item xs={12} md={3}>
          <TextField select label="Domain" value={domainFilter} onChange={(e) => setDomainFilter(e.target.value)} fullWidth>
            <MenuItem value="">ALL</MenuItem>
            <MenuItem value="CASH_DEPOSIT">CASH_DEPOSIT</MenuItem>
            <MenuItem value="CASH_WITHDRAW">CASH_WITHDRAW</MenuItem>
            <MenuItem value="FX">FX</MenuItem>
          </TextField>
        </Grid>
        <Grid item xs={12} md={3}>
          <TextField
            label="Currency"
            value={currencyFilter}
            onChange={(e) => setCurrencyFilter(e.target.value)}
            onKeyDown={onEnterSearch}
            fullWidth
          />
        </Grid>
        <Grid item xs={12} md={3}>
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
            <TableCell>Currency</TableCell>
            <TableCell>Max/Req</TableCell>
            <TableCell>Daily Soft</TableCell>
            <TableCell>Daily Hard</TableCell>
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
              <TableCell>{row.currencyCode ?? "*"}</TableCell>
              <TableCell>{row.maxPerRequest}</TableCell>
              <TableCell>{row.dailySoftLimit}</TableCell>
              <TableCell>{row.dailyHardLimit}</TableCell>
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
        <DialogTitle>{form.id ? "Edit Risk Limit" : "Create Risk Limit"}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              label="Broker Code"
              value={form.brokerCode}
              onChange={(e) => setForm((prev) => ({ ...prev, brokerCode: e.target.value }))}
              fullWidth
            />
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
            <TextField
              label="Currency (blank = all)"
              value={form.currencyCode}
              onChange={(e) => setForm((prev) => ({ ...prev, currencyCode: e.target.value }))}
              fullWidth
            />
            <TextField
              label="Max Per Request"
              value={form.maxPerRequest}
              onChange={(e) => setForm((prev) => ({ ...prev, maxPerRequest: e.target.value }))}
              fullWidth
            />
            <TextField
              label="Daily Soft Limit"
              value={form.dailySoftLimit}
              onChange={(e) => setForm((prev) => ({ ...prev, dailySoftLimit: e.target.value }))}
              fullWidth
            />
            <TextField
              label="Daily Hard Limit"
              value={form.dailyHardLimit}
              onChange={(e) => setForm((prev) => ({ ...prev, dailyHardLimit: e.target.value }))}
              fullWidth
            />
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
