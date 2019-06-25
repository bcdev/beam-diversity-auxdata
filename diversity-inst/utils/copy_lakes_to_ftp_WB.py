#!/usr/bin/env python

from os import listdir, system
from os.path import exists

#srcBaseDir = '/home/olaf/diversity-inst/lakes-WB/v1-1'
srcBaseDir = '/home/olaf/diversity-inst/lakes-WB/v1-2'
dstBaseDir = '/data/ftp/diversity/data/inland_waters'

# 2017 WB new processing and reprocessing
if exists(srcBaseDir):
    cmd = "rsync -avOP " + srcBaseDir + " olafd@bcserver8:" + dstBaseDir
    system(cmd)
else:
    print(srcLakesDir, " does not exist!")

