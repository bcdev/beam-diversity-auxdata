#!/bin/bash
set -e

# l2-format-tif2nc-yearly-step.sh 2009 Lake-Aral 2009-01-01 2009-01-31  /calvalus/projects/diversity/yearly-seq/ /calvalus/projects/diversity/yearly-nc/ 

year=$1
region=$2
minDate=$3
maxDate=$4
input=$5
output=$6

request=requests/l2-format-tif2nc-yearly-${year}-${region}-${minDate}-${maxDate}.xml

cat etc/l2-format-tif2nc-yearly-template.xml \
| sed -e "s,\${year},${year},g" -e "s,\${region},${region},g" -e "s,\${minDate},${minDate},g" -e "s,\${maxDate},${maxDate},g" -e "s,\${input},${input},g" -e "s,\${output},${output},g" > $request

echo "java -Xmx256m -jar $CALVALUS_PRODUCTION_JAR -e --snap $CALVALUS_BEAM_VERSION --calvalus $CALVALUS_CALVALUS_VERSION $request"
java -Xmx256m -jar $CALVALUS_PRODUCTION_JAR -e --snap $CALVALUS_BEAM_VERSION --calvalus $CALVALUS_CALVALUS_VERSION $request

