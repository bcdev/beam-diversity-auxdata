from pmonitor import PMonitor
from datetime import date
import os

years = ['2003', '2004', '2005', '2006', '2007', '2008', '2009', '2010', '2011']
#years = ['2003']
allMonths = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']
#allMonths = ['06']

#################################################################

inputs = ['monthlies']
hosts = [('localhost', 16)]
#   , simulation=True
pm = PMonitor(inputs, 
              request='copy_all_lake_products_for_mosaics_monthly', 
              logdir='log', 
              hosts=hosts,
              script='template.py')

#LAKES_DIR = '/calvalus/projects/diversity/lakes-WB/existing_lakes'
#LAKES_DIR = '/calvalus/projects/diversity/lakes-WB'
LAKES_DIR = '/calvalus/projects/diversity/prototype'
INPUT_DUMMY = '/calvalus/projects/diversity/prototype/Lake-Abe/l3-decade-L3-1'
OUTPUT_DUMMY = '/calvalus/projects/diversity/delete_me/'

# =============== setup copy for year and month, all products =======================
for year in years:
    for month in allMonths:
        dummy_name = 'copy-monthlies-'  + year + '-' + month
        params = [
                'lakes_dir', LAKES_DIR,
                'year', year,
                'month', month,
                'input', INPUT_DUMMY + '/.*.tif$',
                'output', OUTPUT_DUMMY,
        ]
        pm.execute('copy-all-lake-products-for-mosaics-monthly.xml', ['monthlies'], [dummy_name], parameters=params, logprefix=dummy_name)

pm.wait_for_completion()
