#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
APP_NAME="stock-admin"
JAR_PATH="${BASE_DIR}/target/${APP_NAME}.jar"
PID_FILE="${BASE_DIR}/runtime/${APP_NAME}.pid"
LOG_FILE="${BASE_DIR}/runtime/logs/${APP_NAME}.log"
UNIT_NAME="${APP_NAME}.service"

cd "${BASE_DIR}"

mkdir -p "${BASE_DIR}/runtime/logs"

if [ ! -f "${JAR_PATH}" ]; then
  echo "jar not found: ${JAR_PATH}"
  exit 1
fi

if command -v systemctl >/dev/null 2>&1 && systemctl list-units >/dev/null 2>&1; then
  if systemctl is-active --quiet "${UNIT_NAME}"; then
    echo "${APP_NAME} is already running as ${UNIT_NAME}"
    exit 0
  fi
  systemctl reset-failed "${UNIT_NAME}" >/dev/null 2>&1 || true
  rm -f "${PID_FILE}"
  systemd-run \
    --unit="${APP_NAME}" \
    --working-directory="${BASE_DIR}" \
    --property=Restart=on-failure \
    --property=RestartSec=5 \
    /usr/bin/java -jar "${JAR_PATH}" >/dev/null
  echo "${APP_NAME} started as ${UNIT_NAME}"
  exit 0
fi

if [ -f "${PID_FILE}" ] && kill -0 "$(cat "${PID_FILE}")" 2>/dev/null; then
  echo "${APP_NAME} is already running with pid $(cat "${PID_FILE}")"
  exit 0
fi

setsid nohup java -jar "${JAR_PATH}" > "${LOG_FILE}" 2>&1 < /dev/null &
echo $! > "${PID_FILE}"
echo "${APP_NAME} started, pid=$(cat "${PID_FILE}")"
