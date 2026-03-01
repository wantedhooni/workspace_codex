import { useEffect, useMemo, useState } from "react";
import { Box, Card, CardContent, Chip, Grid, Stack, TextField, Typography } from "@mui/material";
import { api } from "../api/client";
import { PageTitle } from "../components/PageTitle";
import type { DomainTerm } from "../types/models";
import { pageDescriptions } from "../utils/pageDescriptions";

export function DomainTermsPage() {
  const [rows, setRows] = useState<DomainTerm[]>([]);
  const [keyword, setKeyword] = useState("");

  useEffect(() => {
    api.get<DomainTerm[]>("/domain-terms").then(setRows).catch(console.error);
  }, []);

  const filteredRows = useMemo(() => {
    const normalizedKeyword = keyword.trim().toLowerCase();
    if (!normalizedKeyword) {
      return rows;
    }

    return rows.filter((item) =>
      [
        item.domainName,
        item.koreanName,
        item.termName,
        item.description,
        item.exampleText ?? "",
      ]
        .join(" ")
        .toLowerCase()
        .includes(normalizedKeyword),
    );
  }, [keyword, rows]);

  const groupedTerms = useMemo(() => {
    return filteredRows.reduce<Array<{ domainKey: string; domainName: string; items: DomainTerm[] }>>((acc, item) => {
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
  }, [filteredRows]);

  return (
    <Stack spacing={2}>
      <PageTitle title="Domain Terms" description={pageDescriptions.domainTerms} />
      <Card>
        <CardContent>
          <Stack spacing={2}>
            <Box>
              <Typography variant="h6" fontWeight={700}>
                도메인 용어집
              </Typography>
              <Typography variant="body2" color="text.secondary">
                운영, 거래, 통제 화면에서 사용하는 핵심 용어를 한글 설명과 예시로 정리했습니다.
              </Typography>
            </Box>
            <TextField
              label="용어 검색"
              value={keyword}
              onChange={(event) => setKeyword(event.target.value)}
              placeholder="예: 정산, 브로커, 포지션"
              size="small"
            />
            <Grid container spacing={2}>
              {groupedTerms.map((group) => (
                <Grid key={group.domainKey} item xs={12} lg={6}>
                  <Card variant="outlined" sx={{ height: "100%" }}>
                    <CardContent>
                      <Stack spacing={1.5}>
                        <Stack direction="row" alignItems="center" justifyContent="space-between" spacing={1}>
                          <Typography variant="subtitle1" fontWeight={700}>
                            {group.domainName}
                          </Typography>
                          <Chip label={`${group.items.length}개`} size="small" variant="outlined" />
                        </Stack>
                        {group.items.map((item) => (
                          <Box
                            key={item.termKey}
                            sx={{
                              pb: 1.5,
                              borderBottom: "1px solid",
                              borderColor: "divider",
                              "&:last-child": { pb: 0, borderBottom: "none" },
                            }}
                          >
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
            {groupedTerms.length === 0 && (
              <Typography variant="body2" color="text.secondary">
                검색 조건에 맞는 용어가 없습니다.
              </Typography>
            )}
          </Stack>
        </CardContent>
      </Card>
    </Stack>
  );
}
