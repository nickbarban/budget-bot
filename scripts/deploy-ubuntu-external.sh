#!/bin/bash
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"

env_file="$root/scripts/deploy-ubuntu-external.env"
if [[ ! -f "$env_file" ]]; then
  echo "Missing $env_file" >&2
  echo "Copy the example and set DEPLOY_SSH / DEPLOY_REMOTE_DIR:" >&2
  echo "  cp scripts/deploy-ubuntu-external.env.example scripts/deploy-ubuntu-external.env" >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "$env_file"
set +a

: "${DEPLOY_SSH:?Set DEPLOY_SSH (user@host) in scripts/deploy-ubuntu-external.env}"
: "${DEPLOY_REMOTE_DIR:?Set DEPLOY_REMOTE_DIR in scripts/deploy-ubuntu-external.env}"

BACKEND_DIR="$root/budget-bot-backend"
COMPOSE_SRC="$BACKEND_DIR/docker-compose.yml"
ENV_SRC="$BACKEND_DIR/.env"
ENV_EXAMPLE="$BACKEND_DIR/.env.example"
WORKFLOW_SRC_DIR="$root/budget-bot-frontend/workflows"

REGISTRY_HOST="${DEPLOY_REGISTRY_HOST:-host.docker.internal:5001}"
IMAGE_TAG="1.0.0-$(date -u +%Y%m%d%H%M%S)"
IMAGE="$REGISTRY_HOST/budget-bot-backend:$IMAGE_TAG"
COMPOSE_SERVICE="${DEPLOY_COMPOSE_SERVICE:-backend}"
BACKEND_HOST_PORT="${DEPLOY_BACKEND_HOST_PORT:-18080}"
POSTGRES_HOST_PORT="${DEPLOY_POSTGRES_HOST_PORT:-15432}"
workflow_remote_dir="${DEPLOY_REMOTE_WORKFLOW_DIR:-$DEPLOY_REMOTE_DIR/n8n}"

if [[ ! -f "$COMPOSE_SRC" ]]; then
  echo "Compose file not found: $COMPOSE_SRC" >&2
  exit 1
fi

if [[ ! -f "$BACKEND_DIR/Dockerfile" ]]; then
  echo "Dockerfile not found: $BACKEND_DIR/Dockerfile" >&2
  exit 1
fi

echo "Deploy image: $IMAGE"
echo "Remote: $DEPLOY_SSH:$DEPLOY_REMOTE_DIR"
echo "Host ports: backend ${BACKEND_HOST_PORT}->8080  postgres ${POSTGRES_HOST_PORT}->5432"

registry_ready() {
  curl -fsS --max-time 2 "http://127.0.0.1:5001/v2/" >/dev/null 2>&1
}

if ! registry_ready; then
  echo "Local registry is not listening on :5001 — starting registry:2"
  if docker ps -a --format '{{.Names}}' | grep -qx registry; then
    docker start registry >/dev/null
  else
    docker run -d --name registry --restart unless-stopped -p 5001:5000 registry:2 >/dev/null
  fi
fi

for _ in $(seq 1 20); do
  if registry_ready; then
    break
  fi
  sleep 0.3
done

if ! registry_ready; then
  echo "Local registry still unreachable at http://127.0.0.1:5001/v2/" >&2
  echo "Start it first, or check that nothing else should own :5001." >&2
  exit 1
fi

docker buildx build --platform linux/amd64 -t "$IMAGE" --load -f "$BACKEND_DIR/Dockerfile" "$BACKEND_DIR"
docker push "$IMAGE"

if [[ ! -d "$WORKFLOW_SRC_DIR" ]]; then
  echo "Workflow directory not found: $WORKFLOW_SRC_DIR" >&2
  exit 1
fi

