import {
  Box,
  Button as MuiButton,
  Card,
  CardContent,
  Dialog,
  DialogContent,
  DialogTitle,
  Grid,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableRow,
  Tabs,
  Typography
} from "@mui/material";
import { useMemo, useState } from "react";
import { useRecordContext } from "react-admin";

type DetailField = {
  source: string;
  label: string;
  render?: (value: unknown, record: Record<string, unknown>) => string;
};

type DetailSummaryItem = {
  label: string;
  value: (record: Record<string, unknown>) => string;
};

type RecordDetailButtonProps = {
  title?: string;
  fields?: DetailField[];
  summaryItems?: DetailSummaryItem[];
  label?: string;
};

const pretty = (value: unknown): string => {
  if (value === null || value === undefined) {
    return "";
  }

  if (typeof value === "string" || typeof value === "number" || typeof value === "boolean") {
    return String(value);
  }

  if (Array.isArray(value)) {
    return value
      .map((item) => pretty(item))
      .filter((item) => item.length > 0)
      .join(", ");
  }

  try {
    return JSON.stringify(value);
  } catch {
    return String(value);
  }
};

export function RecordDetailButton({
  title = "상세",
  fields,
  summaryItems,
  label = "상세"
}: RecordDetailButtonProps) {
  const record = useRecordContext<Record<string, unknown>>();
  const [open, setOpen] = useState(false);
  const [tabIndex, setTabIndex] = useState(0);

  const resolvedFields = useMemo<DetailField[]>(() => {
    if (fields && fields.length > 0) {
      return fields;
    }

    if (!record) {
      return [];
    }

    return Object.keys(record)
      .filter((key) => key !== "id")
      .map((key) => ({ source: key, label: key }));
  }, [fields, record]);

  const resolvedSummary = useMemo(
    () =>
      (summaryItems ?? []).map((item) => {
        try {
          return { label: item.label, value: item.value(record ?? {}) };
        } catch {
          return { label: item.label, value: "-" };
        }
      }),
    [record, summaryItems]
  );

  if (!record) {
    return null;
  }

  return (
    <>
      <MuiButton
        variant="outlined"
        size="small"
        onClick={() => {
          setTabIndex(0);
          setOpen(true);
        }}
      >
        {label}
      </MuiButton>

      <Dialog
        open={open}
        onClose={() => {
          setOpen(false);
          setTabIndex(0);
        }}
        fullWidth
        maxWidth="md"
      >
        <DialogTitle>{title}</DialogTitle>
        <DialogContent>
          {resolvedSummary.length > 0 ? (
            <Grid container spacing={1} sx={{ mb: 1 }}>
              {resolvedSummary.map((item) => (
                <Grid key={item.label} item xs={12} md={4}>
                  <Card variant="outlined">
                    <CardContent sx={{ py: 1.2, "&:last-child": { pb: 1.2 } }}>
                      <Typography variant="caption" color="text.secondary">
                        {item.label}
                      </Typography>
                      <Typography variant="body1" fontWeight={700}>
                        {item.value}
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>
          ) : null}

          <Tabs value={tabIndex} onChange={(_, value: number) => setTabIndex(value)}>
            <Tab label="필드 보기" />
            <Tab label="원본 JSON" />
          </Tabs>

          {tabIndex === 0 ? (
            <TableContainer sx={{ mt: 1 }}>
              <Table size="small">
                <TableBody>
                  {resolvedFields.map((field) => {
                    const raw = record[field.source];
                    const displayed = field.render ? field.render(raw, record) : pretty(raw);
                    return (
                      <TableRow key={field.source}>
                        <TableCell sx={{ width: 220, fontWeight: 600 }}>{field.label}</TableCell>
                        <TableCell sx={{ wordBreak: "break-all" }}>{displayed}</TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </TableContainer>
          ) : (
            <Box
              component="pre"
              sx={{
                mt: 1,
                p: 1.5,
                borderRadius: 1,
                overflowX: "auto",
                backgroundColor: "action.hover"
              }}
            >
              <Typography component="span" variant="body2" sx={{ fontFamily: "monospace" }}>
                {JSON.stringify(record, null, 2)}
              </Typography>
            </Box>
          )}
        </DialogContent>
      </Dialog>
    </>
  );
}
