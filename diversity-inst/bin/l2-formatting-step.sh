#!/bin/bash
set -e

# l2-formatting-step.sh 2009 Lake-Peipus idepix /calvalus/projects/diversity/prototype/Lake-Peipus/idepix/2009 /calvalus/projects/diversity/prototype/Lake-Peipus/idepix/2009/nc

year=$1
region=$2
processor=$3
input=$4
output=$5

request=requests/l2-format-${processor}-${region}-${year}.xml

cat etc/l2-format-template.xml \
| sed -e "s,\${year},${year},g" -e "s,\${region},${region},g" -e "s,\${processor},${processor},g" -e "s,\${input},${input},g" -e "s,\${output},${output},g" \
> $request

echo "java -Xmx128m -jar $DIVERSITY_PRODUCTION_JAR -e --beam $DIVERSITY_BEAM_VERSION --calvalus $DIVERSITY_CALVALUS_VERSION $request"
java -Xmx128m -jar $DIVERSITY_PRODUCTION_JAR -e --beam $DIVERSITY_BEAM_VERSION --calvalus $DIVERSITY_CALVALUS_VERSION $request

