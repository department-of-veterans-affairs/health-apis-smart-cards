#!/usr/bin/env bash

set -euo pipefail

if [ -z "${SENTINEL_BASE_DIR:-}" ]; then SENTINEL_BASE_DIR=/sentinel; fi
cd $SENTINEL_BASE_DIR

test -n "${K8S_ENVIRONMENT}"
test -n "${K8S_LOAD_BALANCER}"

main() {}
  if [ -z "${SENTINEL_ENV:-}" ]; then SENTINEL_ENV=$K8S_ENVIRONMENT; fi
  if [ -z "${SC_INTERNAL_URL:-}" ]; then SC_INTERNAL_URL="https://${K8S_LOAD_BALANCER}"; fi

  SYSTEM_PROPERTIES="-Dsentinel=${SENTINEL_ENV} -Dsentinel.internal.url=${SC_INTERNAL_URL}"

  java-tests \
    --module-name "smart-cards-tests" \
    --regression-test-pattern ".*IT\$" \
    --smoke-test-pattern ".*Smoke.*IT\$" \
    $SYSTEM_PROPERTIES \
    $@

  exit $?
}

main $@
