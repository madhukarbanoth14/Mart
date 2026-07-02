#!/usr/bin/env bash
# Shared Cloud SQL Auth Proxy helper. Source from other scripts.
# Sets PROXY_STARTED_BY_US=1 when this script started the proxy (safe to kill on exit).

start_cloud_sql_proxy_if_needed() {
  local instance="${1:?instance required}"
  local port="${2:?port required}"

  PROXY_STARTED_BY_US=0
  PROXY_PID=""

  if (echo >/dev/tcp/127.0.0.1/"$port") >/dev/null 2>&1; then
    echo "Reusing Cloud SQL proxy already listening on 127.0.0.1:${port}"
    return 0
  fi

  # Redirect proxy output to a log file. If it inherited our stdout it would keep
  # the write end of a downstream pipe (e.g. `| tail`) open, so consumers never
  # see EOF and the whole command hangs even after migrations finish.
  local logfile="${CLOUD_SQL_PROXY_LOG:-/tmp/cloud-sql-proxy-${port}.log}"
  echo "Starting Cloud SQL Auth Proxy on 127.0.0.1:${port} → ${instance} (logs: ${logfile})"
  cloud-sql-proxy "$instance" --port "$port" >"$logfile" 2>&1 &
  PROXY_PID=$!
  PROXY_STARTED_BY_US=1

  for _ in $(seq 1 30); do
    if (echo >/dev/tcp/127.0.0.1/"$port") >/dev/null 2>&1; then
      return 0
    fi
    sleep 1
  done

  echo "Timed out waiting for proxy on 127.0.0.1:${port}"
  return 1
}

stop_cloud_sql_proxy_if_started() {
  if [[ "${PROXY_STARTED_BY_US:-0}" == "1" && -n "${PROXY_PID:-}" ]]; then
    kill "$PROXY_PID" 2>/dev/null || true
  fi
}

rewrite_proxy_database_url() {
  local url="$1"
  local port="$2"
  echo "$url" | sed -E "s/@[^/?]+/@127.0.0.1:${port}/"
}
