from pmonitor import PMonitor
from datetime import date
from calendar import monthrange, isleap
import os

years = ['2003', '2004', '2005', '2006', '2007', '2008', '2009', '2010', '2011']
#years = ['2005']
allMonths = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']
#allMonths = ['09']

bands = ['chl_mph_mean']
#bands = ['owt_cc_dominant_class_mode']
#bands = ['chl_mph_mean', 'chl_fub_mean', 'cdom_fub_mean', 'tsm_cc_mean', 'turbidity_cc_mean', 'immersed_cyanobacteria_mean', 
#         'floating_cyanobacteria_mean', 'floating_vegetation_mean', 'owt_cc_dominant_class_mode']

#################################################################

#regions = ['Lakes-Mosaic-EUROPE']
#regions = ['Lakes-Mosaic-ASIA','Lakes-Mosaic-SOUTHAMERICA']
regions = ['Lakes-Mosaic-EUROPE','Lakes-Mosaic-AUSTRALIA','Lakes-Mosaic-AFRICA',
           'Lakes-Mosaic-ASIA_WEST','Lakes-Mosaic-ASIA_EAST',
           'Lakes-Mosaic-SOUTHAMERICA','Lakes-Mosaic-SOUTHAMERICA',
           'Lakes-Mosaic-AMERICAS','Lakes-Mosaic-EUROPE_AFRICA']
regions = ['Lakes-Mosaic-ASIA']

DIVERSITY_INST_DIR = os.environ['DIVERSITY_INST']
# before starting, check if WKT files are available. 
for region in regions:
    shapeWktFile = DIVERSITY_INST_DIR + '/wkt/' + region + '.shape'
    if not os.access(shapeWktFile, os.R_OK):
        raise IOError('Unable to access ' + shapeWktFile)

#   output format must be one of 'GeoTIFF' 'NetCDF' or 'NetCDF4'
# outputFormat = 'GeoTIFF'
# extension = 'nc'
# if outputFormat == 'GeoTIFF':
#    extension = 'tif'

inputFormat = 'GeoTIFF'
outputFormat = 'NetCDF4'
inputExtension = 'tif'

inputs = ['monthlies']
hosts = [('localhost', 16)]
#   , simulation=True
pm = PMonitor(inputs, request='lake_products_mosaics_monthlies', logdir='log', hosts=hosts, script='template.py')

SRCBASE = '/calvalus/projects/diversity/monthly/'
MOSAICBASE = '/calvalus/projects/diversity/mosaics/'

# =============== l3 mosaics =======================
for region in regions:
    shapeWktFile = 'wkt/' + region + '.shape'
    boxWktFile = shapeWktFile

    for year in years:
        for month in allMonths:
            (_, lastdayofmonth) = monthrange(int(year), int(month))
            startDate = str(date(int(year), int(month), 1))
            stopDate = str(date(int(year), int(month), lastdayofmonth))
            period = str(lastdayofmonth)

            for band in bands:
                l3MosaicDir = MOSAICBASE + '/monthly/' + region + '/' + year + '/' + month + '/' + band
                mosaic_name = 'monthly-' + region + '-' + year + '-' + month + '-' +  band
                params = [
                    'startDate', startDate,
                    'stopDate', stopDate,
                    'period', period,
                    'region', region,
                    'band', band,
                    'wkt', 'include:'+boxWktFile,
                    'input', SRCBASE + year + '/' + month + '/.*.' + inputExtension + '$',
                    'inputFormat', inputFormat,
                    'output', l3MosaicDir,
                    'outputFormat', outputFormat
                ]
                pm.execute('l3-mosaic-from-l3-monthly-' + band + '.xml', ['monthlies'], [mosaic_name], parameters=params, logprefix=mosaic_name)

pm.wait_for_completion()
