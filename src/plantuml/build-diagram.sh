#!/usr/bin/env bash
PLANTUML=plantuml
DIAGRAMS=$(cd $(dirname $0) && pwd)

which $PLANTUML > /dev/null 2>&1
[ $? != 0 ] && echo "$PLANTUML not found" && exit 1

echo "Looking for diagrams in $DIAGRAMS"
cd $DIAGRAMS
find . -type f -name "*.puml" | xargs -t -P 4 $PLANTUML
