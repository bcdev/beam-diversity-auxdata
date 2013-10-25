#!/bin/bash

# calls NCAR ncl script to convert binary format to netcdf
# example: bin2nc_cmorph.bash 2001 03

year=$1
month=$2

echo "Processing year $year, month $month..."

cd $year/$year$month
INFILES=`ls CMORPH*30min_$year$month*`
#echo "infiles: $INFILES"
if [ ! -d nc ]; then
   mkdir nc
fi
if [ ! -d ps ]; then
   mkdir ps
fi
cd ../../

#for file in $INFILES
#do
#   echo "ncl year=$year 'month=\"$month\"' 'fName=\"$file\"' bin2nc_cmorph.ncl"
#   ncl year=$year 'month=\"$month\"' 'fName=\"$file\"' bin2nc_cmorph.ncl &
#done

echo "ncl year=$year 'month=\"$month\"' 'dName=\"./$year/$year$month/\"' bin2nc_cmorph_all.ncl"
#ncl year=$year 'month=\"$month\"' 'dName=\"./$year/$year$month/\"' bin2nc_cmorph_all.ncl


echo "Done processing year $year, month $month."
