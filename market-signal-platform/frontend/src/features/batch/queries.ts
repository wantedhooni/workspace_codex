"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@/lib/api-client";
import type { BatchJobMetadata, BatchJobStatus } from "@/types/batch";

export function useBatchJobs() {
  return useQuery({
    queryKey: ["batch-jobs"],
    queryFn: () => apiClient.request<BatchJobStatus[]>("/batch/jobs"),
    refetchInterval: 30000,
  });
}

export function useBatchJobMetadata(jobName: string | null) {
  return useQuery({
    queryKey: ["batch-job-metadata", jobName],
    queryFn: () => apiClient.request<BatchJobMetadata>(`/batch/jobs/${jobName}/metadata?limit=10`),
    enabled: Boolean(jobName),
    refetchInterval: 30000,
  });
}

export function useRunBatchJob() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (jobName: string) => apiClient.request<BatchJobStatus>(`/batch/jobs/${jobName}/run`, { method: "POST" }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["batch-jobs"] });
      queryClient.invalidateQueries({ queryKey: ["batch-job-metadata"] });
      queryClient.invalidateQueries({ queryKey: ["today-report"] });
      queryClient.invalidateQueries({ queryKey: ["recent-reports"] });
    },
  });
}

export function usePauseBatchJob() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (jobName: string) => apiClient.request<BatchJobStatus>(`/batch/jobs/${jobName}/pause`, { method: "POST" }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["batch-jobs"] });
      queryClient.invalidateQueries({ queryKey: ["batch-job-metadata"] });
    },
  });
}

export function useResumeBatchJob() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (jobName: string) => apiClient.request<BatchJobStatus>(`/batch/jobs/${jobName}/resume`, { method: "POST" }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["batch-jobs"] });
      queryClient.invalidateQueries({ queryKey: ["batch-job-metadata"] });
    },
  });
}
