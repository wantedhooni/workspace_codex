import { useEffect, useState } from "react";
import {
  Alert,
  Box,
  Card,
  CardContent,
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
  Typography,
} from "@mui/material";
import { api, getRole } from "../api/client";
import { PageTitle } from "../components/PageTitle";
import type { Account, PagedResponse, PortfolioOverview } from "../types/models";
import { pageDescriptions } from "../utils/pageDescriptions";

export function PortfolioPage() {
  const isAdmin = getRole() === "OPS_ADMIN";
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [selectedAccountId, setSelectedAccountId] = useState("");
  const [unmask, setUnmask] = useState(false);
  const [overview, setOverview] = useState<PortfolioOverview | null>(null);
  const [error, setError] = useState<string | null>(null);

  const loadAccounts = async () => {
    const params = new URLSearchParams();
    params.set("size", "100");
    if (isAdmin && unmask) {
      params.set("unmask", "true");
    }
    const res = await api.get<PagedResponse<Account>>(`/accounts?${params.toString()}`);
    setAccounts(res.content);
    if (!selectedAccountId && res.content.length > 0) {
      setSelectedAccountId(String(res.content[0].id));
    }
  };

  const loadOverview = async (accountId: string) => {
    if (!accountId) return;
    const params = new URLSearchParams();
    if (isAdmin && unmask) {
      params.set("unmask", "true");
    }
    const suffix = params.toString() ? `?${params.toString()}` : "";
    const res = await api.get<PortfolioOverview>(`/portfolios/${accountId}${suffix}`);
    setOverview(res);
    setError(null);
  };

  useEffect(() => {
    loadAccounts().catch((err: Error) => setError(err.message));
  }, [isAdmin, unmask]);

  useEffect(() => {
    loadOverview(selectedAccountId).catch((err: Error) => setError(err.message));
  }, [selectedAccountId, isAdmin, unmask]);

  return (
    <Stack spacing={2}>
      <PageTitle title="Portfolio" description={pageDescriptions.portfolio} />

      <Grid container spacing={2} alignItems="center">
        <Grid item xs={12} md={8}>
          <FormControl fullWidth>
            <InputLabel>Account</InputLabel>
            <Select label="Account" value={selectedAccountId} onChange={(e) => setSelectedAccountId(e.target.value)}>
              {accounts.map((item) => (
                <MenuItem key={item.id} value={String(item.id)}>
                  {item.accountNo} / {item.broker} / {item.ownerName}
                </MenuItem>
              ))}
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

      {error && <Alert severity="warning">{error}</Alert>}

      {overview && (
        <>
          <Grid container spacing={2}>
            <Grid item xs={12} md={4}>
              <Card>
                <CardContent>
                  <Typography variant="overline" color="text.secondary">
                    계좌
                  </Typography>
                  <Typography variant="h6" fontWeight={700}>
                    {overview.accountNo}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {overview.broker} / {overview.ownerName} / {overview.status}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} md={4}>
              <Card>
                <CardContent>
                  <Typography variant="overline" color="text.secondary">
                    보유 종목 수
                  </Typography>
                  <Typography variant="h4" fontWeight={700}>
                    {overview.holdingCount}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} md={4}>
              <Card>
                <CardContent>
                  <Typography variant="overline" color="text.secondary">
                    최근 매수 건수
                  </Typography>
                  <Typography variant="h4" fontWeight={700}>
                    {overview.recentPurchaseCount}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          </Grid>

          <Grid container spacing={2}>
            <Grid item xs={12} lg={5}>
              <Card sx={{ height: "100%" }}>
                <CardContent>
                  <Typography variant="subtitle1" fontWeight={700}>
                    현금 잔고
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5 }}>
                    스냅샷 기준일: {overview.cashSnapshotDate}
                  </Typography>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Currency</TableCell>
                        <TableCell>Amount</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {overview.cashBalances.map((item) => (
                        <TableRow key={item.currency}>
                          <TableCell>{item.currency}</TableCell>
                          <TableCell>{item.amount}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} lg={7}>
              <Card sx={{ height: "100%" }}>
                <CardContent>
                  <Typography variant="subtitle1" fontWeight={700}>
                    통화별 주식 장부원가
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5 }}>
                    다통화 계좌를 고려해 원가를 통화별로 분리합니다.
                  </Typography>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Currency</TableCell>
                        <TableCell>Total Cost</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {overview.stockCostByCurrency.map((item) => (
                        <TableRow key={item.currency}>
                          <TableCell>{item.currency}</TableCell>
                          <TableCell>{item.totalCost}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </CardContent>
              </Card>
            </Grid>
          </Grid>

          <Card>
            <CardContent>
              <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 1.5 }}>
                주식 보유 현황
              </Typography>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Symbol</TableCell>
                    <TableCell>Market</TableCell>
                    <TableCell>Currency</TableCell>
                    <TableCell>Quantity</TableCell>
                    <TableCell>Avg Price</TableCell>
                    <TableCell>Total Cost</TableCell>
                    <TableCell>Last Trade</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {overview.holdings.map((item) => (
                    <TableRow key={`${item.symbol}-${item.market}`}>
                      <TableCell>{item.symbol}</TableCell>
                      <TableCell>{item.market}</TableCell>
                      <TableCell>{item.currency}</TableCell>
                      <TableCell>{item.quantity}</TableCell>
                      <TableCell>{item.averagePrice}</TableCell>
                      <TableCell>{item.totalCost}</TableCell>
                      <TableCell>{item.lastTradeDate}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>

          <Card>
            <CardContent>
              <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 1.5 }}>
                최근 매수 내역
              </Typography>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>ID</TableCell>
                    <TableCell>Trade Date</TableCell>
                    <TableCell>Symbol</TableCell>
                    <TableCell>Market</TableCell>
                    <TableCell>Quantity</TableCell>
                    <TableCell>Price</TableCell>
                    <TableCell>Fee</TableCell>
                    <TableCell>Net Amount</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {overview.recentPurchases.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell>{item.id}</TableCell>
                      <TableCell>{item.tradeDate}</TableCell>
                      <TableCell>{item.symbol}</TableCell>
                      <TableCell>{item.market}</TableCell>
                      <TableCell>{item.quantity}</TableCell>
                      <TableCell>{item.price}</TableCell>
                      <TableCell>{item.feeAmount}</TableCell>
                      <TableCell>{item.netAmount}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </>
      )}
    </Stack>
  );
}
