__author__ = 'olafd'

# This is a script to clean for all NEW lakes and years all results but not the shallow results and not the final results (l3-monthly, l3-yearly, l3-10y)

# python clean_allnewlakes_allyears.py

import os
import sys
import subprocess

if len(sys.argv) != 1:
    print 'Usage:  python clean_alllakes_allyears.py'
    sys.exit(-1)

root_folder = '/calvalus/projects/diversity/prototype/'

years = ['2002', '2003', '2004', '2005', '2006', '2007', '2008', '2009', '2010', '2011', '2012']
#years = ['2005']

northRegionsFile = open("./wkt/lakes-north-regions.txt","r")
northRegions = []
for line in northRegionsFile:
    name = line.strip().replace('\'', '')
    northRegions.append(name)

southRegionsFile = open("./wkt/lakes-south-regions.txt","r")
southRegions = []
for line in southRegionsFile:
    name = line.strip().replace('\'', '')
    southRegions.append(name)

regions = northRegions + southRegions

# the 47 OLD lakes:
northRegionsOld = ['Lake-Alakol', 'Lake-Aral', 'Lake-Baikal', 'Lake-Balaton', 'Lake-Biwa',
                'Lake-Caspian', 'Lake-Chapala', 'Lake-Chilka', 'Lake-Constance', 'Lake-Egirdir',
                'Lake-Erie', 'Lake-Garda', 'Lake-Geneva', 'Lake-Great_Slave', 'Lake-Hjalmaren',
                'Lake-Huron', 'Lake-Issykkul', 'Lake-Kasumigaura', 'Lake-Malaren', 'Lake-Michigan',
                'Lake-Murray', 'Lake-Neagh', 'Lake-Neuchatel', 'Lake-Nicaragua', 'Lake-Ontario',
                'Lake-Paijanne', 'Lake-Peipus', 'Lake-Superior', 'Lake-Tahoe', 'Lake-Taihu',
                'Lake-Vanern', 'Lake-Vattern', 'Lake-Winnipeg', 'Lake-Woods' ]
southRegionsOld = ['Lake-Alexandrina', 'Lake-Champlain', 'Lake-Chilwa', 'Lake-Colhue_Huapi', 'Lake-Edward',
                'Lake-Eyasi', 'Lake-Eyre', 'Lake-Kivu', 'Lake-Mangueira', 'Lake-Natron',
                'Lake-Victoria', 'Lake-Tanganyika', 'Lake-Titicaca' ]
regionsOld = northRegionsOld + southRegionsOld


print 'starting...'

#yearly_subdirs_to_delete = ['case2r', 'ccl2w', 'fub', 'idepix', 'l3-monthly', 'l3-monthly-intermediate', 'l3-monthly-intermediate1', 'l3-monthly-intermediate2', 'l3-yearly', 'l3-yearly-intermediate', 'mph' ]
yearly_subdirs_to_delete = ['case2r', 'ccl2w', 'fub', 'idepix', 'l3-monthly-intermediate', 'l3-monthly-intermediate1', 'l3-monthly-intermediate2', 'l3-yearly-intermediate', 'mph' ]
#tenyear_subdirs_to_delete = ['l3-10y', 'l3-10y-intermediate', 'l3-10y-intermediate-L3-1', 'l3-10y-intermediate-L3-output',  ]
tenyear_subdirs_to_delete = ['l3-10y-intermediate', 'l3-10y-intermediate-L3-1', 'l3-10y-intermediate-L3-output',  ]

for year in years:
    for region in regions:
	if region not in regionsOld:
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
