"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { ActivitySquare, CalendarClock, DatabaseZap, Pause, Play, RefreshCcw } from "lucide-react";
import { ApiError } from "@/lib/api-client";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useBatchJobMetadata, useBatchJobs, usePauseBatchJob, useResumeBatchJob, useRunBatchJob } from "@/features/batch/queries";
import type { BatchExecution, BatchExecutionDetail, BatchJobStatus } from "@/types/batch";

const dateTimeFormatter = new Intl.DateTimeFormat("ko-KR", {
  month: "short",
  day: "numeric",
  hour: "2-digit",
  minute: "2-digit",
});

function formatDateTime(value: string | null) {
  if (!value) {
    return "기록 없음";
  }
  return dateTimeFormatter.format(new Date(value));
}

function formatDuration(value: number | null) {
  if (value == null) {
    return "측정 전";
  }
  return `${value}초`;
}

function getScheduleTone(state: BatchJobStatus["scheduleState"]) {
  switch (state) {
    case "SCHEDULED":
      return "bg-success/10 text-success";
    case "PAUSED":
      return "bg-accent/12 text-accent";
    case "ERROR":
      return "bg-danger/12 text-danger";
    case "MANUAL_ONLY":
      return "bg-foreground/8 text-foreground/70";
    default:
      return "bg-muted text-foreground/70";
  }
}

function getExecutionTone(execution: BatchExecution | null) {
  if (!execution) {
    return "bg-muted text-foreground/65";
  }
  if (execution.status === "COMPLETED") {
    return "bg-success/10 text-success";
  }
  if (execution.status === "STARTED" || execution.status === "STARTING") {
    return "bg-accent/12 text-accent";
  }
  return "bg-danger/12 text-danger";
}

