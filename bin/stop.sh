#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
APP_NAME="stock-admin"
PID_FILE="${BASE_DIR}/runtime/${APP_NAME}.pid"
UNIT_NAME="${APP_NAME}.service"

if command -v systemctl >/dev/null 2>&1 && systemctl list-units >/dev/null 2>&1; then
  if systemctl is-active --quiet "${UNIT_NAME}" && ! timeout 20 systemctl stop "${UNIT_NAME}"; then
    systemctl kill -s KILL "${UNIT_NAME}" >/dev/null 2>&1 || true
  fi
  systemctl reset-failed "${UNIT_NAME}" >/dev/null 2>&1 || true
fi

if [ ! -f "${PID_FILE}" ]; then
  echo "stopped ${UNIT_NAME}"
  exit 0
fi

PID="$(cat "${PID_FILE}")"
if kill -0 "${PID}" 2>/dev/null; then
  kill "${PID}"
  echo "stopped pid ${PID}"
fi
rm -f "${PID_FILE}"
