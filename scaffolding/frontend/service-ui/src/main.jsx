import React from 'react'
import { createRoot } from 'react-dom/client'

const App = () => {
  return (
    <div style={{ fontFamily: 'system-ui, sans-serif', padding: '2rem' }}>
      <h1>Service UI</h1>
      <p>Service frontend is running.</p>
    </div>
  )
}

createRoot(document.getElementById('root')).render(<App />)
