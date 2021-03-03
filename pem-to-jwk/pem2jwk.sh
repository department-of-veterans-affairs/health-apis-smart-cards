#!/usr/bin/env bash
[ $# -ne 1 ] && echo "pem2jwk.sh pem-file-path" && exit 1

BASE_DIR=$(dirname $(readlink -f $0))

findJar() {
  PEM2JWK_JAR=$(find $BASE_DIR/target -name "pem-to-jwk-*.jar")
}

findJar
[ -z "$PEM2JWK_JAR" ] && mvn clean install -P"!standard" -DskipTests -f $BASE_DIR
findJar
[ -z "$PEM2JWK_JAR" ] && echo "pem-to-jwk jar still missing... giving up" && exit 1

[ -n "$JAVA_HOME" ] && JAVA_EXE="$JAVA_HOME/bin/java" || JAVA_EXE=java

CLASSPATH="${PEM2JWK_JAR}"

$JAVA_EXE -cp "${CLASSPATH}" gov.va.api.pem2jwk.Pem2Jwk $@
