__author__ = 'olafd'

# This is a script to clean for all years per lake all results but not the shallow results

# example call: python clean_per_lake_allyears.py Lake-Balaton

import os
import sys
import subprocess

if len(sys.argv) != 2:
    print 'Usage:  python clean_per_lake_allyears.py <lake>'
    print 'example call:  clean_per_lake_allyears.py Lake-Balaton'
    sys.exit(-1)

root_folder = '/calvalus/projects/diversity/prototype/'
region = sys.argv[1]
years = ['2002','2003','2004','2005','2006','2007','2008','2009','2010','2011','2012']

print 'starting...'

lake_folder = root_folder + region
yearly_subdirs_to_delete = ['l2-ccl2w','l2-fub', 'l2-idepix','l3-shallow-L3-1', 'l3-monthly', 'l3-yearly', 'l2-mph' ]
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

