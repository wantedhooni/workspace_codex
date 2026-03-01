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
import { useNavigate } from "react-router-dom";
import { api, getRole } from "../api/client";
import { PageTitle } from "../components/PageTitle";
import type { OpsCase, OpsCaseCategory, OpsCaseSeverity, PagedResponse } from "../types/models";
import { pageDescriptions } from "../utils/pageDescriptions";
import { buildRsqlFilter, createEnterSearchHandler } from "../utils/listSearch";

type CaseForm = {
  id?: number;
  category: OpsCaseCategory;
  severity: OpsCaseSeverity;
  title: string;
  description: string;
  assignee: string;
  dueAt: string;
  linkedType: string;
  linkedId: string;
  accountId: string;
};

type TransitionState = {
  open: boolean;
  action: "start" | "resolve" | "close" | "reopen";
  caseId: number;
  comment: string;
};

function defaultForm(): CaseForm {
  const defaultDue = new Date(Date.now() + 4 * 60 * 60 * 1000).toISOString().slice(0, 16);
  return {
    category: "REQUEST_FAILURE",
    severity: "HIGH",
    title: "Broker submission failure follow-up",
    description: "Investigate failure cause, retry strategy, and broker desk confirmation.",
    assignee: "opsadmin",
    dueAt: defaultDue,
    linkedType: "",
    linkedId: "",
    accountId: "",
  };
}

