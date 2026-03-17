export type BatchExecution = {
  executionId: number;
  status: string;
  exitCode: string;
  exitDescription: string;
  startedAt: string | null;
  endedAt: string | null;
};

export type BatchJobStatus = {
  jobName: string;
  title: string;
  description: string;
  schedulable: boolean;
  scheduleState: "MANUAL_ONLY" | "SCHEDULED" | "PAUSED" | "COMPLETED" | "ERROR" | "NOT_FOUND";
  cronExpression: string | null;
  zoneId: string | null;
  nextFireTime: string | null;
  previousFireTime: string | null;
  running: boolean;
  lastExecution: BatchExecution | null;
  recentExecutions: BatchExecution[];
};

export type BatchStepExecution = {
  stepExecutionId: number;
  stepName: string;
  status: string;
  exitCode: string;
  exitDescription: string;
  readCount: number;
  writeCount: number;
  commitCount: number;
  rollbackCount: number;
  startedAt: string | null;
  endedAt: string | null;
  lastUpdatedAt: string | null;
};

export type BatchExecutionDetail = {
  executionId: number;
  status: string;
  exitCode: string;
  exitDescription: string;
  createdAt: string | null;
  startedAt: string | null;
  endedAt: string | null;
  lastUpdatedAt: string | null;
  durationSeconds: number | null;
  parameters: Record<string, string>;
  stepExecutions: BatchStepExecution[];
};

export type BatchJdbcMetrics = {
  totalExecutions: number;
  completedExecutions: number;
  failedExecutions: number;
  runningExecutions: number;
  lastSuccessfulAt: string | null;
  lastFailedAt: string | null;
};

export type QuartzTriggerMetadata = {
  triggerName: string;
  triggerGroup: string;
  triggerState: string;
  cronExpression: string | null;
  zoneId: string | null;
  startTime: string | null;
  nextFireTime: string | null;
  previousFireTime: string | null;
  endTime: string | null;
};

export type BatchJobMetadata = {
  jobName: string;
  title: string;
  description: string;
  metrics: BatchJdbcMetrics;
  quartzTrigger: QuartzTriggerMetadata | null;
  executions: BatchExecutionDetail[];
};
