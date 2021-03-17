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
# DO NOT REUSE private keys outside of local env:
configValue smart-cards $PROFILE jwk-set.private-json '{"keys":[{"kty":"EC","d":"5h-A-oFDpePWC9zZ54_sWANZlwDxppu2-DQuAuvW3Bw","use":"sig","crv":"P-256","kid":"gSb6VludekqwZS_0osHNxAK2y2Rua1V_aGWNFaTvgNM","x":"WcBuVJRGiCvhEdU6nbKysIxEjQOqzaH-kiIZoIpFNfE","y":"FiWqBp6ilDBJ-VEnZsgHTK4fYKmB841n2wNMfvFZB2E","alg":"ES256"}]}'
configValue smart-cards $PROFILE jwk-set.current-key-id 'gSb6VludekqwZS_0osHNxAK2y2Rua1V_aGWNFaTvgNM'
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
