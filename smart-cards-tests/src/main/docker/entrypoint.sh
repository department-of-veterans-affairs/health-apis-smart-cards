#!/usr/bin/env bash

set -euo pipefail

if [ -z "${SENTINEL_BASE_DIR:-}" ]; then SENTINEL_BASE_DIR=/sentinel; fi
cd $SENTINEL_BASE_DIR

test -n "${K8S_ENVIRONMENT}"
test -n "${K8S_LOAD_BALANCER}"

if [ -z "${MAGIC_ACCESS_TOKEN:-}" ]; then MAGIC_ACCESS_TOKEN="$MAGIC_ACCESS_TOKEN"; fi
if [ -z "${SENTINEL_ENV:-}" ]; then SENTINEL_ENV="$K8S_ENVIRONMENT"; fi
if [ -z "${SENTINEL_URL:-}" ]; then SENTINEL_URL="https://${K8S_LOAD_BALANCER}"; fi

java-tests \
  --module-name "smart-cards-tests" \
  --regression-test-pattern ".*IT\$" \
  --smoke-test-pattern ".*Smoke.*IT\$" \
  -Daccess-token="$MAGIC_ACCESS_TOKEN" \
  -Dsentinel="$SENTINEL_ENV" \
  -Dsentinel.external.url="${SENTINEL_URL}" \
  -Dsentinel.internal.url="${SENTINEL_URL}" \
  $@

exit $?
