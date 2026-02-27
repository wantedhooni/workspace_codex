import { useEffect, useState } from "react";
import { Button, Grid, MenuItem, Stack, Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography } from "@mui/material";
import { api } from "../api/client";
import type { BatchRun, PagedResponse } from "../types/models";

export function BatchRunsPage() {
  const [rows, setRows] = useState<BatchRun[]>([]);
  const [filter, setFilter] = useState("");
  const [status, setStatus] = useState("");
  const [date, setDate] = useState("");
  const [keyword, setKeyword] = useState("");

  const load = async () => {
    const params = new URLSearchParams();
    params.set("size", "100");
    if (status) params.set("status", status);
    if (date) params.set("date", date);
    if (keyword.trim()) params.set("keyword", keyword.trim());
    if (filter.trim()) params.set("filter", filter.trim());

    const res = await api.get<PagedResponse<BatchRun>>(`/batches/runs?${params.toString()}`);
    setRows(res.content);
  };

  useEffect(() => {
    load().catch(console.error);
  }, []);

  return (
    <Stack spacing={2}>
      <Typography variant="h5" fontWeight={700}>
        Batch Runs
      </Typography>

      <Grid container spacing={2}>
        <Grid item xs={12} md={3}>
          <TextField select label="Status" value={status} onChange={(e) => setStatus(e.target.value)} fullWidth>
            <MenuItem value="">ALL</MenuItem>
            <MenuItem value="RUNNING">RUNNING</MenuItem>
            <MenuItem value="SUCCESS">SUCCESS</MenuItem>
            <MenuItem value="FAILED">FAILED</MenuItem>
          </TextField>
        </Grid>
        <Grid item xs={12} md={3}>
          <TextField type="date" label="Date" value={date} onChange={(e) => setDate(e.target.value)} fullWidth InputLabelProps={{ shrink: true }} />
        </Grid>
        <Grid item xs={12} md={3}>
          <TextField label="Keyword (batch/error)" value={keyword} onChange={(e) => setKeyword(e.target.value)} fullWidth />
        </Grid>
        <Grid item xs={12} md={3}>
          <TextField
            label="RSQL Filter"
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
            placeholder={"status==FAILED;retryCount=ge=1"}
            fullWidth
          />
        </Grid>
        <Grid item xs={12} md={3}>
          <Button variant="outlined" onClick={() => load().catch(console.error)}>
            Search
          </Button>
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
    </Stack>
  );
}
