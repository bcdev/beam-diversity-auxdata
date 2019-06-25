from pmonitor import PMonitor
from datetime import date
from calendar import monthrange, isleap
import os

years = ['2003', '2004', '2005', '2006', '2007', '2008', '2009', '2010', '2011']
#years = ['2005']
allMonths = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']
#allMonths = ['09']

#bands = ['chl_mph_mean']
#bands = ['owt_cc_dominant_class_mode']
bands = ['chl_mph_mean', 'chl_fub_mean', 'cdom_fub_mean', 'tsm_cc_mean', 'turbidity_cc_mean', 'immersed_cyanobacteria_mean', 
         'floating_cyanobacteria_mean', 'floating_vegetation_mean', 'owt_cc_dominant_class_mode']

#################################################################

def getMonth(year):
    if year == '2002':
        return ['06', '07', '08', '09', '10', '11', '12']
    if year == '2012':
        return ['01', '02', '03', '04']
    return allMonths
    #return ['12']  # test

##################################################################

#regions = ['Lakes-Mosaic-AUSTRALIA']
#regions = ['Lakes-Mosaic-ASIA','Lakes-Mosaic-AFRICA']
regions = ['Lakes-Mosaic-EUROPE','Lakes-Mosaic-AUSTRALIA',
           'Lakes-Mosaic-NORTHAMERICA','Lakes-Mosaic-SOUTHAMERICA',
           'Lakes-Mosaic-AMERICAS','Lakes-Mosaic-EUROPE_AFRICA']
#regions = ['Lakes-Mosaic-ASIA']
#regions = ['Lakes-Mosaic-NORTHAMERICA']

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
#inputFormat = 'NetCDF4'
outputFormat = 'NetCDF4'
inputExtension = 'tif'
#inputExtension = 'nc.gz'

inputs = ['yearlies']
hosts = [('localhost', 16)]
#   , simulation=True
pm = PMonitor(inputs, request='lake_products_mosaics_yearlies', logdir='log', hosts=hosts, script='template.py')

#SRCBASE = '/calvalus/projects/diversity/yearly-nc/'
SRCBASE = '/calvalus/projects/diversity/yearly/'
MOSAICBASE = '/calvalus/projects/diversity/mosaics/'

# =============== l3 mosaics =======================
for region in regions:
    shapeWktFile = 'wkt/' + region + '.shape'
    boxWktFile = shapeWktFile

    for year in years:
        for band in bands:
            l3MosaicDir = MOSAICBASE + '/yearly/' + region + '/' + year + '/' + band
            mosaic_name = 'yearly-' + region + '-' + year + '-' +  band
	    INPUT = SRCBASE + year + '/.*.' + inputExtension + '$'
	    #INPUT = SRCBASE + year + '/.*Lake-.*' + '$'   # to allow both 'Lake-*.tif' and 'L2_of_Lake-*.nc.gz'
            params = [
                    'region', region,
                    'year', year,
                    'band', band,
                    'wkt', 'include:'+boxWktFile,
                    'input', INPUT,
                    'inputFormat', inputFormat,
                    'output', l3MosaicDir,
                    'outputFormat', outputFormat
            ]
            pm.execute('l3-mosaic-from-l3-yearly-' + band + '.xml', ['yearlies'], [mosaic_name], parameters=params, logprefix=mosaic_name)

pm.wait_for_completion()
