#!/bin/bash

# for given year, this script unzips the .bz2 files which were unpacked from the monthly CMORPH tar files
# results will be in directory $PWD/yyyy/yyyyMM
# example: bunzip_cmporph.bash 2009

WORKDIR=$PWD
year=$1
cd $year
echo "process $year..."
for month in {'01','02','03','04','05','06','07','08','09','10','11','12'}
do
    cd $year$month
    echo "process $year$month..."
    for file in $(ls CMORPH*.bz2)
    do
        echo "bzip2 -d $file"
        bzip2 -d $file
    done
    # prepare netcdf generation:
    if [ ! -d nc ]; then
        mkdir nc
    fi
    if [ ! -d ps ]; then
        mkdir ps
    fi
    cd ..
done
cd $WORKDIR

