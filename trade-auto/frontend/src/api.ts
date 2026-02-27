import axios from 'axios'
import { BacktestRun, Signal, Ticker } from './types'

const baseURL = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

const api = axios.create({ baseURL })

type ApiResponse<T> = {
  success: boolean
  data: T
  message?: string | null
}

export const setAdminToken = (token: string) => {
  api.defaults.headers.common['Authorization'] = `Bearer ${token}`
}

export const clearAdminAuth = () => {
  delete api.defaults.headers.common['Authorization']
}

const unwrap = <T>(res: { data: ApiResponse<T> }): T => {
  if (!res.data.success) {
    throw new Error(res.data.message || 'Request failed')
  }
  return res.data.data
}

export const fetchTickers = async (): Promise<Ticker[]> => {
  const res = await api.get('/api/v1/tickers')
  return unwrap(res)
}

export const seedTickers = async (): Promise<string> => {
  const res = await api.post('/api/v1/tickers/seed')
  return unwrap(res)
}

export const toggleTicker = async (symbol: string): Promise<Ticker> => {
  const res = await api.post(`/api/v1/tickers/${symbol}/toggle`)
  return unwrap(res)
}

export const refreshMarketData = async (days = 365): Promise<string> => {
  const res = await api.post(`/api/v1/marketdata/refresh?days=${days}`)
  return unwrap(res)
}

export const generateSignals = async (): Promise<Signal[]> => {
  const res = await api.post('/api/v1/signals/generate')
  return unwrap(res)
}

export const fetchLatestSignals = async (): Promise<Signal[]> => {
  const res = await api.get('/api/v1/signals/latest')
  return unwrap(res)
}

export const fetchSignalsBySymbol = async (symbol: string): Promise<Signal[]> => {
  const res = await api.get(`/api/v1/signals/${symbol}`)
  return unwrap(res)
}

export const runBacktest = async (startDate?: string, endDate?: string): Promise<BacktestRun> => {
  const res = await api.post('/api/v1/backtest/run', { startDate, endDate })
  return unwrap(res)
}

export const fetchBacktestRuns = async (): Promise<BacktestRun[]> => {
  const res = await api.get('/api/v1/backtest/runs')
  return unwrap(res)
}

export const fetchBacktestRun = async (id: number): Promise<BacktestRun> => {
  const res = await api.get(`/api/v1/backtest/runs/${id}`)
  return unwrap(res)
}

export type QuartzJob = {
  name: string
  group: string
  description?: string | null
  durable: boolean
}

export type QuartzTrigger = {
  name: string
  group: string
  jobName: string
  jobGroup: string
  type: string
  cron?: string | null
  timeZone?: string | null
  nextFireTime?: string | null
  prevFireTime?: string | null
  state?: string | null
}

export type JobRun = {
  id: number
  jobName: string
  status: string
  startedAt: string
  endedAt?: string | null
  message?: string | null
}

export const adminLogin = async (username: string, password: string) => {
  const res = await api.post('/api/v1/auth/login', { username, password })
  return unwrap(res) as { token: string; tokenType: string }
}

export const fetchQuartzJobs = async (): Promise<QuartzJob[]> => {
  const res = await api.get('/api/v1/quartz/jobs')
  return unwrap(res)
}

export const createQuartzJob = async (payload: {
  name: string
  group: string
  className: string
  description?: string
  durable?: boolean
}) => {
  const res = await api.post('/api/v1/quartz/jobs', payload)
  return unwrap(res)
}

export const deleteQuartzJob = async (group: string, name: string) => {
  const res = await api.delete(`/api/v1/quartz/jobs/${group}/${name}`)
  return unwrap(res)
}

export const fetchQuartzTriggers = async (): Promise<QuartzTrigger[]> => {
  const res = await api.get('/api/v1/quartz/triggers')
  return unwrap(res)
}

export const updateQuartzTrigger = async (group: string, name: string, cron: string, timeZone?: string) => {
  const res = await api.put(`/api/v1/quartz/triggers/${group}/${name}`, { cron, timeZone })
  return unwrap(res)
}

export const createQuartzTrigger = async (payload: {
  jobName: string
  jobGroup: string
  triggerName: string
  triggerGroup: string
  cron: string
  timeZone?: string
}) => {
  const res = await api.post('/api/v1/quartz/triggers', payload)
  return unwrap(res)
}

export const deleteQuartzTrigger = async (group: string, name: string) => {
  const res = await api.delete(`/api/v1/quartz/triggers/${group}/${name}`)
  return unwrap(res)
}

export const pauseQuartzTrigger = async (group: string, name: string) => {
  const res = await api.post(`/api/v1/quartz/triggers/${group}/${name}/pause`)
  return unwrap(res)
}

export const resumeQuartzTrigger = async (group: string, name: string) => {
  const res = await api.post(`/api/v1/quartz/triggers/${group}/${name}/resume`)
  return unwrap(res)
}

export const runQuartzJob = async (group: string, name: string) => {
  const res = await api.post(`/api/v1/quartz/jobs/${group}/${name}/run`)
  return unwrap(res)
}

export const fetchJobRuns = async (jobName: string): Promise<JobRun[]> => {
  const res = await api.get(`/api/v1/jobs/runs?jobName=${encodeURIComponent(jobName)}`)
  return unwrap(res)
}

export type QuartzAudit = {
  id: number
  action: string
  target: string
  detail?: string | null
  actor: string
  createdAt: string
}

export const fetchQuartzAudit = async (): Promise<QuartzAudit[]> => {
  const res = await api.get('/api/v1/quartz/audit')
  return unwrap(res)
}
