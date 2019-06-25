__author__ = 'olafd'

# This is a script to remname marked blacklisted files back to original name as mosaicking entry (reset to initial state)
# call: python show_monthlies.py

import os
import sys
import subprocess
from os import path
from sys import stderr, argv

def main(argv):

    if len(sys.argv) != 1:
        print 'Usage:  python show_monthlies.py'
        sys.exit(-1)

    root_folder = '/calvalus/projects/diversity'

    years = ['2003', '2004', '2005', '2006', '2007', '2008', '2009', '2010', '2011']
    #years = ['2006']
    allMonths = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']
    #allMonths = ['04']

    print 'starting...'
    for year in years:
        for month in allMonths:
            monthlies_dir = '/mnt/hdfs' + root_folder + '/monthly/' + year + '/' + month
            monthly_files = os.listdir(monthlies_dir)
            if len(monthly_files) > 0:
                for index in range(0, len(monthly_files)):
                    if monthly_files[index].endswith(".tif.BLACKLISTED"):
                        monthly_file = monthly_files[index]

                        orig_file_end_index = str.find(monthly_file,".BLACKLISTED")
                        orig_file = monthly_file[:orig_file_end_index]

                        showcommand = 'mv ' + monthlies_dir + '/' + monthly_file + ' ' + monthlies_dir + '/' + orig_file
                        print showcommand
                        os.popen(showcommand)

if __name__ == '__main__':
    print >> stderr, argv
    main(argv)