export function BatchControlView() {
  const batchJobsQuery = useBatchJobs();
  const runMutation = useRunBatchJob();
  const pauseMutation = usePauseBatchJob();
  const resumeMutation = useResumeBatchJob();
  const [selectedJobName, setSelectedJobName] = useState<string | null>(null);
  const jobs = batchJobsQuery.data ?? [];
  const batchMetadataQuery = useBatchJobMetadata(selectedJobName);

  useEffect(() => {
    if (jobs.length === 0) {
      return;
    }

    if (!selectedJobName || !jobs.some((job) => job.jobName === selectedJobName)) {
      setSelectedJobName(jobs[0]?.jobName ?? null);
    }
  }, [jobs, selectedJobName]);

  if (batchJobsQuery.isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>배치 운영 상태를 불러오는 중입니다</CardTitle>
          <CardDescription>Quartz 스케줄과 Spring Batch 실행 이력을 JDBC 메타데이터에서 읽고 있습니다.</CardDescription>
        </CardHeader>
      </Card>
    );
  }

  if (batchJobsQuery.isError) {
    const message = batchJobsQuery.error instanceof ApiError ? batchJobsQuery.error.message : "배치 운영 상태를 조회하지 못했습니다.";
    return (
      <Card>
        <CardHeader>
          <CardTitle>배치 운영 상태를 불러오지 못했습니다</CardTitle>
          <CardDescription>{message}</CardDescription>
        </CardHeader>
      </Card>
    );
  }

  const pausedCount = jobs.filter((job) => job.scheduleState === "PAUSED").length;
  const runningCount = jobs.filter((job) => job.running).length;
  const selectedMetadata = batchMetadataQuery.data;
  const selectedMetadataMessage = batchMetadataQuery.error instanceof ApiError
    ? batchMetadataQuery.error.message
    : "JDBC 메타데이터를 조회하지 못했습니다.";

  return (
    <div className="space-y-6">
      <section className="grid gap-6 xl:grid-cols-[minmax(0,1.15fr)_360px]">
        <Card className="overflow-hidden">
          <CardContent className="grid gap-0 p-0 lg:grid-cols-[minmax(0,1fr)_320px]">
            <div className="px-6 py-6 lg:px-8 lg:py-8">
              <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-accent">Operations Console</p>
              <h3 className="mt-4 text-3xl font-semibold leading-tight text-foreground">Quartz 스케줄과 Spring Batch 실행 이력을 JDBC 기준으로 제어합니다.</h3>
              <p className="mt-4 text-sm leading-7 text-foreground/68">
                실무 운영에서 필요한 수동 실행, 스케줄 pause/resume, 최근 실행 이력을 한 화면에서 확인할 수 있도록 구성했습니다.
              </p>
              <div className="mt-6 grid gap-3 sm:grid-cols-3">
                <div className="rounded-[24px] border border-border bg-white/72 px-4 py-4">
                  <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-foreground/45">Managed Jobs</p>
                  <p className="mt-2 text-lg font-semibold">{jobs.length}</p>
                </div>
                <div className="rounded-[24px] border border-border bg-white/72 px-4 py-4">
                  <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-foreground/45">Paused Schedules</p>
                  <p className="mt-2 text-lg font-semibold">{pausedCount}</p>
                </div>
                <div className="rounded-[24px] border border-border bg-white/72 px-4 py-4">
                  <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-foreground/45">Running</p>
                  <p className="mt-2 text-lg font-semibold">{runningCount}</p>
                </div>
              </div>
            </div>
            <div className="border-t border-border/70 bg-foreground px-6 py-6 text-white lg:border-l lg:border-t-0">
              <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-white/60">JDBC Metadata</p>
              <div className="mt-4 space-y-3">
                <div className="rounded-[24px] border border-white/10 bg-white/8 px-4 py-4">
                  <div className="flex items-center gap-3">
                    <DatabaseZap className="h-4 w-4 text-white/70" />
                    <p className="text-sm font-semibold">Spring Batch 메타 테이블</p>
                  </div>
                  <p className="mt-3 text-sm leading-6 text-white/72">실행 이력과 JobExecution, StepExecution 상태를 JDBC로 저장해 운영 이력을 재확인할 수 있습니다.</p>
                </div>
                <div className="rounded-[24px] border border-white/10 bg-white/8 px-4 py-4">
                  <div className="flex items-center gap-3">
                    <CalendarClock className="h-4 w-4 text-white/70" />
                    <p className="text-sm font-semibold">Quartz JDBC JobStore</p>
                  </div>
                  <p className="mt-3 text-sm leading-6 text-white/72">리포트 생성 스케줄은 Quartz JDBC 테이블에서 관리되며 화면에서 pause와 resume을 직접 적용할 수 있습니다.</p>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardDescription>Quick Paths</CardDescription>
            <CardTitle>운영 흐름 바로가기</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <Link className="flex items-start gap-3 rounded-[24px] border border-border bg-white/76 px-4 py-4 transition hover:border-primary" href="/dashboard">
              <ActivitySquare className="mt-0.5 h-4 w-4 text-primary" />
              <div>
                <p className="text-sm font-semibold text-foreground">대시보드</p>
                <p className="mt-1 text-sm leading-6 text-foreground/65">시장 레짐과 리포트 최신 상태를 먼저 점검합니다.</p>
              </div>
            </Link>
            <Link className="flex items-start gap-3 rounded-[24px] border border-border bg-white/76 px-4 py-4 transition hover:border-primary" href="/reports/today">
              <CalendarClock className="mt-0.5 h-4 w-4 text-primary" />
              <div>
                <p className="text-sm font-semibold text-foreground">오늘의 리포트</p>
                <p className="mt-1 text-sm leading-6 text-foreground/65">배치 실행 후 실제 생성 결과를 바로 검증합니다.</p>
              </div>
            </Link>
          </CardContent>
        </Card>
      </section>

      <section className="grid gap-6">
        {jobs.map((job) => {
          const isMutating =
            (runMutation.isPending && runMutation.variables === job.jobName)
            || (pauseMutation.isPending && pauseMutation.variables === job.jobName)
            || (resumeMutation.isPending && resumeMutation.variables === job.jobName);
          const isSelected = selectedJobName === job.jobName;

          return (
            <Card
              key={job.jobName}
              className={isSelected ? "border-primary/60 shadow-[0_18px_50px_rgba(15,23,42,0.08)]" : undefined}
            >
              <CardHeader>
                <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                  <button
                    type="button"
                    className="text-left"
                    onClick={() => setSelectedJobName(job.jobName)}
                  >
                    <div className="flex flex-wrap items-center gap-2">
                      <CardTitle>{job.title}</CardTitle>
                      <Badge className={getScheduleTone(job.scheduleState)}>{job.scheduleState}</Badge>
                      {job.running && <Badge className="bg-accent/12 text-accent">RUNNING</Badge>}
                    </div>
                    <CardDescription className="mt-3">{job.description}</CardDescription>
                  </button>
                  <div className="flex flex-wrap gap-2">
                    <Button disabled={isMutating || job.running} onClick={() => runMutation.mutate(job.jobName)}>
                      <RefreshCcw className="mr-2 h-4 w-4" />
                      {runMutation.isPending && runMutation.variables === job.jobName ? "실행 중..." : "지금 실행"}
                    </Button>
                    {job.schedulable && job.scheduleState !== "PAUSED" && (
                      <Button variant="outline" disabled={isMutating} onClick={() => pauseMutation.mutate(job.jobName)}>
                        <Pause className="mr-2 h-4 w-4" />
                        Pause
                      </Button>
                    )}
                    {job.schedulable && job.scheduleState === "PAUSED" && (
                      <Button variant="outline" disabled={isMutating} onClick={() => resumeMutation.mutate(job.jobName)}>
                        <Play className="mr-2 h-4 w-4" />
                        Resume
                      </Button>
                    )}
                  </div>
                </div>
              </CardHeader>
              <CardContent className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_420px]">
                <div className="grid gap-3 md:grid-cols-3">
                  <div className="rounded-[24px] border border-border bg-white/76 px-4 py-4">
                    <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-foreground/45">Next Fire</p>
                    <p className="mt-2 text-sm font-semibold text-foreground">{formatDateTime(job.nextFireTime)}</p>
                    <p className="mt-2 text-sm leading-6 text-foreground/60">{job.zoneId ?? "수동 실행 전용 작업"}</p>
                  </div>
                  <div className="rounded-[24px] border border-border bg-white/76 px-4 py-4">
                    <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-foreground/45">Previous Fire</p>
                    <p className="mt-2 text-sm font-semibold text-foreground">{formatDateTime(job.previousFireTime)}</p>
                    <p className="mt-2 text-sm leading-6 text-foreground/60">{job.cronExpression ?? "Quartz 트리거 없음"}</p>
                  </div>
                  <div className="rounded-[24px] border border-border bg-white/76 px-4 py-4">
                    <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-foreground/45">Last Execution</p>
                    <div className="mt-2 flex flex-wrap items-center gap-2">
                      <Badge className={getExecutionTone(job.lastExecution)}>{job.lastExecution?.status ?? "NO_HISTORY"}</Badge>
                    </div>
                    <p className="mt-2 text-sm leading-6 text-foreground/60">{formatDateTime(job.lastExecution?.startedAt ?? null)}</p>
                  </div>
                </div>

                <div className="rounded-[28px] border border-border bg-muted/55 p-4">
                  <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-accent">Recent Executions</p>
                  <div className="mt-4 space-y-3">
                    {job.recentExecutions.map((execution) => (
                      <div key={execution.executionId} className="rounded-[22px] border border-border/80 bg-white/82 px-4 py-4">
                        <div className="flex flex-wrap items-center justify-between gap-3">
                          <div className="flex items-center gap-2">
                            <Badge className={getExecutionTone(execution)}>{execution.status}</Badge>
                            <span className="text-sm font-semibold text-foreground">#{execution.executionId}</span>
                          </div>
                          <span className="text-xs text-foreground/50">{execution.exitCode}</span>
                        </div>
                        <p className="mt-3 text-sm leading-6 text-foreground/65">
                          시작 {formatDateTime(execution.startedAt)} / 종료 {formatDateTime(execution.endedAt)}
                        </p>
                        {execution.exitDescription && (
                          <p className="mt-2 text-sm leading-6 text-foreground/60">{execution.exitDescription}</p>
                        )}
                      </div>
                    ))}
                    {job.recentExecutions.length === 0 && (
                      <p className="rounded-[22px] border border-border/80 bg-white/82 px-4 py-4 text-sm text-foreground/60">
                        아직 기록된 실행 이력이 없습니다.
                      </p>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>
          );
        })}
      </section>

      <section className="grid gap-6 xl:grid-cols-[340px_minmax(0,1fr)]">
        <Card>
          <CardHeader>
            <CardDescription>JDBC Summary</CardDescription>
            <CardTitle>선택된 배치 메타데이터</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {selectedMetadata ? (
              <>
                <div className="rounded-[24px] border border-border bg-white/76 px-4 py-4">
                  <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-foreground/45">Execution Count</p>
                  <p className="mt-2 text-2xl font-semibold text-foreground">{selectedMetadata.metrics.totalExecutions}</p>
                  <p className="mt-2 text-sm text-foreground/60">
                    성공 {selectedMetadata.metrics.completedExecutions} / 실패 {selectedMetadata.metrics.failedExecutions} / 실행 중 {selectedMetadata.metrics.runningExecutions}
                  </p>
                </div>
                <div className="rounded-[24px] border border-border bg-white/76 px-4 py-4">
                  <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-foreground/45">Last Successful Run</p>
                  <p className="mt-2 text-sm font-semibold text-foreground">{formatDateTime(selectedMetadata.metrics.lastSuccessfulAt)}</p>
                </div>
                <div className="rounded-[24px] border border-border bg-white/76 px-4 py-4">
                  <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-foreground/45">Quartz Trigger</p>
                  <p className="mt-2 text-sm font-semibold text-foreground">
                    {selectedMetadata.quartzTrigger?.triggerState ?? "MANUAL_ONLY"}
                  </p>
                  <p className="mt-2 text-sm leading-6 text-foreground/60">
                    {selectedMetadata.quartzTrigger?.cronExpression ?? "수동 실행 전용 작업"}
                  </p>
                  <p className="mt-2 text-sm leading-6 text-foreground/60">
                    다음 실행 {formatDateTime(selectedMetadata.quartzTrigger?.nextFireTime ?? null)}
                  </p>
                </div>
              </>
            ) : batchMetadataQuery.isLoading ? (
              <p className="text-sm text-foreground/60">선택한 배치의 JDBC 메타데이터를 읽는 중입니다.</p>
            ) : (
              <p className="text-sm text-foreground/60">{selectedMetadataMessage}</p>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardDescription>Execution Detail</CardDescription>
            <CardTitle>JobExecution / StepExecution 상세</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {selectedMetadata?.executions.map((execution) => (
              <ExecutionDetailCard key={execution.executionId} execution={execution} />
            ))}
            {selectedMetadata && selectedMetadata.executions.length === 0 && (
              <p className="rounded-[22px] border border-border bg-white/82 px-4 py-4 text-sm text-foreground/60">
                아직 JDBC 메타데이터에 기록된 실행 이력이 없습니다.
              </p>
            )}
            {!selectedMetadata && batchMetadataQuery.isLoading && (
              <p className="rounded-[22px] border border-border bg-white/82 px-4 py-4 text-sm text-foreground/60">
                실행 상세를 불러오는 중입니다.
              </p>
            )}
            {!selectedMetadata && batchMetadataQuery.isError && (
              <p className="rounded-[22px] border border-border bg-white/82 px-4 py-4 text-sm text-danger">
                {selectedMetadataMessage}
              </p>
            )}
          </CardContent>
        </Card>
      </section>
    </div>
  );
}

function ExecutionDetailCard({ execution }: { execution: BatchExecutionDetail }) {
  return (
    <div className="rounded-[24px] border border-border bg-white/82 p-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <Badge className={getExecutionTone(execution)}>{execution.status}</Badge>
          <span className="text-sm font-semibold text-foreground">#{execution.executionId}</span>
        </div>
        <span className="text-xs text-foreground/50">소요 시간 {formatDuration(execution.durationSeconds)}</span>
      </div>
      <div className="mt-3 grid gap-3 md:grid-cols-3">
        <div>
          <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-foreground/45">Created</p>
          <p className="mt-2 text-sm text-foreground/70">{formatDateTime(execution.createdAt)}</p>
        </div>
        <div>
          <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-foreground/45">Started</p>
          <p className="mt-2 text-sm text-foreground/70">{formatDateTime(execution.startedAt)}</p>
        </div>
        <div>
          <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-foreground/45">Ended</p>
          <p className="mt-2 text-sm text-foreground/70">{formatDateTime(execution.endedAt)}</p>
        </div>
      </div>
      {Object.keys(execution.parameters).length > 0 && (
        <div className="mt-4 rounded-[20px] border border-border/80 bg-muted/45 px-4 py-4">
          <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-accent">Parameters</p>
          <div className="mt-3 grid gap-2 md:grid-cols-2">
            {Object.entries(execution.parameters).map(([key, value]) => (
              <div key={key} className="rounded-[16px] bg-white/90 px-3 py-3">
                <p className="text-[11px] font-semibold uppercase tracking-[0.12em] text-foreground/45">{key}</p>
                <p className="mt-1 text-sm text-foreground/72">{value}</p>
              </div>
            ))}
          </div>
        </div>
      )}
      <div className="mt-4 space-y-3">
        {execution.stepExecutions.map((step) => (
          <div key={step.stepExecutionId} className="rounded-[20px] border border-border/80 bg-muted/35 px-4 py-4">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <div className="flex items-center gap-2">
                <Badge className={step.status === "COMPLETED" ? "bg-success/10 text-success" : "bg-danger/12 text-danger"}>{step.status}</Badge>
                <span className="text-sm font-semibold text-foreground">{step.stepName}</span>
              </div>
              <span className="text-xs text-foreground/50">#{step.stepExecutionId}</span>
            </div>
            <div className="mt-3 grid gap-3 md:grid-cols-4">
              <MetricCell label="Read" value={String(step.readCount)} />
              <MetricCell label="Write" value={String(step.writeCount)} />
              <MetricCell label="Commit" value={String(step.commitCount)} />
              <MetricCell label="Rollback" value={String(step.rollbackCount)} />
            </div>
            <p className="mt-3 text-sm leading-6 text-foreground/65">
              시작 {formatDateTime(step.startedAt)} / 종료 {formatDateTime(step.endedAt)}
            </p>
            {step.exitDescription && <p className="mt-2 text-sm leading-6 text-foreground/60">{step.exitDescription}</p>}
          </div>
        ))}
      </div>
    </div>
  );
}

function MetricCell({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-[16px] border border-border/70 bg-white/84 px-3 py-3">
      <p className="text-[11px] font-semibold uppercase tracking-[0.14em] text-foreground/45">{label}</p>
      <p className="mt-2 text-sm font-semibold text-foreground">{value}</p>
    </div>
  );
}
