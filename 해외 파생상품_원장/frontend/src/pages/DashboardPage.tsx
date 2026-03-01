import { useEffect, useMemo, useState } from "react";
import { Box, Card, CardContent, Chip, Grid, Stack, Typography } from "@mui/material";
import { api } from "../api/client";
import { PageTitle } from "../components/PageTitle";
import type { BatchRun, DomainTerm, PagedResponse, RequestRow } from "../types/models";
import { pageDescriptions } from "../utils/pageDescriptions";

type StatCardProps = {
  title: string;
  value: number;
};

function StatCard({ title, value }: StatCardProps) {
  return (
    <Card>
      <CardContent>
        <Typography variant="overline" color="text.secondary">
          {title}
        </Typography>
        <Typography variant="h4" fontWeight={700}>
          {value}
        </Typography>
      </CardContent>
    </Card>
  );
}

export function DashboardPage() {
  const [failedBatchCount, setFailedBatchCount] = useState(0);
  const [pendingCashCount, setPendingCashCount] = useState(0);
  const [pendingFxCount, setPendingFxCount] = useState(0);
  const [domainTerms, setDomainTerms] = useState<DomainTerm[]>([]);

  useEffect(() => {
    const load = async () => {
      const [batches, cashRequests, fxRequests, terms] = await Promise.all([
        api.get<PagedResponse<BatchRun>>("/batches/runs?size=100"),
        api.get<PagedResponse<RequestRow>>("/cash-requests?size=100"),
        api.get<PagedResponse<RequestRow>>("/fx-requests?size=100"),
        api.get<DomainTerm[]>("/domain-terms"),
      ]);

      setFailedBatchCount(batches.content.filter((x) => x.status === "FAILED").length);
      setPendingCashCount(cashRequests.content.filter((x) => x.status === "PENDING").length);
      setPendingFxCount(fxRequests.content.filter((x) => x.status === "PENDING").length);
      setDomainTerms(terms);
    };

    load().catch(console.error);
  }, []);

  const groupedTerms = useMemo(() => {
    return domainTerms.reduce<Array<{ domainKey: string; domainName: string; items: DomainTerm[] }>>((acc, item) => {
      const current = acc.find((group) => group.domainKey === item.domainKey);
      if (current) {
        current.items.push(item);
        return acc;
      }

      acc.push({
        domainKey: item.domainKey,
        domainName: item.domainName,
        items: [item],
      });
      return acc;
    }, []);
  }, [domainTerms]);

  return (
    <Grid container spacing={2}>
      <Grid item xs={12}>
        <PageTitle title="Dashboard" description={pageDescriptions.dashboard} />
      </Grid>
      <Grid item xs={12} md={4}>
        <StatCard title="오늘 실패 배치" value={failedBatchCount} />
      </Grid>
      <Grid item xs={12} md={4}>
        <StatCard title="입출금 승인 대기" value={pendingCashCount} />
      </Grid>
      <Grid item xs={12} md={4}>
        <StatCard title="환전 승인 대기" value={pendingFxCount} />
      </Grid>
      <Grid item xs={12}>
        <Card>
          <CardContent>
            <Stack spacing={2}>
              <Box>
                <Typography variant="h6" fontWeight={700}>
                  도메인 용어집
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  운영 화면에서 쓰는 핵심 용어를 DB 기준으로 정리했습니다. 문서와 Dashboard가 같은 데이터를 봅니다.
                </Typography>
              </Box>
              <Grid container spacing={2}>
                {groupedTerms.map((group) => (
                  <Grid key={group.domainKey} item xs={12} lg={6}>
                    <Card variant="outlined" sx={{ height: "100%" }}>
                      <CardContent>
                        <Stack spacing={1.5}>
                          <Typography variant="subtitle1" fontWeight={700}>
                            {group.domainName}
                          </Typography>
                          {group.items.map((item) => (
                            <Box key={item.termKey} sx={{ pb: 1.5, borderBottom: "1px solid", borderColor: "divider", "&:last-child": { pb: 0, borderBottom: "none" } }}>
                              <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap" useFlexGap>
                                <Typography variant="body1" fontWeight={700}>
                                  {item.koreanName}
                                </Typography>
                                <Chip label={item.termName} size="small" variant="outlined" />
                              </Stack>
                              <Typography variant="body2" sx={{ mt: 0.75 }}>
                                {item.description}
                              </Typography>
                              {item.exampleText && (
                                <Typography variant="caption" color="text.secondary" sx={{ display: "block", mt: 0.75 }}>
                                  예시: {item.exampleText}
                                </Typography>
                              )}
                            </Box>
                          ))}
                        </Stack>
                      </CardContent>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            </Stack>
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );
}
