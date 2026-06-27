#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
PID_FILE="${BASE_DIR}/runtime/stock-admin.pid"

if [ ! -f "${PID_FILE}" ]; then
  echo "pid file not found"
  exit 0
fi

PID="$(cat "${PID_FILE}")"
if kill -0 "${PID}" 2>/dev/null; then
  kill "${PID}"
  echo "stopped pid ${PID}"
fi
rm -f "${PID_FILE}"
