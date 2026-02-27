import { useEffect, useState } from 'react'
import {
  adminLogin,
  clearAdminAuth,
  createQuartzTrigger,
  createQuartzJob,
  fetchJobRuns,
  fetchQuartzJobs,
  fetchQuartzTriggers,
  pauseQuartzTrigger,
  QuartzJob,
  QuartzTrigger,
  resumeQuartzTrigger,
  runQuartzJob,
  setAdminToken,
  updateQuartzTrigger,
  deleteQuartzTrigger,
  deleteQuartzJob,
  JobRun,
  fetchQuartzAudit,
  QuartzAudit
} from '../api'

export default function Quartz() {
  const [jobs, setJobs] = useState<QuartzJob[]>([])
  const [triggers, setTriggers] = useState<QuartzTrigger[]>([])
  const [runs, setRuns] = useState<JobRun[]>([])
  const [audit, setAudit] = useState<QuartzAudit[]>([])
  const [status, setStatus] = useState<string>('')
  const [editing, setEditing] = useState<Record<string, { cron: string; timeZone: string }>>({})
  const [login, setLogin] = useState({ user: '', pass: '' })
  const [createForm, setCreateForm] = useState({
    jobName: 'dailyJob',
    jobGroup: 'DEFAULT',
    triggerName: 'dailyTrigger',
    triggerGroup: 'DEFAULT',
    cron: '0 20 18 * * ?',
    timeZone: 'America/New_York'
  })

  const [jobForm, setJobForm] = useState({
    name: 'dailyJob',
    group: 'DEFAULT',
    className: 'com.tradeauto.service.DailyQuartzJob',
    description: 'Daily scan job',
    durable: true
  })

  const load = async () => {
    try {
      const [j, t, r, a] = await Promise.all([
        fetchQuartzJobs(),
        fetchQuartzTriggers(),
        fetchJobRuns('daily-scan'),
        fetchQuartzAudit()
      ])
      setJobs(j)
      setTriggers(t)
      setRuns(r)
      setAudit(a)
    } catch (err) {
      setStatus('Admin auth required or request failed')
    }
  }

  useEffect(() => {
    load()
  }, [])

  const key = (t: QuartzTrigger) => `${t.group}::${t.name}`

  const updateField = (t: QuartzTrigger, field: 'cron' | 'timeZone', value: string) => {
    const k = key(t)
    setEditing(prev => ({
      ...prev,
      [k]: {
        cron: field === 'cron' ? value : (prev[k]?.cron ?? t.cron ?? ''),
        timeZone: field === 'timeZone' ? value : (prev[k]?.timeZone ?? t.timeZone ?? 'UTC')
      }
    }))
  }

  const handleUpdate = async (t: QuartzTrigger) => {
    const k = key(t)
    const cron = editing[k]?.cron || t.cron || ''
    const timeZone = editing[k]?.timeZone || t.timeZone || 'UTC'
    await updateQuartzTrigger(t.group, t.name, cron, timeZone)
    setStatus(`updated ${t.group}/${t.name}`)
    await load()
  }

  const handleCreate = async () => {
    await createQuartzTrigger(createForm)
    setStatus(`created ${createForm.triggerGroup}/${createForm.triggerName}`)
    await load()
  }

  const handleDelete = async (t: QuartzTrigger) => {
    await deleteQuartzTrigger(t.group, t.name)
    setStatus(`deleted ${t.group}/${t.name}`)
    await load()
  }

  const handlePause = async (t: QuartzTrigger) => {
    await pauseQuartzTrigger(t.group, t.name)
    setStatus(`paused ${t.group}/${t.name}`)
    await load()
  }

  const handleResume = async (t: QuartzTrigger) => {
    await resumeQuartzTrigger(t.group, t.name)
    setStatus(`resumed ${t.group}/${t.name}`)
    await load()
  }

  const handleRun = async (j: QuartzJob) => {
    await runQuartzJob(j.group, j.name)
    setStatus(`triggered ${j.group}/${j.name}`)
  }

  const handleDeleteJob = async (j: QuartzJob) => {
    await deleteQuartzJob(j.group, j.name)
    setStatus(`deleted ${j.group}/${j.name}`)
    await load()
  }

  const handleLogin = async () => {
    try {
      const res = await adminLogin(login.user, login.pass)
      setAdminToken(res.token)
      setStatus('admin auth set')
      await load()
    } catch (err) {
      setStatus('invalid credentials')
    }
  }

  const handleLogout = () => {
    clearAdminAuth()
    setStatus('admin auth cleared')
  }

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>Quartz Admin</h1>
          <p>Direct control of Quartz DB schedules</p>
        </div>
      </header>

      {status && <div className="status">{status}</div>}

      <section className="panel">
        <div className="panel-header">
          <h2>Admin Access</h2>
        </div>
        <div className="table">
          <div className="table-row cols-5">
            <div>
              <input
                type="text"
                placeholder="admin user"
                value={login.user}
                onChange={e => setLogin({ ...login, user: e.target.value })}
              />
            </div>
            <div>
              <input
                type="password"
                placeholder="admin password"
                value={login.pass}
                onChange={e => setLogin({ ...login, pass: e.target.value })}
              />
            </div>
            <div></div>
            <div></div>
            <div className="actions">
              <button className="ghost" onClick={handleLogin}>Apply</button>
              <button className="ghost" onClick={handleLogout}>Clear</button>
            </div>
          </div>
        </div>
      </section>

      <section className="panel">
        <div className="panel-header">
          <h2>Jobs</h2>
        </div>
        <div className="table">
          <div className="table-row cols-5 table-head">
            <div>Name</div>
            <div>Group</div>
            <div>Description</div>
            <div>Durable</div>
            <div>Action</div>
          </div>
          {jobs.map(j => (
            <div className="table-row cols-5" key={`${j.group}-${j.name}`}>
              <div>{j.name}</div>
              <div>{j.group}</div>
              <div className="muted">{j.description || '-'}</div>
              <div>{j.durable ? 'Yes' : 'No'}</div>
              <div>
                <button className="ghost" onClick={() => handleRun(j)}>Run</button>
                <button className="ghost" onClick={() => handleDeleteJob(j)}>Delete</button>
              </div>
            </div>
          ))}
          {jobs.length === 0 && <div className="empty">No jobs found.</div>}
        </div>
      </section>

      <section className="panel">
        <div className="panel-header">
          <h2>Create Job</h2>
        </div>
        <div className="table">
          <div className="table-row cols-7">
            <div>
              <input
                type="text"
                placeholder="name"
                value={jobForm.name}
                onChange={e => setJobForm({ ...jobForm, name: e.target.value })}
              />
            </div>
            <div>
              <input
                type="text"
                placeholder="group"
                value={jobForm.group}
                onChange={e => setJobForm({ ...jobForm, group: e.target.value })}
              />
            </div>
            <div>
              <input
                type="text"
                placeholder="class name"
                value={jobForm.className}
                onChange={e => setJobForm({ ...jobForm, className: e.target.value })}
              />
            </div>
            <div>
              <input
                type="text"
                placeholder="description"
                value={jobForm.description}
                onChange={e => setJobForm({ ...jobForm, description: e.target.value })}
              />
            </div>
            <div>
              <input
                type="text"
                placeholder="durable (true/false)"
                value={jobForm.durable ? 'true' : 'false'}
                onChange={e => setJobForm({ ...jobForm, durable: e.target.value === 'true' })}
              />
            </div>
            <div className="actions">
              <button className="ghost" onClick={async () => {
                await createQuartzJob(jobForm)
                setStatus(`created ${jobForm.group}/${jobForm.name}`)
                await load()
              }}>Create</button>
            </div>
            <div></div>
          </div>
        </div>
      </section>

      <section className="panel">
        <div className="panel-header">
          <h2>Triggers</h2>
        </div>
        <div className="table">
          <div className="table-row cols-7 table-head">
            <div>Trigger</div>
            <div>Job</div>
            <div>State</div>
            <div>Cron</div>
            <div>TimeZone</div>
            <div>Next</div>
            <div>Action</div>
          </div>
          {triggers.map(t => (
            <div className="table-row cols-7" key={`${t.group}-${t.name}`}>
              <div>{t.group}/{t.name}</div>
              <div className="muted">{t.jobGroup}/{t.jobName}</div>
              <div>{t.state}</div>
              <div>
                <input
                  type="text"
                  value={editing[key(t)]?.cron ?? t.cron ?? ''}
                  onChange={e => updateField(t, 'cron', e.target.value)}
                />
              </div>
              <div>
                <input
                  type="text"
                  value={editing[key(t)]?.timeZone ?? t.timeZone ?? 'UTC'}
                  onChange={e => updateField(t, 'timeZone', e.target.value)}
                />
              </div>
              <div className="muted">{t.nextFireTime || '-'}</div>
              <div className="actions">
                <button className="ghost" onClick={() => handleUpdate(t)}>Update</button>
                <button className="ghost" onClick={() => handlePause(t)}>Pause</button>
                <button className="ghost" onClick={() => handleResume(t)}>Resume</button>
                <button className="ghost" onClick={() => handleDelete(t)}>Delete</button>
              </div>
            </div>
          ))}
          {triggers.length === 0 && <div className="empty">No triggers found.</div>}
        </div>
      </section>

      <section className="panel">
        <div className="panel-header">
          <h2>Create Trigger</h2>
        </div>
        <div className="table">
          <div className="table-row cols-7">
            <div>
              <input
                type="text"
                placeholder="trigger name"
                value={createForm.triggerName}
                onChange={e => setCreateForm({ ...createForm, triggerName: e.target.value })}
              />
            </div>
            <div>
              <input
                type="text"
                placeholder="trigger group"
                value={createForm.triggerGroup}
                onChange={e => setCreateForm({ ...createForm, triggerGroup: e.target.value })}
              />
            </div>
            <div>
              <input
                type="text"
                placeholder="job name"
                value={createForm.jobName}
                onChange={e => setCreateForm({ ...createForm, jobName: e.target.value })}
              />
            </div>
            <div>
              <input
                type="text"
                placeholder="job group"
                value={createForm.jobGroup}
                onChange={e => setCreateForm({ ...createForm, jobGroup: e.target.value })}
              />
            </div>
            <div>
              <input
                type="text"
                placeholder="cron"
                value={createForm.cron}
                onChange={e => setCreateForm({ ...createForm, cron: e.target.value })}
              />
            </div>
            <div>
              <input
                type="text"
                placeholder="time zone"
                value={createForm.timeZone}
                onChange={e => setCreateForm({ ...createForm, timeZone: e.target.value })}
              />
            </div>
            <div className="actions">
              <button className="ghost" onClick={handleCreate}>Create</button>
            </div>
          </div>
        </div>
      </section>

      <section className="panel">
        <div className="panel-header">
          <h2>Recent Job Runs</h2>
          <span>daily-scan</span>
        </div>
        <div className="table">
          <div className="table-row cols-6 table-head">
            <div>ID</div>
            <div>Status</div>
            <div>Started</div>
            <div>Ended</div>
            <div>Message</div>
            <div>Job</div>
          </div>
          {runs.map(r => (
            <div className="table-row cols-6" key={r.id}>
              <div>#{r.id}</div>
              <div>{r.status}</div>
              <div className="muted">{r.startedAt}</div>
              <div className="muted">{r.endedAt || '-'}</div>
              <div className="muted">{r.message || '-'}</div>
              <div>{r.jobName}</div>
            </div>
          ))}
          {runs.length === 0 && <div className="empty">No job runs yet.</div>}
        </div>
      </section>

      <section className="panel">
        <div className="panel-header">
          <h2>Quartz Audit</h2>
          <span>Last 100</span>
        </div>
        <div className="table">
          <div className="table-row cols-6 table-head">
            <div>Time</div>
            <div>Action</div>
            <div>Target</div>
            <div>Actor</div>
            <div>Detail</div>
            <div>ID</div>
          </div>
          {audit.map(a => (
            <div className="table-row cols-6" key={a.id}>
              <div className="muted">{a.createdAt}</div>
              <div>{a.action}</div>
              <div className="muted">{a.target}</div>
              <div>{a.actor}</div>
              <div className="muted">{a.detail || '-'}</div>
              <div>#{a.id}</div>
            </div>
          ))}
          {audit.length === 0 && <div className="empty">No audit logs yet.</div>}
        </div>
      </section>
    </div>
  )
}
