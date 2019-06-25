__author__ = 'olafd'

# This is a script to clean for all lakes per year all results but not the shallow results

# example call: python clean_alllakes_per_year.py 2005

import os
import sys
import subprocess

if len(sys.argv) != 2:
    print 'Usage:  python clean_alllakes_per_year.py 2005 <year>'
    print 'example call:  python clean_alllakes_per_year.py 2005'
    sys.exit(-1)

root_folder = '/calvalus/projects/diversity/prototype/'
year = sys.argv[1]

# the 47 lakes:
northRegions = ['Lake-Alakol', 'Lake-Aral', 'Lake-Baikal', 'Lake-Balaton', 'Lake-Biwa',
                'Lake-Caspian', 'Lake-Chapala', 'Lake-Chilka', 'Lake-Constance', 'Lake-Egirdir',
                'Lake-Erie', 'Lake-Garda', 'Lake-Geneva', 'Lake-Great_Slave', 'Lake-Hjalmaren',
                'Lake-Huron', 'Lake-Issykkul', 'Lake-Kasumigaura', 'Lake-Malaren', 'Lake-Michigan',
                'Lake-Murray', 'Lake-Neagh', 'Lake-Neuchatel', 'Lake-Nicaragua', 'Lake-Ontario',
                'Lake-Paijanne', 'Lake-Peipus', 'Lake-Superior', 'Lake-Tahoe', 'Lake-Taihu',
                'Lake-Vanern', 'Lake-Vattern', 'Lake-Winnebago', 'Lake-Winnipeg', 'Lake-Woods' ]
southRegions = ['Lake-Alexandrina', 'Lake-Champlain', 'Lake-Chilwa', 'Lake-Colhue_Huapi', 'Lake-Edward',
                'Lake-Eyasi', 'Lake-Eyre', 'Lake-Kivu', 'Lake-Mangueira', 'Lake-Natron',
                'Lake-Victoria', 'Lake-Tanganyika', 'Lake-Titicaca' ]

regions = northRegions + southRegions
#regions = ['Lake-Victoria']

print 'starting...'

yearly_subdirs_to_delete = ['case2r', 'ccl2w','fub', 'idepix', 'l3-monthly', 'l3-monthly-intermediate', 'l3-monthly-intermediate1', 'l3-monthly-intermediate2', 'l3-yearly', 'l3-yearly-intermediate', 'mph' ]
tenyear_subdirs_to_delete = ['l3-10y', 'l3-10y-intermediate' ]

for region in regions:
    lake_folder = root_folder + region
    if os.path.exists('/mnt/hdfs' + lake_folder):
        for subdir in yearly_subdirs_to_delete:
            if os.path.exists('/mnt/hdfs' + lake_folder + os.sep + subdir):
                rmcommand = "hadoop fs -rm -r " + lake_folder + os.sep + subdir + os.sep + year
                print rmcommand
                os.popen(rmcommand)

    if os.path.exists('/mnt/hdfs' + lake_folder):
        for subdir in tenyear_subdirs_to_delete:
            if os.path.exists('/mnt/hdfs' + lake_folder + os.sep + subdir):
                rmcommand = "hadoop fs -rm -r " + lake_folder + os.sep + subdir
                print rmcommand
                os.popen(rmcommand)

print 'done.'
