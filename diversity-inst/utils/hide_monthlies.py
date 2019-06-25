__author__ = 'olafd'

# This is a script to rename lakes tiff files as 'blacklisted' to hide from mosaicking, considering the blacklists per lake and band
# call: python copy_monthlies.py <band> <year> <month>
# e.g.: python copy_monthlies.py tsm_cc_mean 2005 05

import os
import sys
import subprocess
from os import path
from sys import stderr, argv

def main(argv):

    if len(sys.argv) != 2:
        print 'Usage:  python copy_monthlies.py <band>'
        sys.exit(-1)

    root_folder = '/calvalus/projects/diversity'

    years = ['2003', '2004', '2005', '2006', '2007', '2008', '2009', '2010', '2011']
    #years = ['2006']
    allMonths = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']
    #allMonths = ['04']

    band = argv[1]

    print 'starting...'
    for year in years:
        for month in allMonths:
            monthlies_dir = '/mnt/hdfs' + root_folder + '/monthly/' + year + '/' + month
            monthly_files = os.listdir(monthlies_dir)
            if len(monthly_files) > 0:
                for index in range(0, len(monthly_files)):
                    monthly_file = monthly_files[index]
                    hide_file = False
                    
                    region_end_index = str.find(monthly_file,"_")
                    region = monthly_file[:region_end_index]
                    black_dates = []
                    black_dates_file_name = root_folder + "/blacklists-by-param/" + band + "_blacklists/blacklist_" + region + "_" + band + ".txt"
                    if os.path.isfile(black_dates_file_name):
                        black_dates_file = open(black_dates_file_name,"r")
                        if black_dates_file:
                            for line in black_dates_file:
                                black_date = line.strip()
                                black_dates.append(black_date)

                        for black_date in black_dates:
                            black_year = black_date[0:4]
                            black_month = black_date[5:7]
                            if year == black_year and month == black_month:
                                hide_file = True
                                break

                        if hide_file:
                            #hidecommand = "mv " + monthlies_dir + '/' + monthly_file + " " + monthlies_dir + '/blacklisted'
                            hidecommand = "mv " + monthlies_dir + '/' + monthly_file + " " + monthlies_dir + '/' + monthly_file + '.BLACKLISTED'
                            print hidecommand
                            os.popen(hidecommand)

if __name__ == '__main__':
    print >> stderr, argv
    main(argv)
