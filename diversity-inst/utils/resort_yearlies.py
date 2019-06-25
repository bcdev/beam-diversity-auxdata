__author__ = 'olafd'

# This is a script to copy the yearly lakes nc files from each lake one folder up
# call: python resort_yearlies.py

import os
import sys
import subprocess

if len(sys.argv) != 1:
    print 'Usage:  python rename_l3_10y_products.py'
    sys.exit(-1)

root_folder = '/calvalus/projects/diversity'

years = ['2008']

print 'starting...'
for year in years:
    yearlies_nc_dir = '/mnt/hdfs' + root__folder + '/yearly-nc/' + year
    if os.path.exists(yearlies_nc_dir):
        regions = os.listdir(yearlies_nc_dir)
        if len(regions) > 0:
            for index in range(0, len(regions)):            
                if regions[index].find("Lake-") != -1 and regions[index].find(".nc.gz") != -1:
                    mvcommand = "hadoop fs -mv " + yearlies_nc_dir + os.sep + regions[index] + " " + yearlies_nc_dir
                    print mvcommand
                    #os.popen(mvcommand)
print 'done.'
