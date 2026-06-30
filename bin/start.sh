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

load_env_file() {
  local env_file="$1"
  if [ -f "${env_file}" ]; then
    set -a
    # shellcheck disable=SC1090
    . "${env_file}"
    set +a
  fi
}

if [ -f "${BASE_DIR}/.env" ]; then
  load_env_file "${BASE_DIR}/.env"
elif [ -f "${BASE_DIR}/../stock_cron/.env" ]; then
  load_env_file "${BASE_DIR}/../stock_cron/.env"
fi

if [ -z "${STOCK_ADMIN_DB_URL:-}" ] && [ -n "${STOCK_STAT_DB_PATH:-}" ]; then
  export STOCK_ADMIN_DB_URL="jdbc:sqlite:${STOCK_STAT_DB_PATH}"
fi

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
  SYSTEMD_ENV_ARGS=()
  for ENV_NAME in STOCK_ADMIN_DB_URL STOCK_ADMIN_DB_USERNAME STOCK_ADMIN_DB_PASSWORD STOCK_ADMIN_QUOTE_60_DB_URL STOCK_ADMIN_KLINE_CACHE_DIR; do
    if [ -n "${!ENV_NAME:-}" ]; then
      SYSTEMD_ENV_ARGS+=("--setenv=${ENV_NAME}=${!ENV_NAME}")
    fi
  done
  systemd-run \
    --unit="${APP_NAME}" \
    --working-directory="${BASE_DIR}" \
    --property=Restart=on-failure \
    --property=RestartSec=5 \
    "${SYSTEMD_ENV_ARGS[@]}" \
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
