import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchTickers, toggleTicker } from '../api'
import { Ticker } from '../types'

export default function Tickers() {
  const [tickers, setTickers] = useState<Ticker[]>([])

  const load = async () => {
    const data = await fetchTickers()
    setTickers(data)
  }

  useEffect(() => {
    load()
  }, [])

  const handleToggle = async (symbol: string) => {
    await toggleTicker(symbol)
    await load()
  }

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>Tickers</h1>
          <p>Universe management</p>
        </div>
      </header>

      <section className="panel">
        <div className="table">
          <div className="table-row cols-5 table-head">
            <div>Symbol</div>
            <div>Name</div>
            <div>Sector</div>
            <div>Status</div>
            <div>Action</div>
          </div>
          {tickers.map(t => (
            <div className="table-row cols-5" key={t.id}>
              <div><Link to={`/tickers/${t.symbol}`}>{t.symbol}</Link></div>
              <div>{t.name}</div>
              <div className="muted">{t.sector}</div>
              <div>{t.active ? 'Active' : 'Paused'}</div>
              <div>
                <button className="ghost" onClick={() => handleToggle(t.symbol)}>
                  {t.active ? 'Pause' : 'Activate'}
                </button>
              </div>
            </div>
          ))}
          {tickers.length === 0 && (
            <div className="empty">No tickers yet. Seed from the dashboard.</div>
          )}
        </div>
      </section>
    </div>
  )
}
