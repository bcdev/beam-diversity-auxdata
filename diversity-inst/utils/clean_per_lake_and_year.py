__author__ = 'olafd'

# This is a script to clean per lake and year all results but not the shallow results

# example call: python clean_per_lake_and_year.py Lake-Balaton 2003

import os
import sys
import subprocess

if len(sys.argv) != 3:
    print 'Usage:  python copy_shallow_tiffs_to_aux.py <lake> <year>'
    print 'example call:  clean_per_lake_and_year.py Lake-Balaton 2003'
    sys.exit(-1)

root_folder = '/calvalus/projects/diversity/prototype/'
region = sys.argv[1]
year = sys.argv[2]

print 'starting...'

lake_folder = root_folder + region
yearly_subdirs_to_delete = ['case2r', 'ccl2w','fub', 'idepix', 'l3-monthly', 'l3-monthly-intermediate', 'l3-monthly-intermediate1', 'l3-monthly-intermediate2', 'l3-yearly', 'l3-yearly-intermediate', 'mph' ]
if os.path.exists('/mnt/hdfs' + lake_folder):
    for subdir in yearly_subdirs_to_delete:
        if os.path.exists('/mnt/hdfs' + lake_folder + os.sep + subdir):
            rmcommand = "hadoop fs -rm -r " + lake_folder + os.sep + subdir + os.sep + year
            print rmcommand
            os.popen(rmcommand)

tenyear_subdirs_to_delete = ['l3-10y', 'l3-10y-intermediate' ]
if os.path.exists('/mnt/hdfs' + lake_folder):
    for subdir in tenyear_subdirs_to_delete:
        if os.path.exists('/mnt/hdfs' + lake_folder + os.sep + subdir):
            rmcommand = "hadoop fs -rm -r " + lake_folder + os.sep + subdir
            print rmcommand
            os.popen(rmcommand)

print 'done.'
