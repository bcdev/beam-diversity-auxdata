#!/bin/bash

# processes CMORPH biweekly from hourly
# example: process_cmorph_biweekly.bash 2001 03

year=$1
month=$2

if [ $# -ne 2 ]
then
  echo "Usage: ./`basename $0` <year> <month>"
  exit -1
fi

SRCDIR=/data/diversity/data/CMORPH/ftp.cpc.ncep.noaa.gov/$year/$year$month/nc_daily
OUTDIR=/data/diversity/data/CMORPH/ftp.cpc.ncep.noaa.gov/$year/nc_biweekly
BEAMDIR=/opt/beam-4.11

if [ ! -d $OUTDIR ]; then
   mkdir $OUTDIR
fi

echo "time $BEAMDIR/bin/gpt_8192.sh Diversity.Auxdata -PinputDataDir=$SRCDIR -PoutputDataDir=$OUTDIR -Pyear=$year -Pmonth=$month -Pcategory=CMORPH_BIWEEKLY -e"
time $BEAMDIR/bin/gpt_8192.sh Diversity.Auxdata -PinputDataDir=$SRCDIR -PoutputDataDir=$OUTDIR -Pyear=$year -Pmonth=$month -Pcategory=CMORPH_BIWEEKLY -e

echo "Done processing year $year, month $month."
