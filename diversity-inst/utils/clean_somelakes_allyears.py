__author__ = 'olafd'

# This is a script to clean for SOME lakes and all years all results

# python clean_somelakes_allyears.py

import os
import sys
import subprocess

if len(sys.argv) != 1:
    print 'Usage:  python clean_somelakes_allyears.py'
    sys.exit(-1)

root_folder = '/calvalus/projects/diversity/prototype/'

years = ['2002', '2003', '2004', '2005', '2006', '2007', '2008', '2009', '2010', '2011', '2012']
#years = ['2005']

regions = [
    'Lake-Bisina',
    'Lake-Brienz',
    'Lake-Chiemsee',
    'Lake-Chiquita',
    'Lake-Emborcacao',
    'Lake-Ginebra',
    'Lake-Great_salt',
    'Lake-Ijsselmeer',
    'Lake-Iseo',
    'Lake-Lucerne',
    'Lake-Markermeer',
    'Lake-Muggelsee',
    'Lake-Naivasha',
    'Lake-Rogaguado',
    'Lake-Scutari',
    'Lake-Sempach',
    'Lake-Thun',
    'Lake-Trasimeno',
    'Lake-Zug',
    'Lake-Zurich'
]

print 'starting...'

yearly_subdirs_to_delete = ['l2-ccl2w', 'l2-fub', 'l2-idepix', 'l2-mph', 'l3-monthly', 'l3-yearly' ]
tenyear_subdirs_to_delete = ['l3-decade-L3-1', 'l3-shallow-L3-1',  ]

for year in years:
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
