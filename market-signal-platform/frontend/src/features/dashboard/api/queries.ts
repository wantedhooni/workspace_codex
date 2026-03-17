"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "@/lib/api-client";
import type { DailyReport } from "@/features/report/types/report";

export function useTodayReport() {
  return useQuery({
    queryKey: ["today-report"],
    queryFn: () => apiClient.request<DailyReport>("/reports/today"),
  });
}

export function useRecentReports(limit = 5) {
  return useQuery({
    queryKey: ["recent-reports", limit],
    queryFn: () => apiClient.request<DailyReport[]>(`/reports/recent?limit=${limit}`),
  });
}

export function useGenerateReport() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => apiClient.request<DailyReport>("/signals/generate", { method: "POST" }),
    onSuccess: (report) => {
      queryClient.setQueryData(["today-report"], report);
      queryClient.invalidateQueries({ queryKey: ["recent-reports"] });
    },
  });
}
