#!/bin/bash

# processes CMORPH dailies from hourlies
# example: process_cmorph_daily.bash 2001 03 1 31

year=$1
month=$2
startDay=$3
endDay=$4

if [ $# -ne 4 ]
then
  echo "Usage: ./`basename $0` <year> <month> <startDay> <endDay>"
  exit -1
fi

SRCDIR=/data/diversity/data/CMORPH/ftp.cpc.ncep.noaa.gov/$year/$year$month/nc
OUTDIR=/data/diversity/data/CMORPH/ftp.cpc.ncep.noaa.gov/$year/$year$month/nc_daily
BEAMDIR=/opt/beam-4.11

if [ ! -d $OUTDIR ]; then
   mkdir $OUTDIR
fi

echo "start: $startDay"
echo "end: $endDay"

for day in `seq $startDay  $endDay`; 
do
   echo "time $BEAMDIR/bin/gpt_8192.sh Diversity.Auxdata -PinputDataDir=$SRCDIR -PoutputDataDir=$OUTDIR -Pyear=$year -Pmonth=$month -Pday=$day -Pcategory=CMORPH_DAILY -e"
   time $BEAMDIR/bin/gpt.sh Diversity.Auxdata -PinputDataDir=$SRCDIR -PoutputDataDir=$OUTDIR -Pyear=$year -Pmonth=$month -Pday=$day -Pcategory=CMORPH_DAILY -e
done
echo "Done processing year $year, month $month, day $day."