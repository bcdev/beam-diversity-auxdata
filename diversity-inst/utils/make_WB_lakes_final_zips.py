__author__ = 'olafd'

# This is a script to generate the ftp final zip files for all new or reprocessed WB lakes

# python make_WB_lakes_final_zips.py

import os
import fnmatch
import sys
import subprocess

if len(sys.argv) != 1:
    print 'Usage:  python make_WB_lakes_final_zips.py'
    sys.exit(-1)

years  = [ '2002','2003', '2004', '2005', '2006', '2007', '2008' ,'2009', '2010', '2011', '2012' ]
months  = [ '01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12' ]

#root_folder = '/calvalus/projects/diversity/prototype/'
#root_folder = '/calvalus/projects/diversity/lakes-WB/'
root_folder = '/calvalus/projects/diversity/lakes-WB/existing_lakes/'
#target_root_folder = '/home/olaf/diversity-inst/lakes-WB/v1-0/'
target_root_folder = '/home/olaf/diversity-inst/lakes-WB/v1-1/'
#target_root_folder = '/home/olaf/diversity-inst/lakes-WB/v1-2/'

#regionsIDsFile = open("./DIV2_300-lakes_IDs.txt","r")
#regionsIDsFile = open("./DIV2_WB-new-lakes_IDs.txt","r")
regionsIDsFile = open("./DIV2_WB-repro2017-lakes_IDs.txt","r")

regions = []
for line in regionsIDsFile:
    region = line.split(',')
    regions.append(region)
#    print 'Region: ', region

#regions = [ ['0352', 'CONSTANCE', 'Lake-Constance'] ] # test
#regions = [ ['0358', 'RAZELM', 'Lake-Razelm'] ] # test
#regions = [ ['9759', 'MARACAIBO', 'Lake-Maracaibo'] ] # test
regions = [ ['1941', 'COMACCHIO', 'Lake-Comacchio'] ] # test
#regions = [ 
#['0767', 'ER', 'Lake-Er'], 
#['0012', 'ERIE', 'Lake-Erie'], 
#['0000', 'VENETA', 'Lake-Veneta'], 
#['0031', 'WINNIPEGOSIS', 'Lake-Winnipegosis'], 
#] # test
#regions = [
#['0304', 'FAGNANO', 'Lake-Fagnano'],
#['0209', 'LLANQUIHUE', 'Lake-Llanquihue'],
#['0343', 'NAHUEL_HUAPI', 'Lake-Nahuel_huapi']
#] # test


for region in regions:
    id = region[0]
    nameOrig = region[1]
    nameTarget = region[2].strip()

    regionPath = root_folder + nameTarget
    targetRegionPath = target_root_folder + nameTarget
    if os.path.exists(regionPath):
        print regionPath
        print targetRegionPath
        l3Dirs = fnmatch.filter(os.listdir(regionPath), 'l3*')

        # l3dirs must exist for new zipping
        if len(l3Dirs) > 0:
            mkdircommand = "mkdir -p " + targetRegionPath
            print mkdircommand
            os.popen(mkdircommand)

            #zip10ycommand = "tar czvf " + targetRegionPath + "/zip/D2ID" + id + "_" + nameTarget.upper() + "_10y.tar.gz " + regionPath + "/l3-decade-L3-1/" + nameTarget + "*.tif"
            zip10ycommand = "zip -j " + targetRegionPath + "/D2ID" + id + "_" + nameTarget.upper() + "_10y.zip " + regionPath + "/l3-decade-L3-1/" + nameTarget + "*.tif"
            print zip10ycommand
            os.popen(zip10ycommand)
            #zipyearlycommand = "tar czvf " + targetRegionPath + "/zip/D2ID" + id + "_" + nameTarget.upper() + "_yearly.tar.gz " + regionPath + "/l3-yearly/*/" + nameTarget + "*.tif"
            zipyearlycommand = "zip -j " + targetRegionPath + "/D2ID" + id + "_" + nameTarget.upper() + "_yearly.zip " + regionPath + "/l3-yearly/*/" + nameTarget + "*.tif"
            print zipyearlycommand
            os.popen(zipyearlycommand)

            for year in years:
                #zipmonthlycommand = "tar czvf " + targetRegionPath + "/zip/D2ID" + id + "_" + nameTarget.upper() + "_monthly" + year + ".tar.gz " + regionPath + "/l3-monthly/" + year + "/*/" + nameTarget + "*.tif"
                zipmonthlycommand = "zip -j " + targetRegionPath + "/D2ID" + id + "_" + nameTarget.upper() + "_monthly" + year + ".zip " + regionPath + "/l3-monthly/" + year + "/*/" + nameTarget + "*.tif"
                print zipmonthlycommand
                os.popen(zipmonthlycommand)

            #rmcommand = "rm -Rf " + regionPath + "/l3-*"
            #print rmcommand
            #os.popen(rmcommand)

print 'done.'

