#!/bin/bash
set -e

#year=$1
startYear=2003
endYear=2011

OLDDIR=$PWD

#rootDir=/calvalus/projects/diversity/lakes-WB
#rootDir=/calvalus/projects/diversity/lakes-WB/existing_lakes
rootDir=/calvalus/projects/diversity/prototype

for year in $(seq -w $startYear $endYear); do
    commonYearlyDir=/calvalus/projects/diversity/yearly/$year
    for lake in `ls $rootDir`; do
        yearlyTifDir=$rootDir/$lake/l3-yearly/$year-L3-1
        yearlyTifFile=${lake}_${year}-01-01_${year}-12-31.tif
        #if [ -f "$yearlyTifDir/$yearlyTifFile" ] 
        #if [ ! -f "$commonYearlyDir/$yearlyTifFile" ]
        if [ -f "$yearlyTifDir/$yearlyTifFile" ] && [ ! -f "$commonYearlyDir/$yearlyTifFile" ]
        then
            echo "hadoop fs -cp $yearlyTifDir/${yearlyTifFile} $commonYearlyDir"
            hadoop fs -cp $yearlyTifDir/${yearlyTifFile} $commonYearlyDir
        fi
    done
done

echo "done."

cd $OLDDIR

