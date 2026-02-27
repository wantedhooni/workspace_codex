import { useEffect, useState } from "react";
import { Button, Grid, Stack, Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography } from "@mui/material";
import { api } from "../api/client";
import type { AuditLog, PagedResponse } from "../types/models";

export function AuditLogsPage() {
  const [rows, setRows] = useState<AuditLog[]>([]);
  const [filter, setFilter] = useState("");
  const [actor, setActor] = useState("");
  const [action, setAction] = useState("");
  const [keyword, setKeyword] = useState("");

  const load = async () => {
    const params = new URLSearchParams();
    params.set("size", "100");
    if (actor) params.set("actor", actor);
    if (action) params.set("action", action);
    if (keyword.trim()) params.set("keyword", keyword.trim());
    if (filter.trim()) params.set("filter", filter.trim());

    const res = await api.get<PagedResponse<AuditLog>>(`/audit-logs?${params.toString()}`);
    setRows(res.content);
  };

  useEffect(() => {
    load().catch(console.error);
  }, []);

  return (
    <Stack spacing={2}>
      <Typography variant="h5" fontWeight={700}>
        Audit Logs
      </Typography>
      <Grid container spacing={2}>
        <Grid item xs={12} md={3}>
          <TextField label="Actor" value={actor} onChange={(e) => setActor(e.target.value)} fullWidth />
        </Grid>
        <Grid item xs={12} md={3}>
          <TextField label="Action" value={action} onChange={(e) => setAction(e.target.value)} fullWidth />
        </Grid>
        <Grid item xs={12} md={3}>
          <TextField label="Keyword" value={keyword} onChange={(e) => setKeyword(e.target.value)} fullWidth />
        </Grid>
        <Grid item xs={12} md={3}>
          <TextField
            label="RSQL Filter"
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
            placeholder={"action==LOGIN_SUCCESS;createdAt=ge=2026-01-01T00:00:00+09:00"}
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
            <TableCell>Actor</TableCell>
            <TableCell>Action</TableCell>
            <TableCell>Target</TableCell>
            <TableCell>Details</TableCell>
            <TableCell>Created At</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((row) => (
            <TableRow key={row.id}>
              <TableCell>{row.id}</TableCell>
              <TableCell>{row.actor}</TableCell>
              <TableCell>{row.action}</TableCell>
              <TableCell>
                {row.targetType}/{row.targetId}
              </TableCell>
              <TableCell>{row.details}</TableCell>
              <TableCell>{row.createdAt}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Stack>
  );
}
