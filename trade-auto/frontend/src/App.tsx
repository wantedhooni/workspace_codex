import { Route, Routes, NavLink } from 'react-router-dom'
import Dashboard from './pages/Dashboard'
import Tickers from './pages/Tickers'
import TickerDetail from './pages/TickerDetail'
import Backtest from './pages/Backtest'
import Quartz from './pages/Quartz'

export default function App() {
  return (
    <div className="app">
      <aside className="sidebar">
        <div className="brand">Trade Auto</div>
        <nav>
          <NavLink to="/" end>Dashboard</NavLink>
          <NavLink to="/tickers">Tickers</NavLink>
          <NavLink to="/backtest">Backtest</NavLink>
          <NavLink to="/quartz">Quartz</NavLink>
        </nav>
      </aside>
      <main className="content">
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/tickers" element={<Tickers />} />
          <Route path="/tickers/:symbol" element={<TickerDetail />} />
          <Route path="/backtest" element={<Backtest />} />
          <Route path="/quartz" element={<Quartz />} />
        </Routes>
      </main>
    </div>
  )
}
