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
#years = ['2003']

#regionsFile = open("./wkt/lakes-regions-repro2016-251_300.txt","r")
regionsFile = open("./wkt/lakes-regions-repro2016-301_347.txt","r")
regions = []
for line in regionsFile:
    name = line.strip().replace('\'', '')
    regions.append(name)

#regions = ['Lake-Victoria']

print 'starting...'

#yearly_subdirs_to_delete = ['case2r', 'ccl2w', 'fub', 'idepix', 'l3-monthly', 'l3-monthly-intermediate', 'l3-monthly-intermediate1', 'l3-monthly-intermediate2', 'l3-yearly', 'l3-yearly-intermediate', 'mph' ]
yearly_subdirs_to_delete = [ 'l1-child' ]

for year in years:
    for region in regions:
        lake_folder = root_folder + region
        #print 'lake: ', lake_folder         
        #if os.path.exists('/mnt/hdfs' + lake_folder):
        if os.path.exists(lake_folder):
            for subdir in yearly_subdirs_to_delete:
                if os.path.exists('/mnt/hdfs' + lake_folder + os.sep + subdir):
                    rmcommand = "hadoop fs -rm -r " + lake_folder + os.sep + subdir + os.sep + year
                    print rmcommand
                    os.popen(rmcommand)

print 'done.'
