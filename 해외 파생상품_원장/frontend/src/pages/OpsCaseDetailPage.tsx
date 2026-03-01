import { useEffect, useState } from "react";
import { Card, CardContent, Stack, Typography } from "@mui/material";
import { useParams } from "react-router-dom";
import { api } from "../api/client";
import { PageTitle } from "../components/PageTitle";
import type { OpsCase } from "../types/models";
import { pageDescriptions } from "../utils/pageDescriptions";

export function OpsCaseDetailPage() {
  const { id } = useParams();
  const [item, setItem] = useState<OpsCase | null>(null);

  useEffect(() => {
    if (!id) return;
    api.get<OpsCase>(`/ops-cases/${id}`).then(setItem).catch(console.error);
  }, [id]);

  if (!item) {
    return <Typography>Loading...</Typography>;
  }

  return (
    <Card>
      <CardContent>
        <Stack spacing={1}>
          <PageTitle title="Ops Case Detail" description={pageDescriptions.opsCaseDetail} />
          <Typography>Case No: {item.caseNo}</Typography>
          <Typography>Status: {item.status}</Typography>
          <Typography>Severity: {item.severity}</Typography>
          <Typography>Category: {item.category}</Typography>
          <Typography>Title: {item.title}</Typography>
          <Typography>Description: {item.description}</Typography>
          <Typography>Assignee: {item.assignee ?? "-"}</Typography>
          <Typography>Due At: {item.dueAt ?? "-"}</Typography>
          <Typography>
            Linked: {item.linkedType ?? "-"}:{item.linkedId ?? "-"}
          </Typography>
          <Typography>Account ID: {item.accountId ?? "-"}</Typography>
          <Typography>Resolution Summary: {item.resolutionSummary ?? "-"}</Typography>
          <Typography>Created By: {item.createdBy}</Typography>
          <Typography>Updated By: {item.updatedBy ?? "-"}</Typography>
          <Typography>Resolved By: {item.resolvedBy ?? "-"}</Typography>
          <Typography>Resolved At: {item.resolvedAt ?? "-"}</Typography>
          <Typography>Closed By: {item.closedBy ?? "-"}</Typography>
          <Typography>Closed At: {item.closedAt ?? "-"}</Typography>
          <Typography>Created At: {item.createdAt}</Typography>
          <Typography>Updated At: {item.updatedAt}</Typography>
        </Stack>
      </CardContent>
    </Card>
  );
}
