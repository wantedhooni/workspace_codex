import { useEffect, useState } from "react";
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  FormControl,
  Grid,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";
import { api } from "../api/client";
import { PageTitle } from "../components/PageTitle";
import type { Account, PagedResponse, RecommendationHorizon, RecommendationRiskProfile, StockRecommendationResult } from "../types/models";
import { pageDescriptions } from "../utils/pageDescriptions";

export function StockRecommendationsPage() {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [accountId, setAccountId] = useState("");
  const [riskProfile, setRiskProfile] = useState<RecommendationRiskProfile>("BALANCED");
  const [investmentHorizon, setInvestmentHorizon] = useState<RecommendationHorizon>("MEDIUM_TERM");
  const [preferredMarkets, setPreferredMarkets] = useState("NASDAQ");
  const [candidateSymbols, setCandidateSymbols] = useState("MSFT, AMD, QQQ");
  const [operatorView, setOperatorView] = useState("기존 보유 종목 편중을 완화할 수 있는 후보를 보고 싶음");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<StockRecommendationResult | null>(null);

  useEffect(() => {
    api.get<PagedResponse<Account>>("/accounts?size=100")
      .then((res) => {
        setAccounts(res.content);
        if (res.content.length > 0) {
          setAccountId(String(res.content[0].id));
        }
      })
      .catch((err: Error) => setError(err.message));
  }, []);

  const generate = async () => {
    if (!accountId) {
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const response = await api.post<StockRecommendationResult>("/stock-recommendations", {
        accountId: Number(accountId),
        riskProfile,
        investmentHorizon,
        maxRecommendations: 3,
        preferredMarkets: preferredMarkets.split(",").map((item) => item.trim()).filter(Boolean),
        candidateSymbols: candidateSymbols.split(",").map((item) => item.trim().toUpperCase()).filter(Boolean),
        operatorView,
      });
      setResult(response);
    } catch (err) {
      setError(err instanceof Error ? err.message : "추천 결과를 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Stack spacing={2}>
      <PageTitle title="Stock Recommendations" description={pageDescriptions.stockRecommendations} />

      <Alert severity="info">
        AI 추천은 운영 보조용 초안입니다. 실제 주문 전 시세, 유동성, 브로커 주문 가능 시장, 내부 승인 정책을 별도로 확인해야 합니다.
      </Alert>

      <Card>
        <CardContent>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>Account</InputLabel>
                <Select label="Account" value={accountId} onChange={(event) => setAccountId(event.target.value)}>
                  {accounts.map((item) => (
                    <MenuItem key={item.id} value={String(item.id)}>
                      {item.accountNo} / {item.broker} / {item.ownerName}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={3}>
              <FormControl fullWidth>
                <InputLabel>Risk</InputLabel>
                <Select label="Risk" value={riskProfile} onChange={(event) => setRiskProfile(event.target.value as RecommendationRiskProfile)}>
                  <MenuItem value="CONSERVATIVE">Conservative</MenuItem>
                  <MenuItem value="BALANCED">Balanced</MenuItem>
                  <MenuItem value="AGGRESSIVE">Aggressive</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={3}>
              <FormControl fullWidth>
                <InputLabel>Horizon</InputLabel>
                <Select label="Horizon" value={investmentHorizon} onChange={(event) => setInvestmentHorizon(event.target.value as RecommendationHorizon)}>
                  <MenuItem value="SHORT_TERM">Short Term</MenuItem>
                  <MenuItem value="MEDIUM_TERM">Medium Term</MenuItem>
                  <MenuItem value="LONG_TERM">Long Term</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                label="Preferred Markets"
                value={preferredMarkets}
                onChange={(event) => setPreferredMarkets(event.target.value)}
                placeholder="NASDAQ, NYSE"
                fullWidth
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                label="Candidate Symbols"
                value={candidateSymbols}
                onChange={(event) => setCandidateSymbols(event.target.value)}
                placeholder="MSFT, AMD, QQQ"
                fullWidth
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                label="Operator View"
                value={operatorView}
                onChange={(event) => setOperatorView(event.target.value)}
                placeholder="운용 의도, 분산 필요 사유, 피하고 싶은 종목 등을 적습니다."
                fullWidth
                multiline
                minRows={3}
              />
            </Grid>
            <Grid item xs={12}>
              <Button variant="contained" onClick={() => generate().catch(console.error)} disabled={loading || !accountId}>
                {loading ? "Generating..." : "Generate Recommendation"}
              </Button>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {error && <Alert severity="warning">{error}</Alert>}

      {result && (
        <>
          <Card>
            <CardContent>
              <Stack spacing={1.5}>
                <Stack direction={{ xs: "column", md: "row" }} spacing={1} alignItems={{ xs: "flex-start", md: "center" }}>
                  <Typography variant="h6" fontWeight={700}>
                    추천 요약
                  </Typography>
                  <Chip label={`${result.provider} / ${result.model}`} size="small" variant="outlined" />
                  <Chip label={result.accountNo} size="small" variant="outlined" />
                </Stack>
                <Typography variant="body1">{result.summary}</Typography>
                <Typography variant="caption" color="text.secondary">
                  생성시각: {result.generatedAt}
                </Typography>
              </Stack>
            </CardContent>
          </Card>

          <Grid container spacing={2}>
            <Grid item xs={12} lg={8}>
              <Card sx={{ height: "100%" }}>
                <CardContent>
                  <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 1.5 }}>
                    추천 종목
                  </Typography>
                  <Stack spacing={1.5}>
                    {result.recommendations.map((item) => (
                      <Box key={`${item.rank}-${item.symbol}`} sx={{ border: "1px solid", borderColor: "divider", borderRadius: 2, p: 2 }}>
                        <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap" useFlexGap>
                          <Chip label={`#${item.rank}`} size="small" />
                          <Chip label={item.symbol} size="small" color="primary" variant="outlined" />
                          <Chip label={item.market} size="small" variant="outlined" />
                          <Chip label={item.action} size="small" color={item.action === "BUY" ? "success" : "default"} />
                          <Chip label={item.confidence} size="small" variant="outlined" />
                        </Stack>
                        <Typography variant="body2" sx={{ mt: 1 }}>
                          {item.rationale}
                        </Typography>
                        <Typography variant="caption" color="text.secondary" sx={{ display: "block", mt: 0.75 }}>
                          비중 힌트: {item.allocationHint}
                        </Typography>
                        <Typography variant="caption" color="text.secondary" sx={{ display: "block", mt: 0.5 }}>
                          리스크: {item.riskNotes}
                        </Typography>
                      </Box>
                    ))}
                  </Stack>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} lg={4}>
              <Card sx={{ height: "100%" }}>
                <CardContent>
                  <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 1.5 }}>
                    주의사항
                  </Typography>
                  <Stack spacing={1}>
                    {result.cautionPoints.map((item, index) => (
                      <Alert key={`${index}-${item}`} severity="warning">
                        {item}
                      </Alert>
                    ))}
                  </Stack>
                </CardContent>
              </Card>
            </Grid>
          </Grid>

          <Grid container spacing={2}>
            <Grid item xs={12} md={5}>
              <Card sx={{ height: "100%" }}>
                <CardContent>
                  <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 1.5 }}>
                    현금 스냅샷
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                    기준일: {result.portfolioSnapshot.cashSnapshotDate}
                  </Typography>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Currency</TableCell>
                        <TableCell>Amount</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {result.portfolioSnapshot.cashBalances.map((item) => (
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
            <Grid item xs={12} md={7}>
              <Card sx={{ height: "100%" }}>
                <CardContent>
                  <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 1.5 }}>
                    현재 보유 종목
                  </Typography>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Symbol</TableCell>
                        <TableCell>Market</TableCell>
                        <TableCell>Qty</TableCell>
                        <TableCell>Avg Price</TableCell>
                        <TableCell>Total Cost</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {result.portfolioSnapshot.holdings.map((item) => (
                        <TableRow key={`${item.symbol}-${item.market}`}>
                          <TableCell>{item.symbol}</TableCell>
                          <TableCell>{item.market}</TableCell>
                          <TableCell>{item.quantity}</TableCell>
                          <TableCell>{item.averagePrice}</TableCell>
                          <TableCell>{item.totalCost}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </CardContent>
              </Card>
            </Grid>
          </Grid>

          <Alert severity="info">{result.disclaimer}</Alert>
        </>
      )}
    </Stack>
  );
}
