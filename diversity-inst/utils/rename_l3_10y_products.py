__author__ = 'olafd'

# This is a script to copy the new shallow water tif files from each region into the aux data folder
# call: python rename_l3_10y_products.py

import os
import sys
import subprocess

if len(sys.argv) != 1:
    print 'Usage:  python rename_l3_10y_products.py'
    sys.exit(-1)

root_folder = '/calvalus/projects/diversity'
northRegions = ['Lake-Alakol', 'Lake-Aral', 'Lake-Baikal', 'Lake-Balaton', 'Lake-Biwa', 
                'Lake-Caspian', 'Lake-Chapala', 'Lake-Chilka', 'Lake-Constance', 'Lake-Egirdir', 
                'Lake-Erie', 'Lake-Garda', 'Lake-Geneva', 'Lake-Great_Slave', 'Lake-Hjalmaren', 
                'Lake-Huron', 'Lake-Issykkul', 'Lake-Kasumigaura', 'Lake-Malaren', 'Lake-Michigan', 
                'Lake-Murray', 'Lake-Neagh', 'Lake-Neuchatel', 'Lake-Nicaragua', 'Lake-Ontario', 
                'Lake-Paijanne', 'Lake-Peipus', 'Lake-Superior', 'Lake-Tahoe', 'Lake-Taihu', 
                'Lake-Vanern', 'Lake-Vattern', 'Lake-Winnipeg', 'Lake-Woods' ]
southRegions = ['Lake-Alexandrina', 'Lake-Champlain', 'Lake-Chilwa', 'Lake-Colhue_Huapi', 'Lake-Edward', 
                'Lake-Eyasi', 'Lake-Eyre', 'Lake-Kivu', 'Lake-Mangueira', 'Lake-Natron', 
                'Lake-Victoria', 'Lake-Tanganyika', 'Lake-Titicaca' ]

#northRegions = ['Lake-Aral','Lake-Murray']
#southRegions = ['Lake-Chilwa', 'Lake-Colhue_Huapi', 'Lake-Eyasi']

regions = northRegions + southRegions

print 'starting...'
for region in regions:
    l3_10y_folder = root_folder + '/prototype/' + region + '/l3-10y'
    if os.path.exists('/mnt/hdfs' + l3_10y_folder):
        l3_10y_files = os.listdir( '/mnt/hdfs' + l3_10y_folder)
        if len(l3_10y_files) > 0:
            # we do not want other files than Lake_*.tif:
            for index in range(0, len(l3_10y_files)):
                if l3_10y_files[index].find("Lake-") != -1 and l3_10y_files[index].find("2002-01-01") != -1 and l3_10y_files[index].find(".tif") != -1:
                    new_l3_10y_file_name = region + "_2003-01-01_2011-12-31.tif"
                    # rename to a new name:
                    mvcommand = "hadoop fs -mv " + l3_10y_folder + os.sep + l3_10y_files[index] + " " + l3_10y_folder + os.sep + new_l3_10y_file_name
                    print mvcommand
                    os.popen(mvcommand)
print 'done.'
