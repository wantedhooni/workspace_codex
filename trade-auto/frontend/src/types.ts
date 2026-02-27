export type Ticker = {
  id: number
  symbol: string
  name: string
  sector: string
  active: boolean
}

export type Signal = {
  id: number
  symbol: string
  name: string
  signalDate: string
  action: 'BUY' | 'SELL' | 'HOLD'
  score: number
  entryPrice: number
  stopLoss: number
  takeProfit: number
  rationale: string
}

export type BacktestTrade = {
  id: number
  symbol: string
  entryDate: string
  exitDate: string
  entryPrice: number
  exitPrice: number
  pnlPct: number
  exitReason: string
}

export type BacktestRun = {
  id: number
  strategyName: string
  startDate: string
  endDate: string
  totalReturnPct: number
  maxDrawdownPct: number
  winRatePct: number
  trades: number
  createdAt: string
  tradeList: BacktestTrade[]
}
