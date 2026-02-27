import { useEffect, useMemo, useState } from 'react'
import { fetchBacktestRun, fetchBacktestRuns, runBacktest } from '../api'
import { BacktestRun } from '../types'

export default function Backtest() {
  const [runs, setRuns] = useState<BacktestRun[]>([])
  const [selected, setSelected] = useState<BacktestRun | null>(null)
  const [startDate, setStartDate] = useState<string>('')
  const [endDate, setEndDate] = useState<string>('')
  const [loading, setLoading] = useState(false)

  const load = async () => {
    const data = await fetchBacktestRuns()
    setRuns(data)
    if (data.length > 0) {
      const detail = await fetchBacktestRun(data[data.length - 1].id)
      setSelected(detail)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const handleRun = async () => {
    setLoading(true)
    const run = await runBacktest(startDate || undefined, endDate || undefined)
    await load()
    setSelected(run)
    setLoading(false)
  }

  const stats = useMemo(() => {
    if (!selected) return null
    return [
      { label: 'Total Return', value: `${selected.totalReturnPct.toFixed(2)}%` },
      { label: 'Max Drawdown', value: `${selected.maxDrawdownPct.toFixed(2)}%` },
      { label: 'Win Rate', value: `${selected.winRatePct.toFixed(2)}%` },
      { label: 'Trades', value: selected.trades }
    ]
  }, [selected])

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>Backtest</h1>
          <p>Run historical simulation for the current strategy</p>
        </div>
        <div className="actions">
          <input
            type="date"
            value={startDate}
            onChange={e => setStartDate(e.target.value)}
          />
          <input
            type="date"
            value={endDate}
            onChange={e => setEndDate(e.target.value)}
          />
          <button disabled={loading} onClick={handleRun}>Run Backtest</button>
        </div>
      </header>

      <section className="panel">
        <div className="panel-header">
          <h2>Runs</h2>
        </div>
        <div className="table">
          <div className="table-row cols-6 table-head">
            <div>ID</div>
            <div>Period</div>
            <div>Return</div>
            <div>DD</div>
            <div>Win</div>
            <div>Trades</div>
          </div>
          {runs.map(r => (
            <div className="table-row cols-6" key={r.id} onClick={() => fetchBacktestRun(r.id).then(setSelected)}>
              <div>#{r.id}</div>
              <div>{r.startDate} → {r.endDate}</div>
              <div>{r.totalReturnPct.toFixed(2)}%</div>
              <div>{r.maxDrawdownPct.toFixed(2)}%</div>
              <div>{r.winRatePct.toFixed(2)}%</div>
              <div>{r.trades}</div>
            </div>
          ))}
          {runs.length === 0 && (
            <div className="empty">No runs yet. Click Run Backtest.</div>
          )}
        </div>
      </section>

      {selected && stats && (
        <section className="stats">
          {stats.map(s => (
            <div className="stat" key={s.label}>
              <div className="label">{s.label}</div>
              <div className="value">{s.value}</div>
            </div>
          ))}
        </section>
      )}

      {selected && (
        <section className="panel">
          <div className="panel-header">
            <h2>Trades</h2>
            <span>{selected.trades} trades</span>
          </div>
          <div className="table">
            <div className="table-row cols-7 table-head">
              <div>Symbol</div>
              <div>Entry</div>
              <div>Exit</div>
              <div>Entry Px</div>
              <div>Exit Px</div>
              <div>PnL</div>
              <div>Reason</div>
            </div>
            {selected.tradeList.map(t => (
              <div className="table-row cols-7" key={t.id}>
                <div>{t.symbol}</div>
                <div>{t.entryDate}</div>
                <div>{t.exitDate}</div>
                <div>${t.entryPrice.toFixed(2)}</div>
                <div>${t.exitPrice.toFixed(2)}</div>
                <div className={t.pnlPct >= 0 ? 'good' : 'bad'}>{t.pnlPct.toFixed(2)}%</div>
                <div className="muted">{t.exitReason}</div>
              </div>
            ))}
            {selected.tradeList.length === 0 && (
              <div className="empty">No trades for this run.</div>
            )}
          </div>
        </section>
      )}
    </div>
  )
}
