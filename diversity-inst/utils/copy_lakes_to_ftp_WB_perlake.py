#!/usr/bin/env python

from os import listdir, system
from os.path import exists

#srcBaseDir = '/home/olaf/diversity-inst/lakes-WB/v1-0/'
srcBaseDir = '/home/olaf/diversity-inst/lakes-WB/v1-1/'
#srcBaseDir = '/home/olaf/diversity-inst/lakes-WB/v1-2/'
#dstBaseDir = '/data/ftp/diversity/data/inland_waters/v1-0/'
dstBaseDir = '/data/ftp/diversity/data/inland_waters/v1-1/'
#dstBaseDir = '/data/ftp/diversity/data/inland_waters/v1-2/'

# 2017 WB new processing and reprocessing
#regionsFile = open("./lakes-WB-new-regions.txt","r")
regionsFile = open("./lakes-WB-repro2017-regions.txt","r")

regions = []
for line in regionsFile:
    name = line.strip().replace('\'', '')
    regions.append(name)

#regions = ['Lake-Maracaibo'] # test!
regions = ['Lake-Comacchio'] # test!
#regions = ['Lake-Constance'] # test!
#regions = ['Lake-Razelm'] # test!
#regions = ['Lake-Er', 'Lake-Erie', 'Lake-Veneta', 'Lake-Winnipegosis'] # test!
#regions = ['Lake-Fagnano', 'Lake-Llanquihue', 'Lake-Nahuel_huapi'] # test!

#subDirs = ['l3-10y', 'l3-monthly', 'l3-yearly']
subDirs = ['.']
#excludes = ['_temporary', '_SUCCESS', '*_OWT-test', '_processing_metadata', 'part-r-00000']
excludes = []
exclStr = ''
for e in excludes:
    exclStr += ' --exclude=' + e + ' '

for item in regions:
    for subDir in subDirs:
        srcLakesDir = srcBaseDir + item + '/' + subDir + '/'
        dstLakesDir = dstBaseDir + item + '/' + subDir + '/'
        if exists(srcLakesDir):
            #cmd = "rsync -avOP " + exclStr + srcLakesDir + " olafd@bcserver8:" + dstLakesDir
            cmd = "rsync -avOP " + exclStr + srcLakesDir + " olafd@bcserver8:" + dstLakesDir
            #cmd = "rsync -av " + exclStr + srcLakesDir + " -e 'ssh -l olafd' bcserver8:" + dstLakesDir

            system(cmd)
        else:
            print(srcLakesDir, " does not exist!")

