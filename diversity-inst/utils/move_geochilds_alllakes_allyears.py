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

years = ['2002', '2003', '2004', '2005', '2006', '2007', '2008', '2009', '2010', '2011', '2012']

northRegionsFile = open("./wkt/lakes-north-regions.txt","r")
northRegions = []
for line in northRegionsFile:
    name = line.strip().replace('\'', '')
    northRegions.append(name)

southRegionsFile = open("./wkt/lakes-south-regions.txt","r")
southRegions = []
for line in southRegionsFile:
    name = line.strip().replace('\'', '')
    southRegions.append(name)

regions = northRegions + southRegions
#regions = ['Lake-Abe']  # test!

print 'starting...'

for region in regions:
    lake_folder = root_folder + region
    if os.path.exists('/mnt/hdfs' + lake_folder) and not os.path.exists('/mnt/hdfs' + lake_folder + os.sep + "l1-child"):
        mkdircommand = "hadoop fs -mkdir " + lake_folder + os.sep + "l1-child"
        print mkdircommand
        os.popen(mkdircommand)
        for year in years:
            if os.path.exists('/mnt/hdfs' + lake_folder + os.sep + year):
                mvcommand = "hadoop fs -mv " + lake_folder + os.sep + year + " " + lake_folder + os.sep + "l1-child"
                print mvcommand
                os.popen(mvcommand)

print 'done.'
