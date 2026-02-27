import { useEffect, useMemo, useState } from 'react'
import { fetchLatestSignals, fetchTickers, generateSignals, refreshMarketData, seedTickers } from '../api'
import { Signal, Ticker } from '../types'

export default function Dashboard() {
  const [tickers, setTickers] = useState<Ticker[]>([])
  const [signals, setSignals] = useState<Signal[]>([])
  const [status, setStatus] = useState<string>('')
  const [loading, setLoading] = useState(false)

  const reload = async () => {
    const [t, s] = await Promise.all([fetchTickers(), fetchLatestSignals()])
    setTickers(t)
    setSignals(s)
  }

  useEffect(() => {
    reload()
  }, [])

  const handleSeed = async () => {
    setLoading(true)
    const res = await seedTickers()
    setStatus(res)
    await reload()
    setLoading(false)
  }

  const handleRefresh = async () => {
    setLoading(true)
    const res = await refreshMarketData(365)
    setStatus(res)
    setLoading(false)
  }

  const handleGenerate = async () => {
    setLoading(true)
    const res = await generateSignals()
    setSignals(res)
    setStatus(`generated ${res.length} signals`)
    setLoading(false)
  }

  const buySignals = useMemo(() => signals.filter(s => s.action === 'BUY'), [signals])
  const holdSignals = useMemo(() => signals.filter(s => s.action === 'HOLD'), [signals])
  const sellSignals = useMemo(() => signals.filter(s => s.action === 'SELL'), [signals])

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>US Market Signal Desk</h1>
          <p>Daily EOD scan + timing recommendation</p>
        </div>
        <div className="actions">
          <button disabled={loading} onClick={handleSeed}>Seed Tickers</button>
          <button disabled={loading} onClick={handleRefresh}>Refresh Data</button>
          <button disabled={loading} onClick={handleGenerate}>Generate Signals</button>
        </div>
      </header>

      {status && <div className="status">{status}</div>}

      <section className="stats">
        <div className="stat">
          <div className="label">Active Tickers</div>
          <div className="value">{tickers.filter(t => t.active).length}</div>
        </div>
        <div className="stat">
          <div className="label">Buy</div>
          <div className="value">{buySignals.length}</div>
        </div>
        <div className="stat">
          <div className="label">Hold</div>
          <div className="value">{holdSignals.length}</div>
        </div>
        <div className="stat">
          <div className="label">Sell</div>
          <div className="value">{sellSignals.length}</div>
        </div>
      </section>

      <section className="panel">
        <div className="panel-header">
          <h2>Latest Signals</h2>
          <span>Sorted by score</span>
        </div>
        <div className="table">
          <div className="table-row cols-7 table-head">
            <div>Symbol</div>
            <div>Action</div>
            <div>Score</div>
            <div>Entry</div>
            <div>Stop</div>
            <div>Target</div>
            <div>Rationale</div>
          </div>
          {signals
            .slice()
            .sort((a, b) => b.score - a.score)
            .map((s) => (
              <div className="table-row cols-7" key={s.id}>
                <div>{s.symbol}</div>
                <div className={`pill ${s.action.toLowerCase()}`}>{s.action}</div>
                <div>{s.score.toFixed(1)}</div>
                <div>${s.entryPrice.toFixed(2)}</div>
                <div>${s.stopLoss.toFixed(2)}</div>
                <div>${s.takeProfit.toFixed(2)}</div>
                <div className="muted">{s.rationale}</div>
              </div>
            ))}
          {signals.length === 0 && (
            <div className="empty">No signals yet. Seed tickers, refresh data, and generate.</div>
          )}
        </div>
      </section>
    </div>
  )
}
