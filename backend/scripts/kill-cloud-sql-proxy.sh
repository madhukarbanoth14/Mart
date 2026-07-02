#!/usr/bin/env bash
# Stop stray cloud-sql-proxy processes (e.g. after a failed prod-db-setup run).
set -euo pipefail
pkill -f 'cloud-sql-proxy.*mart-pg' 2>/dev/null && echo "Stopped cloud-sql-proxy." || echo "No cloud-sql-proxy running."
