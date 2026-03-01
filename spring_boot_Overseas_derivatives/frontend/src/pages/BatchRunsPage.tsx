import { useEffect, useMemo, useState } from "react";
import {
  Button,
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
import { api, getRole } from "../api/client";
import { PageTitle } from "../components/PageTitle";
import type { BatchRun, BatchSchedule, PagedResponse } from "../types/models";
import { pageDescriptions } from "../utils/pageDescriptions";
import { buildRsqlFilter, createEnterSearchHandler } from "../utils/listSearch";

function getNextDateIso(date: string): string {
  const [year, month, day] = date.split("-").map(Number);
  const d = new Date(Date.UTC(year, month - 1, day));
  d.setUTCDate(d.getUTCDate() + 1);
  return d.toISOString().slice(0, 10);
}

export function BatchRunsPage() {
  const isAdmin = getRole() === "OPS_ADMIN";

  const [rows, setRows] = useState<BatchRun[]>([]);
  const [schedules, setSchedules] = useState<BatchSchedule[]>([]);
  const [status, setStatus] = useState("");
  const [date, setDate] = useState("");
  const [keyword, setKeyword] = useState("");
  const [runNowBatchName, setRunNowBatchName] = useState("POSITION_SYNC");

  const rsqlFilter = useMemo(() => {
    const criteria = [] as Array<{ field: string; operator?: "==" | "=ge=" | "=lt="; value?: string | null }>;
    if (status) {
      criteria.push({ field: "status", operator: "==", value: status });
    }
    if (date) {
      criteria.push({ field: "startedAt", operator: "=ge=", value: `${date}T00:00:00Z` });
      criteria.push({ field: "startedAt", operator: "=lt=", value: `${getNextDateIso(date)}T00:00:00Z` });
    }
    return buildRsqlFilter(criteria);
  }, [status, date]);

  const loadRuns = async () => {
    const params = new URLSearchParams();
    params.set("size", "100");
    if (keyword.trim()) params.set("keyword", keyword.trim());
    if (rsqlFilter) params.set("filter", rsqlFilter);

    const res = await api.get<PagedResponse<BatchRun>>(`/batches/runs?${params.toString()}`);
    setRows(res.content);
  };

  const loadSchedules = async () => {
    const res = await api.get<BatchSchedule[]>("/batches/schedules");
    setSchedules(res);
  };

  const loadAll = async () => {
    await Promise.all([loadRuns(), loadSchedules()]);
  };

  const onEnterSearch = createEnterSearchHandler(() => {
    void loadRuns();
  });

  useEffect(() => {
    loadAll().catch(console.error);
  }, []);

  const reset = () => {
    setStatus("");
    setDate("");
    setKeyword("");
    void loadRuns();
  };

  const runNow = async () => {
    await api.post(`/batches/run-now?batchName=${encodeURIComponent(runNowBatchName)}`, {});
    await loadAll();
  };

  const pause = async (triggerName: string) => {
    await api.post(`/batches/schedules/${encodeURIComponent(triggerName)}/pause`, {});
    await loadSchedules();
  };

  const resume = async (triggerName: string) => {
    await api.post(`/batches/schedules/${encodeURIComponent(triggerName)}/resume`, {});
    await loadSchedules();
  };

  return (
    <Stack spacing={3}>
      <PageTitle title="Batch Runs" description={pageDescriptions.batchRuns} />

      <Stack direction={{ xs: "column", md: "row" }} spacing={1} alignItems={{ xs: "stretch", md: "center" }}>
        <TextField
          label="Keyword (batch/error)"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={onEnterSearch}
          fullWidth
        />
        <Stack direction="row" spacing={1}>
          <Button variant="contained" onClick={() => loadRuns().catch(console.error)}>
            Search
          </Button>
          <Button variant="outlined" onClick={reset}>
            Reset
          </Button>
        </Stack>
      </Stack>

      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <TextField select label="Status" value={status} onChange={(e) => setStatus(e.target.value)} fullWidth>
            <MenuItem value="">ALL</MenuItem>
            <MenuItem value="RUNNING">RUNNING</MenuItem>
            <MenuItem value="SUCCESS">SUCCESS</MenuItem>
            <MenuItem value="FAILED">FAILED</MenuItem>
          </TextField>
        </Grid>
        <Grid item xs={12} md={6}>
          <TextField
            type="date"
            label="Date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            fullWidth
            InputLabelProps={{ shrink: true }}
          />
        </Grid>
      </Grid>

      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>ID</TableCell>
            <TableCell>Batch</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Started</TableCell>
            <TableCell>Finished</TableCell>
            <TableCell>Retry</TableCell>
            <TableCell>Error</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((row) => (
            <TableRow key={row.id}>
              <TableCell>{row.id}</TableCell>
              <TableCell>{row.batchName}</TableCell>
              <TableCell>{row.status}</TableCell>
              <TableCell>{row.startedAt}</TableCell>
              <TableCell>{row.finishedAt ?? "-"}</TableCell>
              <TableCell>{row.retryCount}</TableCell>
              <TableCell>{row.errorMessage ?? "-"}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>

      <Stack direction={{ xs: "column", md: "row" }} spacing={1} alignItems={{ xs: "stretch", md: "center" }}>
        <Typography variant="h6" fontWeight={700} sx={{ flex: 1 }}>
          Quartz Schedules
        </Typography>
        {isAdmin && (
          <>
            <TextField
              select
              label="Run Now Batch"
              value={runNowBatchName}
              onChange={(e) => setRunNowBatchName(e.target.value)}
              sx={{ minWidth: 220 }}
            >
              <MenuItem value="POSITION_SYNC">POSITION_SYNC</MenuItem>
              <MenuItem value="MARGIN_RECALC">MARGIN_RECALC</MenuItem>
              <MenuItem value="EOD_SETTLEMENT">EOD_SETTLEMENT</MenuItem>
            </TextField>
            <Button variant="contained" onClick={() => runNow().catch(console.error)}>
              Run Now
            </Button>
          </>
        )}
        <Button variant="outlined" onClick={() => loadSchedules().catch(console.error)}>
          Refresh Schedules
        </Button>
      </Stack>

      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>Trigger</TableCell>
            <TableCell>Job</TableCell>
            <TableCell>Batch</TableCell>
            <TableCell>Cron</TableCell>
            <TableCell>Prev Fire</TableCell>
            <TableCell>Next Fire</TableCell>
            <TableCell>State</TableCell>
            <TableCell>Action</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {schedules.map((row) => (
            <TableRow key={row.triggerName}>
              <TableCell>{row.triggerName}</TableCell>
              <TableCell>{row.jobName}</TableCell>
              <TableCell>{row.batchName}</TableCell>
              <TableCell>{row.cronExpression ?? "-"}</TableCell>
              <TableCell>{row.previousFireTime ?? "-"}</TableCell>
              <TableCell>{row.nextFireTime ?? "-"}</TableCell>
              <TableCell>{row.triggerState}</TableCell>
              <TableCell>
                {isAdmin && (
                  <>
                    <Button size="small" onClick={() => pause(row.triggerName).catch(console.error)}>
                      Pause
                    </Button>
                    <Button size="small" onClick={() => resume(row.triggerName).catch(console.error)}>
                      Resume
                    </Button>
                  </>
                )}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Stack>
  );
}
