"use client";

import { useDeferredValue, useState } from "react";
import { useForm } from "react-hook-form";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { History, Search, Sparkles } from "lucide-react";
import { apiClient } from "@/lib/api-client";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import type { NewsAnalysis } from "@/features/news/types/news";

const schema = z.object({
  headline: z.string().min(5, "헤드라인을 입력하세요."),
  content: z.string().min(20, "본문은 20자 이상 입력하세요."),
});

type FormValues = z.infer<typeof schema>;

const dateFormatter = new Intl.DateTimeFormat("ko-KR", {
  month: "short",
  day: "numeric",
  hour: "2-digit",
  minute: "2-digit",
});

export function NewsAnalyzeForm() {
  const queryClient = useQueryClient();
  const [selectedAnalysis, setSelectedAnalysis] = useState<NewsAnalysis | null>(null);
  const [historyQueryText, setHistoryQueryText] = useState("");
  const [sentimentFilter, setSentimentFilter] = useState<NewsAnalysis["sentiment"] | "ALL">("ALL");
  const deferredHistoryQueryText = useDeferredValue(historyQueryText);
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      headline: "NVIDIA surges after stronger-than-expected datacenter demand",
      content:
        "NVIDIA reported stronger-than-expected datacenter demand and guided above consensus, while management noted persistent AI infrastructure expansion from enterprise customers.",
    },
  });
  const historyQuery = useQuery({
    queryKey: ["news-analyses", 8, deferredHistoryQueryText, sentimentFilter],
    queryFn: () => {
      const params = new URLSearchParams({ limit: "8" });
      if (deferredHistoryQueryText.trim()) {
        params.set("query", deferredHistoryQueryText.trim());
      }
      if (sentimentFilter !== "ALL") {
        params.set("sentiment", sentimentFilter);
      }
      return apiClient.request<NewsAnalysis[]>(`/news/analyses?${params.toString()}`);
    },
  });
  const mutation = useMutation({
    mutationFn: (values: FormValues) =>
      apiClient.request<NewsAnalysis>("/news/analyze", {
        method: "POST",
        body: JSON.stringify(values),
      }),
    onSuccess: (analysis) => {
      setSelectedAnalysis(analysis);
      queryClient.invalidateQueries({ queryKey: ["news-analyses"] });
    },
  });
  const activeAnalysis = selectedAnalysis ?? mutation.data ?? historyQuery.data?.[0] ?? null;

  return (
    <div className="grid gap-6 xl:grid-cols-[minmax(0,420px)_minmax(0,1fr)]">
      <Card>
        <CardHeader>
          <CardDescription>News Desk</CardDescription>
          <CardTitle>뉴스 입력</CardTitle>
        </CardHeader>
        <CardContent className="space-y-5">
          <div className="rounded-[24px] border border-border bg-white/74 px-4 py-4">
            <p className="text-[11px] font-semibold uppercase tracking-[0.16em] text-foreground/45">Workflow</p>
            <p className="mt-2 text-sm leading-6 text-foreground/66">
              헤드라인을 붙여 넣고 1차 해석을 만든 뒤, 가격 반응과 함께 다시 교차검증하는 흐름을 전제로 구성했습니다.
            </p>
          </div>
          <form className="space-y-5" onSubmit={form.handleSubmit((values) => mutation.mutate(values))}>
            <div>
              <Label htmlFor="headline">Headline</Label>
              <Input id="headline" {...form.register("headline")} />
              {form.formState.errors.headline && (
                <p className="mt-2 text-sm text-danger">{form.formState.errors.headline.message}</p>
              )}
            </div>
            <div>
              <Label htmlFor="content">Content</Label>
              <Textarea id="content" {...form.register("content")} />
              {form.formState.errors.content && (
                <p className="mt-2 text-sm text-danger">{form.formState.errors.content.message}</p>
              )}
            </div>
            <Button className="w-full" type="submit" disabled={mutation.isPending}>
              {mutation.isPending ? "분석 중..." : "뉴스 분석"}
            </Button>
          </form>
        </CardContent>
      </Card>
      <div className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-3">
              <Sparkles className="h-5 w-5 text-accent" />
              분석 결과
            </CardTitle>
            <CardDescription>AI 해석은 설명 보조용이며, 최종 판단은 가격 반응과 함께 확인해야 합니다.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-5">
            {activeAnalysis ? (
              <>
                <div className="rounded-[24px] border border-border bg-white/74 px-4 py-4">
                  <p className="text-sm font-semibold leading-6 text-foreground">{activeAnalysis.headline}</p>
                  <p className="mt-2 text-xs uppercase tracking-[0.18em] text-foreground/50">{dateFormatter.format(new Date(activeAnalysis.analyzedAt))}</p>
                </div>
                <div className="flex flex-wrap gap-2">
                  <Badge>{activeAnalysis.sentiment}</Badge>
                  <Badge>{activeAnalysis.impact}</Badge>
                </div>
                <div className="grid gap-4 lg:grid-cols-2">
                  <div className="rounded-[24px] border border-border bg-white/74 px-4 py-4">
                    <p className="text-xs font-semibold uppercase tracking-[0.2em] text-accent">Summary</p>
                    <p className="mt-2 text-sm leading-6 text-foreground/80">{activeAnalysis.summary}</p>
                  </div>
                  <div className="rounded-[24px] border border-border bg-white/74 px-4 py-4">
                    <p className="text-xs font-semibold uppercase tracking-[0.2em] text-accent">Interpretation</p>
                    <p className="mt-2 text-sm leading-6 text-foreground/80">{activeAnalysis.interpretation}</p>
                  </div>
                </div>
              </>
            ) : (
              <p className="text-sm text-foreground/65">좌측 입력 폼으로 기사를 제출하면 결과가 표시됩니다.</p>
            )}
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-3">
              <History className="h-5 w-5 text-accent" />
              최근 분석 이력
            </CardTitle>
            <CardDescription>최근 생성한 분석 결과를 다시 열어 비교할 수 있습니다.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="rounded-[24px] border border-border bg-white/72 px-4 py-4">
              <div className="flex items-center gap-3 rounded-[20px] border border-border bg-white px-4">
                <Search className="h-4 w-4 text-foreground/45" />
                <Input
                  value={historyQueryText}
                  onChange={(event) => setHistoryQueryText(event.target.value)}
                  placeholder="헤드라인 검색"
                  className="border-0 bg-transparent px-0 focus:border-0"
                />
              </div>
              <div className="mt-3 flex flex-wrap gap-2">
                {(["ALL", "POSITIVE", "NEUTRAL", "NEGATIVE"] as const).map((item) => (
                  <Button
                    key={item}
                    type="button"
                    variant={sentimentFilter === item ? "default" : "outline"}
                    size="sm"
                    onClick={() => setSentimentFilter(item)}
                  >
                    {item}
                  </Button>
                ))}
              </div>
            </div>
            {historyQuery.data?.map((item) => (
              <button
                key={item.id}
                type="button"
                onClick={() => setSelectedAnalysis(item)}
                className="w-full rounded-[24px] border border-border bg-white/72 px-4 py-4 text-left transition hover:border-primary"
              >
                <div className="flex items-center justify-between gap-3">
                  <p className="line-clamp-2 text-sm font-semibold text-foreground">{item.headline}</p>
                  <Badge>{item.sentiment}</Badge>
                </div>
                <div className="mt-3 flex flex-wrap gap-2">
                  <Badge>{item.impact}</Badge>
                  <span className="text-xs uppercase tracking-[0.16em] text-foreground/50">
                    {dateFormatter.format(new Date(item.analyzedAt))}
                  </span>
                </div>
              </button>
            ))}
            {historyQuery.isLoading && <p className="text-sm text-foreground/65">이력을 불러오는 중입니다.</p>}
            {!historyQuery.data?.length && !historyQuery.isLoading && (
              <p className="text-sm text-foreground/65">
                {deferredHistoryQueryText || sentimentFilter !== "ALL"
                  ? "조건에 맞는 뉴스 분석 이력이 없습니다."
                  : "저장된 뉴스 분석 이력이 없습니다."}
              </p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
