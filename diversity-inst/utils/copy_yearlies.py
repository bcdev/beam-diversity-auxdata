__author__ = 'olafd'

# This is a script to copy the yearly lakes tiff files from each lake to the 'yearly' folder for mosaicking
# call: python copy_yearlies.py

import os
import sys
import subprocess

if len(sys.argv) != 1:
    print 'Usage:  python copy_yearlies.py'
    sys.exit(-1)

root_folder = '/calvalus/projects/diversity'
lakes_orig_folder = root_folder + '/lakes-WB/existing_lakes'

#years = ['2003']
years = ['2003','2004','2005','2006','2007','2008','2009','2010','2011']

print 'starting...'
for year in years:
    yearlies_dir = '/mnt/hdfs' + root_folder + '/yearly/' + year
    if os.path.exists(yearlies_dir):
        regions = os.listdir(lakes_orig_folder)
        if len(regions) > 0:
            for index in range(0, len(regions)):
                cpcommand = "hadoop fs -cp -f " + lakes_orig_folder + os.sep + regions[index] + os.sep + "l3-yearly" + os.sep + year + "-L3-1" + os.sep + regions[index] + "*.tif " + root_folder + '/yearly/' + year
                print cpcommand
                os.popen(cpcommand)
print 'done.'
