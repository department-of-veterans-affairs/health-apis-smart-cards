#!/usr/bin/env bash

set -euo pipefail

if [ -z "${SENTINEL_BASE_DIR:-}" ]; then SENTINEL_BASE_DIR=/sentinel; fi
cd $SENTINEL_BASE_DIR

test -n "${K8S_ENVIRONMENT}"
if [ -z "${SENTINEL_ENV:-}" ]; then SENTINEL_ENV=$K8S_ENVIRONMENT; fi

java-tests \
  --module-name "smart-cards-tests" \
  --regression-test-pattern ".*IT\$" \
  --smoke-test-pattern ".*Smoke.*IT\$" \
  -Dsentinel="$SENTINEL_ENV" \
  $@

exit $?
