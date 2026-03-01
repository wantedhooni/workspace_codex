import { useEffect, useState } from "react";
import { Card, CardContent, Stack, Typography } from "@mui/material";
import { useParams } from "react-router-dom";
import { api } from "../api/client";
import { PageTitle } from "../components/PageTitle";
import type { RequestRow } from "../types/models";
import { pageDescriptions } from "../utils/pageDescriptions";

export function CashRequestDetailPage() {
  const { id } = useParams();
  const [item, setItem] = useState<RequestRow | null>(null);

  useEffect(() => {
    if (!id) return;
    api.get<RequestRow>(`/cash-requests/${id}`).then(setItem).catch(console.error);
  }, [id]);

  if (!item) {
    return <Typography>Loading...</Typography>;
  }

  return (
    <Card>
      <CardContent>
        <Stack spacing={1}>
          <PageTitle title="Cash Request Detail" description={pageDescriptions.cashRequestDetail} />
          <Typography>ID: {item.id}</Typography>
          <Typography>Type: {item.requestType}</Typography>
          <Typography>Status: {item.status}</Typography>
          <Typography>
            Account ID: {item.accountId} ({item.accountNo})
          </Typography>
          <Typography>
            Amount: {item.amount} {item.currency}
          </Typography>
          <Typography>Priority: {item.priority ?? "-"}</Typography>
          <Typography>Value Date: {item.valueDate ?? "-"}</Typography>
          <Typography>Manual Review Required: {item.manualReviewRequired ? "Y" : "N"}</Typography>
          <Typography>Control Reason: {item.controlReason ?? "-"}</Typography>
          <Typography>Control Policy ID: {item.controlPolicyId ?? "-"}</Typography>
          <Typography>Control Policy Source: {item.controlPolicySource ?? "-"}</Typography>
          <Typography>Risk Policy ID: {item.controlLimitPolicyId ?? "-"}</Typography>
          <Typography>Risk Policy Source: {item.controlLimitPolicySource ?? "-"}</Typography>
          <Typography>Projected Daily Exposure: {item.projectedDailyExposure ?? "-"}</Typography>
          <Typography>SLA Due At: {item.slaDueAt ?? "-"}</Typography>
          <Typography>Requested By: {item.requestedBy}</Typography>
          <Typography>Reviewed By: {item.reviewedBy ?? "-"}</Typography>
          <Typography>Reason: {item.reason}</Typography>
          <Typography>Review Reason: {item.reviewReason ?? "-"}</Typography>
          <Typography>Requested At: {item.requestedAt}</Typography>
          <Typography>Reviewed At: {item.reviewedAt ?? "-"}</Typography>
        </Stack>
      </CardContent>
    </Card>
  );
}
