#!/bin/bash
set -e

# sequence of rename commands to generate the lake polygons in a file with name of the lake: 
# e.g. ESACCI-LC-L4-SAR-WB-300m-v2-0_ZILING.wkt renamed as ZILING.wkt:
#rename -v "s/.*v2-0_//" *.wkt
# e.g. ZILING.wkt renamed as ziling.wkt:
#rename -v 'y/A-Z/a-z/' *.wkt
# e.g. ziling.wkt renamed as Ziling.Wkt:
#rename -v 's/\b(\w)/\u$1/g' *.wkt
#e.g. Ziling.Wkt renamed as Ziling.wkt:
#rename -v 's/\.Wkt$/\.wkt/' *.Wkt
#e.g. Ziling.wkt renamed as Lake-Ziling.wkt:
#rename -v 's/(.*)$/Lake-$1/' *.wkt
#e.g. Lake-Ziling.wkt renamed as Lake-Ziling:
#rename -v 's/\.wkt$//' *.wkt

# generate text file with all 300 lakes vs. polygons which can directly be pasted or read into prototype*.py:
#for filename in Lake*; do echo "'$filename' : '`cat $filename`'" >> lakes-polygons.txt; done;

# generate text file with all ARC lakes name vs. dummy ID:
# (do only once, enter the names manually, we do not have a proper list)
# for filename in Lake*; do echo "'$filename' : '0000'" >> arc-lakes-alids.txt; done;
# DONE, 20140820
# moved the result file to ../ARC, 20140821

# generate text file with names of all 300 lakes separated by ',', to be used to define the regions:
#for filename in Lake*; do echo "'$filename'" >> lakes-north-regions.txt; done;
#for filename in Lake*; do echo "'$filename'" >> lakes-south-regions.txt; done;
