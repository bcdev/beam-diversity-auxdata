#!/bin/bash

# for given year, this script untars the CMOPRPH monthly input tar files
# results will be in directory $PWD/yyyy/yyyyMM
# example: untar_cmorph.bash 2009 

WORKDIR=$PWD
year =$1
cd $year
echo "untar $year..."
for file in $(ls CMORPH*.tar)
do
    echo "tar -xvf $file"
    tar -xvf $file
done
cd $WORKDIR

