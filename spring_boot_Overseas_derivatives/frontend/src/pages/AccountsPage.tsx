import { useEffect, useMemo, useState } from "react";
import {
  Box,
  Button,
  FormControl,
  Grid,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  Switch,
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
import { PageTitle } from "../components/PageTitle";
import type { Account, PagedResponse } from "../types/models";
import { pageDescriptions } from "../utils/pageDescriptions";
import { buildRsqlFilter, createEnterSearchHandler } from "../utils/listSearch";

export function AccountsPage() {
  const navigate = useNavigate();
  const role = getRole();
  const isAdmin = role === "OPS_ADMIN";

  const [items, setItems] = useState<Account[]>([]);
  const [keyword, setKeyword] = useState("");
  const [broker, setBroker] = useState("");
  const [status, setStatus] = useState("");
  const [unmask, setUnmask] = useState(false);

  const rsqlFilter = useMemo(
    () =>
      buildRsqlFilter([
        { field: "broker", operator: "==", value: broker.trim() || null },
        { field: "status", operator: "==", value: status || null },
      ]),
    [broker, status],
  );

  const load = async () => {
    const params = new URLSearchParams();
    params.set("size", "100");
    if (keyword.trim()) params.set("keyword", keyword.trim());
    if (rsqlFilter) params.set("filter", rsqlFilter);
    if (unmask) params.set("unmask", "true");

    const res = await api.get<PagedResponse<Account>>(`/accounts?${params.toString()}`);
    setItems(res.content);
  };

  const onEnterSearch = createEnterSearchHandler(() => {
    void load();
  });

  const reset = async () => {
    setKeyword("");
    setBroker("");
    setStatus("");

    const params = new URLSearchParams();
    params.set("size", "100");
    if (unmask) params.set("unmask", "true");
    const res = await api.get<PagedResponse<Account>>(`/accounts?${params.toString()}`);
    setItems(res.content);
  };

  useEffect(() => {
    load().catch(console.error);
  }, []);

  return (
    <Stack spacing={2}>
      <PageTitle title="Accounts" description={pageDescriptions.accounts} />

      <Stack direction={{ xs: "column", md: "row" }} spacing={1} alignItems={{ xs: "stretch", md: "center" }}>
        <TextField
          label="Keyword (account/broker/owner)"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={onEnterSearch}
          fullWidth
        />
        <Stack direction="row" spacing={1}>
          <Button variant="contained" onClick={() => load().catch(console.error)}>
            Search
          </Button>
          <Button variant="outlined" onClick={() => reset().catch(console.error)}>
            Reset
          </Button>
        </Stack>
      </Stack>

      <Grid container spacing={2} alignItems="center">
        <Grid item xs={12} md={4}>
          <TextField
            label="Broker"
            value={broker}
            onChange={(e) => setBroker(e.target.value)}
            onKeyDown={onEnterSearch}
            fullWidth
          />
        </Grid>
        <Grid item xs={12} md={4}>
          <FormControl fullWidth>
            <InputLabel>Status</InputLabel>
            <Select label="Status" value={status} onChange={(e) => setStatus(e.target.value)}>
              <MenuItem value="">ALL</MenuItem>
              <MenuItem value="ACTIVE">ACTIVE</MenuItem>
              <MenuItem value="PENDING_OPEN">PENDING_OPEN</MenuItem>
              <MenuItem value="PENDING_CLOSE">PENDING_CLOSE</MenuItem>
              <MenuItem value="SUSPENDED">SUSPENDED</MenuItem>
              <MenuItem value="CLOSED">CLOSED</MenuItem>
            </Select>
          </FormControl>
        </Grid>
        <Grid item xs={12} md={4}>
          <Box>
            <Typography variant="body2">계좌번호 마스킹 해제(관리자)</Typography>
            <Switch checked={unmask} onChange={(e) => setUnmask(e.target.checked)} disabled={!isAdmin} />
          </Box>
        </Grid>
      </Grid>

      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>ID</TableCell>
            <TableCell>Account No</TableCell>
            <TableCell>Broker</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Owner</TableCell>
            <TableCell />
          </TableRow>
        </TableHead>
        <TableBody>
          {items.map((item) => (
            <TableRow key={item.id} hover>
              <TableCell>{item.id}</TableCell>
              <TableCell>{item.accountNo}</TableCell>
              <TableCell>{item.broker}</TableCell>
              <TableCell>{item.status}</TableCell>
              <TableCell>{item.ownerName}</TableCell>
              <TableCell>
                <Button size="small" onClick={() => navigate(`/accounts/${item.id}${unmask ? "?unmask=true" : ""}`)}>
                  Summary
                </Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Stack>
  );
}
