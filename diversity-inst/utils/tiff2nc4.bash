#!/bin/bash

year=$1
month=$2

snapDir=/home/olaf/snap

### set GPT
gpt=$snapDir/bin/gpt

srcRootDir=/calvalus/projects/diversity/prototype
monthTargetDir=/calvalus/projects/diversity/monthly/nc/2006/$month
mkdir -p $monthTargetDir
for srcDir in `ls $srcRootDir`; do
    srcMonthlyDir=$srcRootDir/$srcDir/l3-monthly/$year/${month}-L3-1
    #echo "srcMonthlyDir: $"srcMonthlyDir"
    srcFile=`ls $srcMonthlyDir/*.tif`
    #echo "srcFile: $"srcFile"
    if [ -f "$srcFile" ]
    then
        srcBaseName=$(basename "$srcFile" .tif)
        srcTargetName=${srcBaseName}.nc
        echo "$gpt Tiff2Nc4Op -SsourceProduct=$srcFile  -f NetCDF4-BEAM -t $monthTargetDir/$srcTargetName"
        $gpt Tiff2Nc4Op -SsourceProduct=$srcFile  -f NetCDF4-BEAM -t $monthTargetDir/$srcTargetName
    fi
done

echo `date`

