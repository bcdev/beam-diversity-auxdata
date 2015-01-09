#!/bin/bash

if [ "$1" = "" ]; then
  echo "usage: $0 <request>"
  exit 1
fi

echo $@ 
java -Xmx128m -jar $DIVERSITY_PRODUCTION_JAR -e --beam $DIVERSITY_BEAM_VERSION --calvalus $DIVERSITY_CALVALUS_VERSION $*
