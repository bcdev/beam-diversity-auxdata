__author__ = 'olafd'

# This is a script to clean for all lakes and years all results but not the shallow results

# python clean_alllakes_allyears.py

import os
import sys
import subprocess

if len(sys.argv) != 1:
    print 'Usage:  python clean_alllakes_allyears.py'
    sys.exit(-1)

root_folder = '/calvalus/projects/diversity/prototype/'

#years = ['2006', '2007', '2008', '2009', '2010', '2011', '2012']
years = ['2002', '2003', '2004', '2005', '2006', '2007', '2008', '2009', '2010', '2011', '2012']
#years = ['2002', '2003', '2004', '2005', '2006', '2007']

northRegionsFile = open("./wkt/lakes-north-regions_100.txt","r")
northRegions = []
for line in northRegionsFile:
    name = line.strip().replace('\'', '')
    northRegions.append(name)

southRegionsFile = open("./wkt/lakes-south-regions_100.txt","r")
southRegions = []
for line in southRegionsFile:
    name = line.strip().replace('\'', '')
    southRegions.append(name)

regions = northRegions + southRegions
#regions = ['Lake-Victoria']
#regions = ['Lake-Bardawil']
#regions = ['Lake-Chilka', 'Lake-Amadjuak', 'Lake-Atlin', 'Lake-Bear', 'Lake-Colhue_huapi', 'Lake-Cree', 'Lake-Cross']

print 'starting...'

tenyear_subdirs_to_delete = ['idepix_shallow', 'l3-shallow', 'l3-shallow-L3-1', 'l3-shallow-L3-output', 'l3-shallow-post', 'l3-shallow-post-tiff'  ]
#tenyear_subdirs_to_delete = []

for region in regions:
    lake_folder = root_folder + region
#    print 'lake folder: ', lake_folder
    if os.path.exists('/mnt/hdfs' + lake_folder):
        for subdir in tenyear_subdirs_to_delete:
            if os.path.exists('/mnt/hdfs' + lake_folder + os.sep + subdir):
                rmcommand = "hadoop fs -rm -r " + lake_folder + os.sep + subdir
                print rmcommand
                os.popen(rmcommand)

print 'done.'
