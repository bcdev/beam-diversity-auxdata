#!/usr/bin/env python

from os import system
from os.path import exists

dstBaseDir = '/calvalus/projects/diversity/lakes-WB/existing_lakes/'
#dstBaseDir = '/calvalus/projects/diversity/prototype/'
srcBaseDir = '/data/ftp/diversity/data/inland_waters/v1-1/'
#srcBaseDir = '/data/ftp/diversity/data/inland_waters/v1-0/'

#tifsZerosizeFile = open("./tifs_zerosize_lakes_new_WB.txt","r")
tifsZerosizeFile = open("./tifs_zerosize_lakes_reprocessed_WB.txt","r")

tifs = []
for line in tifsZerosizeFile:
    tif = line.strip().replace('\'', '')
    tifs.append(tif)

#tifs = ['Lake-DAL_2010-02-01_2010-02-28.tif', 'Lake-FALCON_2011-01-01_2011-12-31.tif']  # test

for tif in tifs:
    # e.g. tif = 'Lake-AZUCAR_2005-08-01_2005-08-31.tif'
    print('tif: ', tif)

    tif_lake_end = tif.find('_2')

    tif_lake = tif[0:tif_lake_end]  # 'Lake-AZUCAR'  
    #print('tif_lake: ', tif_lake)

    tif_year = tif[tif_lake_end+1:tif_lake_end+5]
    #print('tif_year: ', tif_year)

    tif_month_1 = tif[tif_lake_end+6:tif_lake_end+8]
    #print('tif_month_1: ', tif_month_1)
    tif_month_2 = tif[tif_lake_end+17:tif_lake_end+19]
    #print('tif_month_2: ', tif_month_2)

    if tif_month_1 == '01' and tif_month_2 == '12':
        cmd = "rsync -avOP " + srcBaseDir + tif_lake + '/l3-yearly/' + tif_year + '-L3-1/' + tif  + " olafd@bcserver8:" + dstBaseDir + tif_lake
    else:
        cmd = "rsync -avOP " + srcBaseDir + tif_lake + '/l3-monthly/' + tif_year + '/' + tif_month_1 + '-L3-1/' + tif  + " olafd@bcserver8:" + dstBaseDir + tif_lake
    
    system(cmd)
    print('cmd: ', cmd)

