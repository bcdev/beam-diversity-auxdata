__author__ = 'olafd'

# This is a script to copy the new shallow water tif files from each region into the aux data folder
# call: python copy_shallow_tiffs_to_aux.py

import os
import sys
import subprocess

if len(sys.argv) != 1:
    print 'Usage:  python copy_shallow_tiffs_to_aux.py'
    print 'example call:  python copy_shallow_tiffs_to_aux.py'
    sys.exit(-1)

root_folder = '/calvalus/projects/diversity'
#northRegions = ['Lake-Alakol', 'Lake-Aral', 'Lake-Baikal', 'Lake-Balaton', 'Lake-Biwa', 
#                'Lake-Caspian', 'Lake-Chapala', 'Lake-Chilka', 'Lake-Constance', 'Lake-Egirdir', 
#                'Lake-Erie', 'Lake-Garda', 'Lake-Geneva', 'Lake-Great_Slave', 'Lake-Hjalmaren', 
#                'Lake-Huron', 'Lake-Issykkul', 'Lake-Kasumigaura', 'Lake-Malaren', 'Lake-Michigan', 
#                'Lake-Murray', 'Lake-Neagh', 'Lake-Neuchatel', 'Lake-Nicaragua', 'Lake-Ontario', 
#                'Lake-Paijanne', 'Lake-Peipus', 'Lake-Superior', 'Lake-Tahoe', 'Lake-Taihu', 
#                'Lake-Vanern', 'Lake-Vattern', 'Lake-Winnipeg', 'Lake-Woods' ]
#southRegions = ['Lake-Alexandrina', 'Lake-Champlain', 'Lake-Chilwa', 'Lake-Colhue_Huapi', 'Lake-Edward', 
#                'Lake-Eyasi', 'Lake-Eyre', 'Lake-Kivu', 'Lake-Mangueira', 'Lake-Natron', 
#                'Lake-Victoria', 'Lake-Tanganyika', 'Lake-Titicaca' ]

northRegions = ['Lake-Aral','Lake-Murray']
southRegions = ['Lake-Chilwa', 'Lake-Colhue_Huapi', 'Lake-Eyasi']

regions = northRegions + southRegions

print 'starting...'
for region in regions:

    shallow_tiff_folder = root_folder + '/prototype/' + region + '/l3-shallow-post-tiff'
    aux_folder = root_folder + '/aux/shallow_new'
    if os.path.exists('/mnt/hdfs' + shallow_tiff_folder) and os.path.exists( '/mnt/hdfs' + aux_folder):
        shallow_tiff_files = os.listdir( '/mnt/hdfs' + shallow_tiff_folder)
        if len(shallow_tiff_files) > 0:
            # we do not want other files than L2_of_*.tif:
            for index in range(0, len(shallow_tiff_files)):
                if shallow_tiff_files[index].find("L2_of_") != -1 and shallow_tiff_files[index].find(".tif") != -1:
                    new_shallow_tiff_file_name = region + "_shallow-mask.tif"
                    cpcommand = "hadoop fs -cp " + shallow_tiff_folder + os.sep + shallow_tiff_files[index] + " " + aux_folder
                    print cpcommand
                    os.popen(cpcommand)

                    # if older file exists remove
                    if os.path.exists('/mnt/hdfs' + aux_folder + os.sep + new_shallow_tiff_file_name):
                        rmcommand = "hadoop fs -rm " + aux_folder + os.sep + new_shallow_tiff_file_name
                        print rmcommand
                        os.popen(rmcommand) 

                    # rename to a nicer name:
                    mvcommand = "hadoop fs -mv " + aux_folder + os.sep + shallow_tiff_files[index] + " " + aux_folder + os.sep + new_shallow_tiff_file_name
                    print mvcommand
                    os.popen(mvcommand)
print 'done.'
