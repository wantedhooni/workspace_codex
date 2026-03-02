import { useEffect, useMemo, useState } from "react";
import {
  Box,
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
} from "@mui/material";
import { api } from "../api/client";
import { PageTitle } from "../components/PageTitle";
import type { MenuItem as MenuModel, PagedResponse } from "../types/models";
import { pageDescriptions } from "../utils/pageDescriptions";
import { buildRsqlFilter, createEnterSearchHandler } from "../utils/listSearch";

export function MenusPage() {
  const [rows, setRows] = useState<MenuModel[]>([]);
  const [keyword, setKeyword] = useState("");
  const [enabledFilter, setEnabledFilter] = useState("");

  const rsqlFilter = useMemo(
    () =>
      buildRsqlFilter([
        { field: "enabled", operator: "==", value: enabledFilter === "" ? null : enabledFilter },
      ]),
    [enabledFilter],
  );

  const load = async () => {
    const params = new URLSearchParams();
    params.set("size", "100");
    if (keyword.trim()) params.set("keyword", keyword.trim());
    if (rsqlFilter) params.set("filter", rsqlFilter);
    const res = await api.get<PagedResponse<MenuModel>>(`/menus?${params.toString()}`);
    setRows(res.content);
  };

  const onEnterSearch = createEnterSearchHandler(() => {
    void load();
  });

  const orderedRows = useMemo(() => {
    const existingKeys = new Set(rows.map((row) => row.menuKey));
    const grouped = new Map<string, MenuModel[]>();

    rows.forEach((row) => {
      const parentKey = row.parentMenuKey && existingKeys.has(row.parentMenuKey) ? row.parentMenuKey : "__root__";
      const group = grouped.get(parentKey) ?? [];
      group.push(row);
      grouped.set(parentKey, group);
    });

    grouped.forEach((group) => {
      group.sort((a, b) => a.sortOrder - b.sortOrder || a.id - b.id);
    });

    const flattened: MenuModel[] = [];
    const visit = (parentKey: string) => {
      const group = grouped.get(parentKey) ?? [];
      group.forEach((row) => {
        flattened.push(row);
        visit(row.menuKey);
      });
    };

    visit("__root__");
    return flattened;
  }, [rows]);

  useEffect(() => {
    load().catch(console.error);
  }, []);

  return (
    <Stack spacing={2}>
      <PageTitle title="Menus" description={pageDescriptions.menus} />

      <Stack direction={{ xs: "column", md: "row" }} spacing={1} alignItems={{ xs: "stretch", md: "center" }}>
        <TextField
          label="Keyword (key/title/description/path/parent/role)"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onKeyDown={onEnterSearch}
          fullWidth
        />
        <Stack direction="row" spacing={1}>
          <Button variant="contained" onClick={() => load().catch(console.error)}>
            Search
          </Button>
          <Button
            variant="outlined"
            onClick={() => {
              setKeyword("");
              setEnabledFilter("");
              void api.get<PagedResponse<MenuModel>>("/menus?size=100").then((res) => setRows(res.content));
            }}
          >
            Reset
          </Button>
        </Stack>
      </Stack>

      <Grid container spacing={2}>
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
            <TableCell>Key</TableCell>
            <TableCell>Parent</TableCell>
            <TableCell>Depth</TableCell>
            <TableCell>Title</TableCell>
            <TableCell>Description</TableCell>
            <TableCell>Path</TableCell>
            <TableCell>Resource</TableCell>
            <TableCell>Order</TableCell>
            <TableCell>Enabled</TableCell>
            <TableCell>Roles</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {orderedRows.map((row) => (
            <TableRow key={row.id}>
              <TableCell>{row.id}</TableCell>
              <TableCell>{row.menuKey}</TableCell>
              <TableCell>{row.parentMenuKey ?? "-"}</TableCell>
              <TableCell>{row.depth}</TableCell>
              <TableCell>
                <Box sx={{ pl: row.depth * 2 }}>{row.title}</Box>
              </TableCell>
              <TableCell>{row.description ?? "-"}</TableCell>
              <TableCell>{row.path || "-"}</TableCell>
              <TableCell>{row.resourceName ?? "-"}</TableCell>
              <TableCell>{row.sortOrder}</TableCell>
              <TableCell>{row.enabled ? "Y" : "N"}</TableCell>
              <TableCell>{row.roles.join(", ")}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Stack>
  );
}
