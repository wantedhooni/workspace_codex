import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { fetchSignalsBySymbol } from '../api'
import { Signal } from '../types'

export default function TickerDetail() {
  const { symbol } = useParams()
  const [signals, setSignals] = useState<Signal[]>([])

  useEffect(() => {
    if (!symbol) return
    fetchSignalsBySymbol(symbol).then(setSignals)
  }, [symbol])

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>{symbol}</h1>
          <p>Signal history</p>
        </div>
      </header>

      <section className="panel">
        <div className="table">
          <div className="table-row cols-7 table-head">
            <div>Date</div>
            <div>Action</div>
            <div>Score</div>
            <div>Entry</div>
            <div>Stop</div>
            <div>Target</div>
            <div>Rationale</div>
          </div>
          {signals.map(s => (
            <div className="table-row cols-7" key={s.id}>
              <div>{s.signalDate}</div>
              <div className={`pill ${s.action.toLowerCase()}`}>{s.action}</div>
              <div>{s.score.toFixed(1)}</div>
              <div>${s.entryPrice.toFixed(2)}</div>
              <div>${s.stopLoss.toFixed(2)}</div>
              <div>${s.takeProfit.toFixed(2)}</div>
              <div className="muted">{s.rationale}</div>
            </div>
          ))}
          {signals.length === 0 && (
            <div className="empty">No signals yet for this ticker.</div>
          )}
        </div>
      </section>
    </div>
  )
}
