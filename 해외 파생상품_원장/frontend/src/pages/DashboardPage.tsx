import { useEffect, useState } from "react";
import { Card, CardContent, Grid, Typography } from "@mui/material";
import { api } from "../api/client";
import type { BatchRun, PagedResponse, RequestRow } from "../types/models";

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

  useEffect(() => {
    const load = async () => {
      const [batches, cashRequests, fxRequests] = await Promise.all([
        api.get<PagedResponse<BatchRun>>("/batches/runs?size=100"),
        api.get<PagedResponse<RequestRow>>("/cash-requests?size=100"),
        api.get<PagedResponse<RequestRow>>("/fx-requests?size=100"),
      ]);

      setFailedBatchCount(batches.content.filter((x) => x.status === "FAILED").length);
      setPendingCashCount(cashRequests.content.filter((x) => x.status === "PENDING").length);
      setPendingFxCount(fxRequests.content.filter((x) => x.status === "PENDING").length);
    };

    load().catch(console.error);
  }, []);

  return (
    <Grid container spacing={2}>
      <Grid item xs={12} md={4}>
        <StatCard title="오늘 실패 배치" value={failedBatchCount} />
      </Grid>
      <Grid item xs={12} md={4}>
        <StatCard title="입출금 승인 대기" value={pendingCashCount} />
      </Grid>
      <Grid item xs={12} md={4}>
        <StatCard title="환전 승인 대기" value={pendingFxCount} />
      </Grid>
    </Grid>
  );
}
