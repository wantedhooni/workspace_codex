import { useEffect, useMemo, useState } from "react";
import { Button, Grid, Stack, Table, TableBody, TableCell, TableHead, TableRow, TextField } from "@mui/material";
import { api } from "../api/client";
import { PageTitle } from "../components/PageTitle";
import type { AuditLog, PagedResponse } from "../types/models";
import { pageDescriptions } from "../utils/pageDescriptions";
import { buildRsqlFilter, createEnterSearchHandler } from "../utils/listSearch";

export function AuditLogsPage() {
  const [rows, setRows] = useState<AuditLog[]>([]);
  const [actor, setActor] = useState("");
  const [action, setAction] = useState("");
  const [keyword, setKeyword] = useState("");

  const rsqlFilter = useMemo(
    () =>
      buildRsqlFilter([
        { field: "actor", operator: "==", value: actor.trim() || null },
        { field: "action", operator: "==", value: action.trim() || null },
      ]),
    [actor, action],
  );

  const load = async () => {
    const params = new URLSearchParams();
    params.set("size", "100");
    if (keyword.trim()) params.set("keyword", keyword.trim());
    if (rsqlFilter) params.set("filter", rsqlFilter);

    const res = await api.get<PagedResponse<AuditLog>>(`/audit-logs?${params.toString()}`);
    setRows(res.content);
  };

  const onEnterSearch = createEnterSearchHandler(() => {
    void load();
  });

  useEffect(() => {
    load().catch(console.error);
  }, []);

  return (
    <Stack spacing={2}>
      <PageTitle title="Audit Logs" description={pageDescriptions.auditLogs} />

      <Stack direction={{ xs: "column", md: "row" }} spacing={1} alignItems={{ xs: "stretch", md: "center" }}>
        <TextField label="Keyword" value={keyword} onChange={(e) => setKeyword(e.target.value)} onKeyDown={onEnterSearch} fullWidth />
        <Stack direction="row" spacing={1}>
          <Button variant="contained" onClick={() => load().catch(console.error)}>
            Search
          </Button>
          <Button
            variant="outlined"
            onClick={() => {
              setActor("");
              setAction("");
              setKeyword("");
              void api.get<PagedResponse<AuditLog>>("/audit-logs?size=100").then((res) => setRows(res.content));
            }}
          >
            Reset
          </Button>
        </Stack>
      </Stack>

      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <TextField label="Actor" value={actor} onChange={(e) => setActor(e.target.value)} onKeyDown={onEnterSearch} fullWidth />
        </Grid>
        <Grid item xs={12} md={6}>
          <TextField label="Action" value={action} onChange={(e) => setAction(e.target.value)} onKeyDown={onEnterSearch} fullWidth />
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
