#! /bin/sh

ndwi=$1
mask=$2
csv=$3
lake=$4

if [ "$4" = "" ]; then
    echo "usage:"
    echo "$0 <ndwi-input> <mask-input> <csv-output-name> <lake-name>"
    echo "$0 /home/boe/tmp/L3_2008-04-30_2008-05-14.dim /home/boe/tmp/Natron-mask.dim /home/boe/tmp/lake-statistics.csv Natron"
    exit 1
fi

export BEAM4_HOME=/opt/viewer/beam-4.10.4-SNAPSHOT

if [ -z "$BEAM4_HOME" ]; then
    echo
    echo Error: BEAM4_HOME not found in your environment.
    echo Please set the BEAM4_HOME variable in your environment to match the
    echo location of the BEAM 4.x installation
    echo
    exit 2
fi

. "$BEAM4_HOME/bin/detect_java.sh"

"$app_java_home/bin/java" \
    -Xmx1024M \
    -Dceres.context=beam \
    "-Dbeam.mainClass=org.esa.beam.framework.gpf.main.GPT" \
    "-Dbeam.home=$BEAM4_HOME" \
    "-Dncsa.hdf.hdflib.HDFLibrary.hdflib=$BEAM4_HOME/modules/lib-hdf-2.7/lib/libjhdf.so" \
    "-Dncsa.hdf.hdf5lib.H5.hdf5lib=$BEAM4_HOME/modules/lib-hdf-2.7/lib/libjhdf5.so" \
    -jar "$BEAM4_HOME/bin/ceres-launcher.jar" \
    WaterStatisticsOp \
    -Ssource="$ndwi" \
    -Smask="$mask" \
    -Pcsv="$csv" \
    -PregionName="$lake"

exit $?
