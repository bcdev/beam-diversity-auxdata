__author__ = 'olafd'

# This is a script to remove some folders
# call: python remove_tmp.py

import os
import sys
import subprocess

if len(sys.argv) != 1:
    print 'Usage:  python rename_l3_10y_products.py'
    sys.exit(-1)

root_folder = '/calvalus/projects/diversity'

northRegions = [
'Lake-Acigol',
'Lake-Aral',
'Lake-Argentino',
'Lake-Ayakkum',
'Lake-Aylmer',
'Lake-Balkhash',
'Lake-Barun-Torey',
'Lake-Bear',
'Lake-Beysehir',
'Lake-Bogoria',
'Lake-Buenos_aires',
'Lake-Burdur',
'Lake-Buyr',
'Lake-Chany',
'Lake-Chocon',
'Lake-Constance',
'Lake-Cuitzeo',
'Lake-Dianchi',
'Lake-Ebi',
'Lake-Enriquillo',
'Lake-Er',
'Lake-Erie',
'Lake-Fort_peck',
'Lake-Gavkhouni',
'Lake-Geneva',
'Lake-George',
'Lake-Gyaring',
'Lake-Har',
'Lake-Har_us',
'Lake-Kapchagayskoyevodo',
'Lake-Kara-Bogaz-Gol',
'Lake-Keban_baraji',
'Lake-Kulundinskoye',
'Lake-Kus',
'Lake-Kyaring',
'Lake-Kyoga',
'Lake-Larga',
'Lake-Logipi',
'Lake-Luang',
'Lake-Manitoba',
'Lake-Manych-Gudilo',
'Lake-Maracaibo',
'Lake-Mono',
'Lake-Naknek',
'Lake-Nam',
'Lake-Nasser',
'Lake-Natron',
'Lake-Ngoring',
'Lake-Patzcuaro',
'Lake-Poopo',
'Lake-Prespa',
'Lake-Qinghai',
'Lake-Razazza',
'Lake-Razelm',
'Lake-Rogaguado',
'Lake-Saint_clair',
'Lake-Salton',
'Lake-San_martin',
'Lake-Sarykamyshskoye',
'Lake-Songkhla',
'Lake-Tangra',
'Lake-Tengiz',
'Lake-Terinam',
'Lake-Tsimanampetsotsa',
'Lake-Turkana',
'Lake-Ulungur',
'Lake-Urmia',
'Lake-Uvs',
'Lake-Van',
'Lake-Veneta',
'Lake-Viedma',
'Lake-Winnipeg',
'Lake-Winnipegosis',
'Lake-Xavantes',
'Lake-Yamdrok',
'Lake-Ziling'
]

years = ['2008']

regions = northRegions

print 'starting...'
for year in years:
    for region in regions:
        seq_folder = root_folder + '/yearly-seq/' + year + os.sep + region
        #print 'seq_folder: ', seq_folder
        nc_folder = root_folder + '/yearly-nc/' + year + os.sep + region
        if os.path.exists('/mnt/hdfs' + seq_folder):
            rmcommand = "hadoop fs -rm -r " + seq_folder
            print rmcommand
            os.popen(rmcommand)
        if os.path.exists('/mnt/hdfs' + nc_folder):
            rmcommand = "hadoop fs -rm -r " + nc_folder
            print rmcommand
            os.popen(rmcommand)

print 'done.'
