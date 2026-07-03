/** Frontend debug logger - records API calls and errors, sends to backend file. */

const MAX_LOGS = 300

interface LogEntry {
  time: string
  type: string
  message: string
  data?: string
}

let logs: LogEntry[] = []
let flushTimer: ReturnType<typeof setTimeout> | null = null

function flush() {
  if (logs.length === 0) return
  const batch = logs.splice(0, logs.length)
  fetch('/api/debug/log', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ logs: batch }),
  }).catch(() => {}) // fire-and-forget
}

function scheduleFlush() {
  if (!flushTimer) {
    flushTimer = setTimeout(() => {
      flushTimer = null
      flush()
    }, 2000)
  }
}

export function addLog(type: string, message: string, data?: string) {
  const entry: LogEntry = {
    time: new Date().toISOString().slice(11, 23),
    type,
    message,
    data: data ? data.slice(0, 500) : undefined,
  }
  logs.push(entry)
  if (logs.length > MAX_LOGS) logs.splice(0, logs.length - MAX_LOGS)
  scheduleFlush()

  // Also print to console
  const prefix = `[${type}]`
  if (type === 'ERR') console.error(prefix, message, data || '')
  else if (type === 'REQ') console.log(prefix, '>>>', message)
  else if (type === 'RES') console.log(prefix, '<<<', message)
  else console.log(prefix, message)
}

export function clearLogs() {
  logs = []
  flush()
}

// Send remaining logs before page unload
window.addEventListener('beforeunload', () => flush())