echo "Copying n8n workflows to $DEPLOY_SSH:$workflow_remote_dir/"
ssh "$DEPLOY_SSH" "mkdir -p \"$DEPLOY_REMOTE_DIR\" \"$workflow_remote_dir\""
scp "$WORKFLOW_SRC_DIR"/*.json "$DEPLOY_SSH:$workflow_remote_dir/"
scp "$COMPOSE_SRC" "$DEPLOY_SSH:$DEPLOY_REMOTE_DIR/docker-compose.yml"

if [[ -f "$ENV_SRC" ]]; then
  env_src="$ENV_SRC"
elif [[ -f "$ENV_EXAMPLE" ]]; then
  echo "No local .env — using .env.example as remote .env"
  env_src="$ENV_EXAMPLE"
else
  echo "No backend .env or .env.example to copy" >&2
  exit 1
fi

env_tmp="$(mktemp)"
grep -vE '^(BACKEND_HOST_PORT|POSTGRES_HOST_PORT)=' "$env_src" > "$env_tmp"
printf '\nBACKEND_HOST_PORT=%s\nPOSTGRES_HOST_PORT=%s\n' \
  "$BACKEND_HOST_PORT" "$POSTGRES_HOST_PORT" >> "$env_tmp"
echo "Copying backend .env to $DEPLOY_SSH:$DEPLOY_REMOTE_DIR/.env (host ports ${BACKEND_HOST_PORT}/${POSTGRES_HOST_PORT})"
scp "$env_tmp" "$DEPLOY_SSH:$DEPLOY_REMOTE_DIR/.env"
rm -f "$env_tmp"

override="$(mktemp)"
{
  printf 'services:\n'
  printf '  %s:\n    image: %s\n    pull_policy: never\n    extra_hosts:\n      - "host.docker.internal:host-gateway"\n' \
    "$COMPOSE_SERVICE" "$IMAGE"
  if [[ -n "${DEPLOY_TELEGRAM_BOT_TOKEN:-}" || -n "${DEPLOY_TELEGRAM_WEBHOOK_SECRET:-}" || -n "${DEPLOY_MONOBANK_TOKEN:-}" ]]; then
    printf '    environment:\n'
    if [[ -n "${DEPLOY_TELEGRAM_BOT_TOKEN:-}" ]]; then
      printf '      TELEGRAM_BOT_TOKEN: "%s"\n' "$DEPLOY_TELEGRAM_BOT_TOKEN"
    fi
    if [[ -n "${DEPLOY_TELEGRAM_WEBHOOK_SECRET:-}" ]]; then
      printf '      TELEGRAM_WEBHOOK_SECRET: "%s"\n' "$DEPLOY_TELEGRAM_WEBHOOK_SECRET"
    fi
    if [[ -n "${DEPLOY_MONOBANK_TOKEN:-}" ]]; then
      printf '      MONOBANK_TOKEN: "%s"\n' "$DEPLOY_MONOBANK_TOKEN"
    fi
    if [[ -n "${DEPLOY_MONOBANK_ACCOUNT_ID:-}" ]]; then
      printf '      MONOBANK_ACCOUNT_ID: "%s"\n' "$DEPLOY_MONOBANK_ACCOUNT_ID"
    fi
  fi
} > "$override"
scp "$override" "$DEPLOY_SSH:$DEPLOY_REMOTE_DIR/docker-compose.deploy.yml"
rm -f "$override"

# Ubuntu cannot resolve host.docker.internal (Docker Desktop DNS). Load the
# image over SSH instead of docker compose pull.
echo "Loading $IMAGE onto $DEPLOY_SSH (no pull from host.docker.internal)"
docker save "$IMAGE" | ssh "$DEPLOY_SSH" "docker load && \
  cd \"$DEPLOY_REMOTE_DIR\" && \
  docker compose --env-file .env -f docker-compose.yml -f docker-compose.deploy.yml up -d --force-recreate && \
  docker compose --env-file .env -f docker-compose.yml -f docker-compose.deploy.yml images"
