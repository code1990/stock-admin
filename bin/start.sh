#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
APP_NAME="stock-admin"
JAR_PATH="${BASE_DIR}/target/${APP_NAME}.jar"
PID_FILE="${BASE_DIR}/runtime/${APP_NAME}.pid"
LOG_FILE="${BASE_DIR}/runtime/logs/${APP_NAME}.log"

cd "${BASE_DIR}"

mkdir -p "${BASE_DIR}/runtime/logs"

if [ ! -f "${JAR_PATH}" ]; then
  echo "jar not found: ${JAR_PATH}"
  exit 1
fi

if [ -f "${PID_FILE}" ] && kill -0 "$(cat "${PID_FILE}")" 2>/dev/null; then
  echo "${APP_NAME} is already running with pid $(cat "${PID_FILE}")"
  exit 0
fi

setsid nohup java -jar "${JAR_PATH}" > "${LOG_FILE}" 2>&1 < /dev/null &
echo $! > "${PID_FILE}"
echo "${APP_NAME} started, pid=$(cat "${PID_FILE}")"
