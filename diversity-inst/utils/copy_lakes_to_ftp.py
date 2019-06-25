#!/usr/bin/env python

from os import listdir, system
from os.path import exists

srcBaseDir = '/mnt/hdfs/calvalus/projects/diversity/prototype/'
dstBaseDir = '/data/ftp/diversity/data/prototype_products/inland-waters_perennial/'

# the first 50 lakes:
#lakes = ['Lake-Alakol', 'Lake-Alexandrina', 'Lake-Aral', 'Lake-Baikal', 'Lake-Balaton', 'Lake-Biwa', 'Lake-Caspian', 'Lake-Champlain', 'Lake-Chapala', 'Lake-Chilka', 'Lake-Chilwa', 'Lake-Colhue_Huapi', 'Lake-Constance', 'Lake-Edward', 'Lake-Egirdir', 'Lake-Erie', 'Lake-Eyasi', 'Lake-Eyre', 'Lake-Garda', 'Lake-Geneva', 'Lake-Great_slave', 'Lake-Hjalmaren', 'Lake-Huron', 'Lake-Issykkul', 'Lake-Kasumigaura', 'Lake-Kivu', 'Lake-Logipi', 'Lake-Malaren', 'Lake-Mangueira', 'Lake-Manyara', 'Lake-Michigan', 'Lake-Murray', 'Lake-Natron', 'Lake-Neagh', 'Lake-Neuchatel', 'Lake-Nicaragua', 'Lake-Ontario', 'Lake-Paijanne', 'Lake-Peipus', 'Lake-Superior', 'Lake-Tahoe', 'Lake-Taihu', 'Lake-Tanganyika', 'Lake-Titicaca', 'Lake-Vanern', 'Lake-Vattern', 'Lake-Victoria', 'Lake-Winnipeg', 'Lake-Woods']

# the 4 Mexican lakes (special request by DO, KS, 20140923):
#lakes = ['Lake-Bonxan', 'Lake-Canitzan', 'Lake-Catazaja', 'Lake-Mandinga']
lakes = ['Lake-Cross']
subDirs = ['l3-10y', 'l3-monthly', 'l3-yearly']
excludes = ['_temporary', '_SUCCESS', '*_OWT-test']
exclStr = ''
for e in excludes:
    exclStr += ' --exclude=' + e + ' '


for item in lakes:
    for subDir in subDirs:
        srcLakesDir = srcBaseDir + item + '/' + subDir + '/'
        dstLakesDir = dstBaseDir + item + '/' + subDir + '/'
        if exists(srcLakesDir):
#            cmd = "rsync -avP " + exclStr + srcLakesDir + " uwe@bcserver8:" + dstLakesDir
            cmd = "rsync -avP " + exclStr + srcLakesDir + " olafd@bcserver8:" + dstLakesDir
            system(cmd)
        else:
            print(srcLakesDir, " does not exist!")

