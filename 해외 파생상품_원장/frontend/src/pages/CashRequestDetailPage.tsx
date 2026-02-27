import { useEffect, useState } from "react";
import { Card, CardContent, Stack, Typography } from "@mui/material";
import { useParams } from "react-router-dom";
import { api } from "../api/client";
import type { RequestRow } from "../types/models";

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
          <Typography variant="h5" fontWeight={700}>
            Cash Request Detail
          </Typography>
          <Typography>ID: {item.id}</Typography>
          <Typography>Type: {item.requestType}</Typography>
          <Typography>Status: {item.status}</Typography>
          <Typography>Account ID: {item.accountId}</Typography>
          <Typography>Amount: {item.amount}</Typography>
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
