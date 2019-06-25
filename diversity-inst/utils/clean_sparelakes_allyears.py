__author__ = 'olafd'

# This is a script to clean for spare lakes and years all results but not the shallow results

# python clean_alllakes_allyears.py

import os
import sys
import subprocess

if len(sys.argv) != 1:
    print 'Usage:  python clean_sparelakes_allyears.py'
    sys.exit(-1)

root_folder = '/calvalus/projects/diversity/prototype/'

#years = ['2008', '2009', '2010', '2011', '2012']
years = ['2002', '2003', '2004', '2005', '2006', '2007', '2008', '2009', '2010', '2011', '2012']

# read the regions for the spare lakes:
#regionsFile = open("./wkt/lakes-regions-spare.txt","r")
regionsFile = open("./wkt/lakes-regions-repro2017.txt","r")
regions = []
for line in regionsFile:
    name = line.strip().replace('\'', '')
    regions.append(name)

print 'starting...'

#yearly_subdirs_to_delete = ['l3-monthly', 'l3-yearly', 'l2-mph', 'l2-fub', 'l2-ccl2w']
yearly_subdirs_to_delete = ['l3-monthly', 'l3-yearly', 'l3-shallow-L3-1', 'l3-decade-L3-1']

for year in years:
    for region in regions:
        lake_folder = root_folder + region
        if os.path.exists('/mnt/hdfs' + lake_folder):
            for subdir in yearly_subdirs_to_delete:
                if os.path.exists('/mnt/hdfs' + lake_folder + os.sep + subdir):
                    print 'hier 5' 
                    rmcommand = "hadoop fs -rm -r " + lake_folder + os.sep + subdir + os.sep + year
                    print rmcommand
                    os.popen(rmcommand)

print 'done.'
