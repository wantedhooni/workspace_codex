"use client";

import { useDeferredValue, useState } from "react";
import { useForm } from "react-hook-form";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { Activity, Search, Trash2 } from "lucide-react";
import { ApiError, apiClient } from "@/lib/api-client";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import type { WatchlistCoverage, WatchlistItem } from "@/features/watchlist/types/watchlist";

const schema = z.object({
  ticker: z.string().min(1, "티커를 입력하세요."),
  companyName: z.string().min(2, "종목명을 입력하세요."),
});

type FormValues = z.infer<typeof schema>;

const dateFormatter = new Intl.DateTimeFormat("ko-KR", {
  month: "short",
  day: "numeric",
  hour: "2-digit",
  minute: "2-digit",
});

function formatLocalDate(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    year: "numeric",
    month: "short",
    day: "numeric",
  }).format(new Date(`${value}T00:00:00`));
}

export function WatchlistView() {
  const [query, setQuery] = useState("");
  const deferredQuery = useDeferredValue(query);
  const queryClient = useQueryClient();
  const coverageQuery = useQuery({
    queryKey: ["watchlist-coverage", deferredQuery],
    queryFn: () =>
      apiClient.request<WatchlistCoverage[]>(
        deferredQuery ? `/watchlists/coverage?query=${encodeURIComponent(deferredQuery)}` : "/watchlists/coverage",
      ),
  });
  const createMutation = useMutation({
    mutationFn: (values: FormValues) =>
      apiClient.request<WatchlistItem>("/watchlists", {
        method: "POST",
        body: JSON.stringify(values),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["watchlist-coverage"] });
    },
  });
  const deleteMutation = useMutation({
    mutationFn: (id: number) => apiClient.request<void>(`/watchlists/${id}`, { method: "DELETE" }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["watchlist-coverage"] });
    },
  });
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
  });
  const items = coverageQuery.data ?? [];
  const watchlistError = coverageQuery.error instanceof ApiError ? coverageQuery.error.message : null;
  const mutationError =
    createMutation.error instanceof ApiError
      ? createMutation.error.message
      : deleteMutation.error instanceof ApiError
        ? deleteMutation.error.message
        : null;
  const signalCounts = items.reduce(
    (accumulator, item) => {
      if (!item.signalAvailable || !item.action) {
        accumulator.pending += 1;
        return accumulator;
      }
      accumulator[item.action] += 1;
      return accumulator;
    },
    { BUY: 0, WATCH: 0, AVOID: 0, pending: 0 },
  );

  const onSubmit = async (values: FormValues) => {
    await createMutation.mutateAsync(values);
    reset();
  };

  return (
    <div className="grid gap-6 lg:grid-cols-[380px_minmax(0,1fr)]">
      <Card>
        <CardHeader>
          <CardDescription>Watchlist Control</CardDescription>
          <CardTitle>추적 우선순위 관리</CardTitle>
        </CardHeader>
        <CardContent className="space-y-5">
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-1">
            <div className="rounded-[24px] border border-border bg-white/74 px-4 py-4">
              <p className="text-[11px] uppercase tracking-[0.16em] text-foreground/45">BUY / WATCH</p>
              <p className="mt-2 text-2xl font-semibold">{signalCounts.BUY + signalCounts.WATCH}</p>
              <p className="mt-2 text-sm leading-6 text-foreground/64">당장 계속 추적할 후보 수입니다.</p>
            </div>
            <div className="rounded-[24px] border border-border bg-white/74 px-4 py-4">
              <p className="text-[11px] uppercase tracking-[0.16em] text-foreground/45">Pending</p>
              <p className="mt-2 text-2xl font-semibold">{signalCounts.pending}</p>
              <p className="mt-2 text-sm leading-6 text-foreground/64">아직 최신 스냅샷이 없는 종목 수입니다.</p>
            </div>
          </div>
          <form className="space-y-5" onSubmit={handleSubmit(onSubmit)}>
            <div>
              <Label htmlFor="ticker">Ticker</Label>
              <Input id="ticker" placeholder="AAPL" {...register("ticker")} />
              {errors.ticker && <p className="mt-2 text-sm text-danger">{errors.ticker.message}</p>}
            </div>
            <div>
              <Label htmlFor="companyName">Company</Label>
              <Input id="companyName" placeholder="Apple Inc." {...register("companyName")} />
              {errors.companyName && <p className="mt-2 text-sm text-danger">{errors.companyName.message}</p>}
            </div>
            <Button className="w-full" type="submit" disabled={createMutation.isPending}>
              {createMutation.isPending ? "추가 중..." : "관심 종목 등록"}
            </Button>
          </form>
          {mutationError && <p className="text-sm text-danger">{mutationError}</p>}
        </CardContent>
      </Card>
      <Card>
        <CardHeader>
          <CardDescription>Coverage Board</CardDescription>
          <CardTitle>현재 추적 목록</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
            <div className="rounded-[24px] border border-border bg-white/72 px-4 py-4">
              <p className="text-xs uppercase tracking-[0.16em] text-foreground/50">BUY</p>
              <p className="mt-2 text-2xl font-semibold text-success">{signalCounts.BUY}</p>
            </div>
            <div className="rounded-[24px] border border-border bg-white/72 px-4 py-4">
              <p className="text-xs uppercase tracking-[0.16em] text-foreground/50">WATCH</p>
              <p className="mt-2 text-2xl font-semibold text-accent">{signalCounts.WATCH}</p>
            </div>
            <div className="rounded-[24px] border border-border bg-white/72 px-4 py-4">
              <p className="text-xs uppercase tracking-[0.16em] text-foreground/50">AVOID</p>
              <p className="mt-2 text-2xl font-semibold text-danger">{signalCounts.AVOID}</p>
            </div>
            <div className="rounded-[24px] border border-border bg-white/72 px-4 py-4">
              <p className="text-xs uppercase tracking-[0.16em] text-foreground/50">Pending</p>
              <p className="mt-2 text-2xl font-semibold text-foreground">{signalCounts.pending}</p>
            </div>
          </div>
          <div className="rounded-[24px] border border-border bg-white/72 px-4 py-4">
            <div className="flex items-center gap-3 rounded-[20px] border border-border bg-white px-4">
              <Search className="h-4 w-4 text-foreground/45" />
              <Input
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder="티커 또는 종목명으로 검색"
                className="border-0 bg-transparent px-0 focus:border-0"
              />
            </div>
            <p className="mt-3 text-sm text-foreground/60">
              현재 {items.length}개 종목이 표시되고 있습니다.
            </p>
          </div>
          {watchlistError && <p className="text-sm text-danger">{watchlistError}</p>}
          {items.map((item) => (
            <div key={item.id} className="rounded-[26px] border border-border bg-white/76 px-5 py-5">
              <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_180px_auto] xl:items-start">
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-3">
                    <p className="text-lg font-semibold">{item.ticker}</p>
                    <span className="text-xs uppercase tracking-[0.16em] text-foreground/45">
                      등록 {dateFormatter.format(new Date(item.createdAt))}
                    </span>
                  </div>
                  <p className="mt-1 text-sm text-foreground/65">{item.companyName}</p>
                  <div className="mt-3 flex flex-wrap gap-2">
                    {item.signalAvailable && item.action ? (
                      <>
                        <Badge>{item.action}</Badge>
                        <Badge>Score {item.score}</Badge>
                      </>
                    ) : (
                      <span className="inline-flex items-center rounded-full border border-border px-3 py-1 text-xs font-medium text-foreground/60">
                        Signal Pending
                      </span>
                    )}
                  </div>
                </div>
                <div className="rounded-[20px] border border-border bg-background/55 px-4 py-4">
                  <p className="text-[11px] uppercase tracking-[0.16em] text-foreground/45">Latest Signal</p>
                  <p className="mt-2 text-sm font-semibold">
                    {item.latestSignalDate ? formatLocalDate(item.latestSignalDate) : "미생성"}
                  </p>
                  <p className="mt-2 text-sm leading-6 text-foreground/62">
                    {item.signalAvailable ? "최신 점수와 근거를 아래에서 확인하세요." : "다음 스냅샷 적재 후 계산됩니다."}
                  </p>
                </div>
                <Button
                  variant="ghost"
                  className="justify-center xl:justify-end"
                  onClick={() => deleteMutation.mutate(item.id)}
                  disabled={deleteMutation.isPending}
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </div>
              {item.signalAvailable && item.action ? (
                <div className="mt-4 flex flex-wrap gap-2">
                    {item.reasons.map((reason) => (
                      <span
                        key={`${item.id}-${reason}`}
                        className="inline-flex items-center rounded-full border border-foreground/10 bg-background/55 px-3 py-1 text-xs font-medium text-foreground/70"
                      >
                        {reason}
                      </span>
                    ))}
                </div>
              ) : (
                <div className="mt-4 flex items-center gap-2 rounded-[20px] bg-background/60 px-4 py-3 text-sm text-foreground/65">
                  <Activity className="h-4 w-4" />
                  최신 스냅샷이 없어 아직 시그널을 계산하지 못했습니다.
                </div>
              )}
            </div>
          ))}
          {items.length === 0 && !coverageQuery.isLoading && (
            <p className="text-sm text-foreground/65">
              {deferredQuery ? "검색 조건에 맞는 관심 종목이 없습니다." : "등록된 관심 종목이 없습니다."}
            </p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
