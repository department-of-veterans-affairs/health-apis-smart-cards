#! /usr/bin/env bash

REPO=$(cd $(dirname $0) && pwd)
PROFILE=dev
MARKER=$(date +%s)

makeConfig() {
  local project="$1"
  local profile="$2"
  local target="$REPO/$project/config/application-${profile}.properties"
  [ -f "$target" ] && mv -v $target $target.$MARKER
  grep -E '(.*= *unset)' "$REPO/$project/src/main/resources/application.properties" \
    > "$target"
}

configValue() {
  local project="$1"
  local profile="$2"
  local key="$3"
  local value="$4"
  local target="$REPO/$project/config/application-${profile}.properties"
  local escapedValue=$(echo $value | sed -e 's/\\/\\\\/g; s/\//\\\//g; s/&/\\\&/g')
  sed -i "s/^$key=.*/$key=$escapedValue/" $target
}

checkForUnsetValues() {
  local project="$1"
  local profile="$2"
  local target="$REPO/$project/config/application-${profile}.properties"
  echo "checking $target"
  grep -E '(.*= *unset)' "$target"
  [ $? == 0 ] && echo "Failed to populate all unset values" && exit 1
  diff -q $target $target.$MARKER
  [ $? == 0 ] && rm -v $target.$MARKER
}

makeConfig smart-cards $PROFILE
configValue smart-cards $PROFILE data-query.r4-base-path '/'
configValue smart-cards $PROFILE data-query.internal-url 'http://localhost:8777'
configValue smart-cards $PROFILE metadata.security.token-endpoint 'http://fake.com/token'
configValue smart-cards $PROFILE metadata.security.authorize-endpoint 'http://fake.com/authorize'
configValue smart-cards $PROFILE metadata.security.management-endpoint 'http://fake.com/manage'
configValue smart-cards $PROFILE metadata.security.revocation-endpoint 'http://fake.com/revoke'
configValue smart-cards $PROFILE public-r4-base-path 'r4'
configValue smart-cards $PROFILE public-url 'http://localhost:8096'
configValue smart-cards $PROFILE web-exception-key '-sharktopus-v-pteracuda-'
configValue smart-cards $PROFILE well-known.capabilities "context-standalone-patient, launch-ehr, permission-offline, permission-patient"
configValue smart-cards $PROFILE well-known.response "ode, refresh_token"
configValue smart-cards $PROFILE well-known.scopes-supported "patient/DiagnosticReport.read, patient/Patient.read, offline_access"
checkForUnsetValues smart-cards $PROFILE