export function OpsCasesPage() {
  const navigate = useNavigate();
  const isAdmin = getRole() === "OPS_ADMIN";

  const [rows, setRows] = useState<OpsCase[]>([]);
  const [keyword, setKeyword] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [severityFilter, setSeverityFilter] = useState("");
  const [assigneeFilter, setAssigneeFilter] = useState("");

  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<CaseForm>(defaultForm());

  const [transition, setTransition] = useState<TransitionState>({
    open: false,
    action: "start",
    caseId: 0,
    comment: "checked",
  });

  const rsqlFilter = useMemo(
    () =>
      buildRsqlFilter([
        { field: "status", operator: "==", value: statusFilter || null },
        { field: "severity", operator: "==", value: severityFilter || null },
        { field: "assignee", operator: "=like=", value: assigneeFilter.trim() || null },
      ]),
    [statusFilter, severityFilter, assigneeFilter],
  );

  const load = async () => {
    const params = new URLSearchParams();
    params.set("size", "100");
    if (keyword.trim()) params.set("keyword", keyword.trim());
    if (rsqlFilter) params.set("filter", rsqlFilter);
    const res = await api.get<PagedResponse<OpsCase>>(`/ops-cases?${params.toString()}`);
    setRows(res.content);
  };

  const onEnterSearch = createEnterSearchHandler(() => {
    void load();
  });

  const reset = async () => {
    setKeyword("");
    setStatusFilter("");
    setSeverityFilter("");
    setAssigneeFilter("");
    const res = await api.get<PagedResponse<OpsCase>>("/ops-cases?size=100");
    setRows(res.content);
  };

  useEffect(() => {
    load().catch(console.error);
  }, []);

  const openCreate = () => {
    setForm(defaultForm());
    setOpen(true);
  };

  const openEdit = (row: OpsCase) => {
    setForm({
      id: row.id,
      category: row.category,
      severity: row.severity,
      title: row.title,
      description: row.description,
      assignee: row.assignee ?? "",
      dueAt: row.dueAt ? row.dueAt.slice(0, 16) : "",
      linkedType: row.linkedType ?? "",
      linkedId: row.linkedId ?? "",
      accountId: row.accountId ? String(row.accountId) : "",
    });
    setOpen(true);
  };

  const submit = async () => {
    if (!form.title.trim() || !form.description.trim()) return;
    const payload = {
      category: form.category,
      severity: form.severity,
      title: form.title,
      description: form.description,
      assignee: form.assignee || null,
      dueAt: form.dueAt ? new Date(form.dueAt).toISOString() : null,
      linkedType: form.linkedType || null,
      linkedId: form.linkedId || null,
      accountId: form.accountId ? Number(form.accountId) : null,
    };

    if (form.id) {
      await api.put(`/ops-cases/${form.id}`, {
        category: payload.category,
        severity: payload.severity,
        title: payload.title,
        description: payload.description,
        assignee: payload.assignee,
        dueAt: payload.dueAt,
      });
    } else {
      await api.post("/ops-cases", payload);
    }
    setOpen(false);
    await load();
  };

  const openTransition = (caseId: number, action: "start" | "resolve" | "close" | "reopen") => {
    setTransition({
      open: true,
      action,
      caseId,
      comment: action === "resolve" ? "Root cause identified and mitigated" : "Validated and moved",
    });
  };

  const submitTransition = async () => {
    await api.post(`/ops-cases/${transition.caseId}/${transition.action}`, { comment: transition.comment });
    setTransition((prev) => ({ ...prev, open: false, comment: "" }));
    await load();
  };

  return (
    <Stack spacing={2}>
      <PageTitle title="Ops Cases" description={pageDescriptions.opsCases} />

      <Stack direction={{ xs: "column", md: "row" }} spacing={1} alignItems={{ xs: "stretch", md: "center" }}>
        <TextField
          label="Keyword (caseNo/title/linked/description)"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={onEnterSearch}
          fullWidth
        />
        <Stack direction="row" spacing={1}>
          {isAdmin && (
            <Button variant="contained" onClick={openCreate}>
              Create Case
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
          <TextField select label="Status" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} fullWidth>
            <MenuItem value="">ALL</MenuItem>
            <MenuItem value="OPEN">OPEN</MenuItem>
            <MenuItem value="IN_PROGRESS">IN_PROGRESS</MenuItem>
            <MenuItem value="RESOLVED">RESOLVED</MenuItem>
            <MenuItem value="CLOSED">CLOSED</MenuItem>
          </TextField>
        </Grid>
        <Grid item xs={12} md={4}>
          <TextField select label="Severity" value={severityFilter} onChange={(e) => setSeverityFilter(e.target.value)} fullWidth>
            <MenuItem value="">ALL</MenuItem>
            <MenuItem value="LOW">LOW</MenuItem>
            <MenuItem value="MEDIUM">MEDIUM</MenuItem>
            <MenuItem value="HIGH">HIGH</MenuItem>
            <MenuItem value="CRITICAL">CRITICAL</MenuItem>
          </TextField>
        </Grid>
        <Grid item xs={12} md={4}>
          <TextField
            label="Assignee"
            value={assigneeFilter}
            onChange={(e) => setAssigneeFilter(e.target.value)}
            onKeyDown={onEnterSearch}
            fullWidth
          />
        </Grid>
      </Grid>

      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>Case No</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Severity</TableCell>
            <TableCell>Category</TableCell>
            <TableCell>Title</TableCell>
            <TableCell>Assignee</TableCell>
            <TableCell>Linked</TableCell>
            <TableCell>Due</TableCell>
            <TableCell>Created</TableCell>
            <TableCell>Action</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((row) => (
            <TableRow key={row.id}>
              <TableCell>{row.caseNo}</TableCell>
              <TableCell>{row.status}</TableCell>
              <TableCell>{row.severity}</TableCell>
              <TableCell>{row.category}</TableCell>
              <TableCell>{row.title}</TableCell>
              <TableCell>{row.assignee ?? "-"}</TableCell>
              <TableCell>
                {row.linkedType ?? "-"}:{row.linkedId ?? "-"}
              </TableCell>
              <TableCell>{row.dueAt ?? "-"}</TableCell>
              <TableCell>{row.createdAt}</TableCell>
              <TableCell>
                <Button size="small" onClick={() => navigate(`/ops-cases/${row.id}`)}>
                  Detail
                </Button>
                {isAdmin && (
                  <>
                    <Button size="small" onClick={() => openEdit(row)}>
                      Edit
                    </Button>
                    {row.status === "OPEN" && (
                      <Button size="small" onClick={() => openTransition(row.id, "start")}>
                        Start
                      </Button>
                    )}
                    {(row.status === "OPEN" || row.status === "IN_PROGRESS") && (
                      <Button size="small" onClick={() => openTransition(row.id, "resolve")}>
                        Resolve
                      </Button>
                    )}
                    {row.status === "RESOLVED" && (
                      <Button size="small" onClick={() => openTransition(row.id, "close")}>
                        Close
                      </Button>
                    )}
                    {(row.status === "RESOLVED" || row.status === "CLOSED") && (
                      <Button size="small" onClick={() => openTransition(row.id, "reopen")}>
                        Reopen
                      </Button>
                    )}
                  </>
                )}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>

      <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{form.id ? "Edit Ops Case" : "Create Ops Case"}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              select
              label="Category"
              value={form.category}
              onChange={(e) => setForm((prev) => ({ ...prev, category: e.target.value as OpsCaseCategory }))}
              fullWidth
            >
              <MenuItem value="REQUEST_FAILURE">REQUEST_FAILURE</MenuItem>
              <MenuItem value="BATCH_FAILURE">BATCH_FAILURE</MenuItem>
              <MenuItem value="INTEGRATION">INTEGRATION</MenuItem>
              <MenuItem value="CONTROL">CONTROL</MenuItem>
              <MenuItem value="DATA_QUALITY">DATA_QUALITY</MenuItem>
              <MenuItem value="OTHER">OTHER</MenuItem>
            </TextField>
            <TextField
              select
              label="Severity"
              value={form.severity}
              onChange={(e) => setForm((prev) => ({ ...prev, severity: e.target.value as OpsCaseSeverity }))}
              fullWidth
            >
              <MenuItem value="LOW">LOW</MenuItem>
              <MenuItem value="MEDIUM">MEDIUM</MenuItem>
              <MenuItem value="HIGH">HIGH</MenuItem>
              <MenuItem value="CRITICAL">CRITICAL</MenuItem>
            </TextField>
            <TextField label="Title" value={form.title} onChange={(e) => setForm((prev) => ({ ...prev, title: e.target.value }))} fullWidth />
            <TextField
              label="Description"
              value={form.description}
              onChange={(e) => setForm((prev) => ({ ...prev, description: e.target.value }))}
              fullWidth
              multiline
              minRows={3}
            />
            <TextField label="Assignee" value={form.assignee} onChange={(e) => setForm((prev) => ({ ...prev, assignee: e.target.value }))} fullWidth />
            <TextField
              label="Due At"
              type="datetime-local"
              value={form.dueAt}
              onChange={(e) => setForm((prev) => ({ ...prev, dueAt: e.target.value }))}
              InputLabelProps={{ shrink: true }}
              fullWidth
            />
            {!form.id && (
              <>
                <TextField label="Linked Type" value={form.linkedType} onChange={(e) => setForm((prev) => ({ ...prev, linkedType: e.target.value }))} fullWidth />
                <TextField label="Linked ID" value={form.linkedId} onChange={(e) => setForm((prev) => ({ ...prev, linkedId: e.target.value }))} fullWidth />
                <TextField label="Account ID" value={form.accountId} onChange={(e) => setForm((prev) => ({ ...prev, accountId: e.target.value }))} fullWidth />
              </>
            )}
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={() => submit().catch(console.error)}>
            Save
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={transition.open} onClose={() => setTransition((prev) => ({ ...prev, open: false }))} fullWidth maxWidth="sm">
        <DialogTitle>{transition.action.toUpperCase()} Case</DialogTitle>
        <DialogContent>
          <TextField
            sx={{ mt: 1 }}
            label="Comment"
            value={transition.comment}
            onChange={(e) => setTransition((prev) => ({ ...prev, comment: e.target.value }))}
            multiline
            minRows={3}
            fullWidth
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setTransition((prev) => ({ ...prev, open: false }))}>Cancel</Button>
          <Button variant="contained" onClick={() => submitTransition().catch(console.error)}>
            Confirm
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  );
}
