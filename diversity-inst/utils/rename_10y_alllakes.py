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
#regions = ['Lake-Victoria']

print 'starting...'

tenyear_subdir = ['l3-10y']
#tenyear_subdir = []

for region in regions:
    lake_folder = root_folder + region
    print 'lake folder: ', lake_folder 
    if os.path.exists('/mnt/hdfs' + lake_folder):
        path_wrong = lake_folder + os.sep + tenyear_subdir + os.sep + region + '_2003-01-01_2012-12-30.tif'
	print 'path: ', path_wrong
        path_right = lake_folder + os.sep + tenyear_subdir + os.sep + region + '_2003-01-01_2011-12-31.tif'
        if os.path.exists('/mnt/hdfs' + path_wrong):
            cpcommand = "hadoop fs -cp " + path_wrong + ' ' + path_right
            print cpcommand
            #os.popen(cpcommand)
            rmcommand = "hadoop fs -rm " + path_wrong
            print rmcommand
            #os.popen(rmcommand)


print 'done.'
