#!/usr/bin/env bash
# Local deploy: PostgreSQL 17 + Spring Boot backend via docker compose.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="${ROOT_DIR}/budget-bot-backend"
COMPOSE_FILE="${BACKEND_DIR}/docker-compose.yml"
ENV_FILE="${BACKEND_DIR}/.env"
ENV_EXAMPLE="${BACKEND_DIR}/.env.example"
HEALTH_URL="${HEALTH_URL:-http://localhost:8080/actuator/health}"
WAIT_SECONDS="${WAIT_SECONDS:-90}"

usage() {
  cat <<'EOF'
Usage: scripts/deploy-local.sh [command]

Commands:
  up        Build images and start postgres + backend (default)
  rebuild   Force rebuild images, then start
  down      Stop containers (keeps the postgres volume)
  reset     Stop containers and delete the postgres volume
  logs      Follow backend + postgres logs
  status    Show compose status and health

Examples:
  ./scripts/deploy-local.sh
  ./scripts/deploy-local.sh logs
  WAIT_SECONDS=120 ./scripts/deploy-local.sh rebuild
EOF
}

need_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "error: '$1' is required but not installed" >&2
    exit 1
  fi
}

compose() {
  docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" "$@"
}

ensure_env() {
  if [[ ! -f "${ENV_FILE}" ]]; then
    if [[ ! -f "${ENV_EXAMPLE}" ]]; then
      echo "error: missing ${ENV_EXAMPLE}" >&2
      exit 1
    fi
    cp "${ENV_EXAMPLE}" "${ENV_FILE}"
    echo "created ${ENV_FILE} from .env.example"
    echo "fill TELEGRAM_BOT_TOKEN / MONOBANK_TOKEN there if you need bot/bank features"
  fi
}

wait_for_health() {
  local elapsed=0
  echo "waiting for ${HEALTH_URL} (timeout ${WAIT_SECONDS}s)"
  until curl -fsS "${HEALTH_URL}" >/dev/null 2>&1; do
    if (( elapsed >= WAIT_SECONDS )); then
      echo "error: backend did not become healthy in ${WAIT_SECONDS}s" >&2
      compose logs --tail 80 backend
      exit 1
    fi
    sleep 2
    elapsed=$((elapsed + 2))
  done
  echo "health: $(curl -fsS "${HEALTH_URL}")"
}

cmd_up() {
  local extra_args=()
  if [[ "${1:-}" == "rebuild" ]]; then
    extra_args+=(--build --force-recreate)
  else
    extra_args+=(--build)
  fi
  ensure_env
  echo "starting stack from ${COMPOSE_FILE}"
  compose up -d "${extra_args[@]}"
  wait_for_health
  compose ps
  echo
  echo "backend:  ${HEALTH_URL}"
  echo "postgres: localhost:5432  (user/db: budgetbot)"
}

cmd_down() {
  compose down
}

cmd_reset() {
  compose down -v
  echo "stopped stack and removed postgres volume"
}

cmd_logs() {
  compose logs -f
}

cmd_status() {
  compose ps
  echo
  if curl -fsS "${HEALTH_URL}" >/dev/null 2>&1; then
    echo "health: $(curl -fsS "${HEALTH_URL}")"
  else
    echo "health: down (${HEALTH_URL})"
  fi
}

main() {
  need_cmd docker
  need_cmd curl
  if ! docker compose version >/dev/null 2>&1; then
    echo "error: docker compose is required" >&2
    exit 1
  fi
  if [[ ! -f "${COMPOSE_FILE}" ]]; then
    echo "error: missing ${COMPOSE_FILE}" >&2
    exit 1
  fi

  local cmd="${1:-up}"
  case "${cmd}" in
    -h|--help|help) usage ;;
    up) cmd_up ;;
    rebuild) cmd_up rebuild ;;
    down) cmd_down ;;
    reset) cmd_reset ;;
    logs) cmd_logs ;;
    status) cmd_status ;;
    *)
      echo "error: unknown command '${cmd}'" >&2
      usage
      exit 1
      ;;
  esac
}

main "$@"
