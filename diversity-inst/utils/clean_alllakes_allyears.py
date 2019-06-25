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

#northRegionsFile = open("./wkt/lakes-north-regions.txt","r")
northRegionsFile = open("./wkt/lakes-north-regions_to_delete_20150401.txt","r")
northRegions = []
for line in northRegionsFile:
    name = line.strip().replace('\'', '')
    northRegions.append(name)

southRegionsFile = open("./wkt/lakes-south-regions.txt","r")
southRegions = []
#for line in southRegionsFile:
#    name = line.strip().replace('\'', '')
#    southRegions.append(name)

regions = northRegions + southRegions
#regions = ['Lake-Eyre']
#regions = ['Lake-Bardawil']
#regions = ['Lake-Chilka', 'Lake-Amadjuak', 'Lake-Atlin', 'Lake-Bear', 'Lake-Colhue_huapi', 'Lake-Cree', 'Lake-Cross']

print 'starting...'

for region in regions:

    lake_folder = root_folder + region
    yearly_subdirs_to_delete = ['l2-ccl2w','l2-fub', 'l2-idepix', 'l2-mph', 'l3-shallow-L3-1', 'l3-monthly', 'l3-yearly'] 
    tenyear_subdirs_to_delete = ['l3-decade-L3-1' ]

    print 'lake_folder: ', lake_folder

    if os.path.exists('/mnt/hdfs' + lake_folder):
        for subdir in yearly_subdirs_to_delete:
            if os.path.exists('/mnt/hdfs' + lake_folder + os.sep + subdir):
                rmcommand = "hadoop fs -rm -r " + lake_folder + os.sep + subdir
                print rmcommand
                os.popen(rmcommand)

    if os.path.exists('/mnt/hdfs' + lake_folder):
        for subdir in tenyear_subdirs_to_delete:
            if os.path.exists('/mnt/hdfs' + lake_folder + os.sep + subdir):
                rmcommand = "hadoop fs -rm -r " + lake_folder + os.sep + subdir
                print rmcommand
                os.popen(rmcommand)

print 'done.'
