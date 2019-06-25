from pmonitor import PMonitor
from datetime import date
from calendar import monthrange, isleap
import os
from auxdata import ratio490Threshold, arcAuxdata

#years = ['2003', '2004', '2005', '2006', '2007', '2008', '2009', '2010', '2011']
years = ['2004']
#allMonths = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']

#def getMonth(year):
#    if year == '2002':
#        return ['06', '07', '08', '09', '10', '11', '12']
#    if year == '2012':
#        return ['01', '02', '03', '04']
#    return allMonths
#    #return ['12']  # test


northRegionsFile = open("./wkt/lakes-WB-north-regions.txt","r")
northRegions = []
for line in northRegionsFile:
    name = line.strip().replace('\'', '')
    northRegions.append(name)

southRegionsFile = open("./wkt/lakes-WB-south-regions.txt","r")
southRegions = []
for line in southRegionsFile:
    name = line.strip().replace('\'', '')
    southRegions.append(name)

regions = northRegions + southRegions

#regions = ['Lake-GOVIND_BALLABH_PANT']

DIVERSITY_INST_DIR = os.environ['DIVERSITY_INST']
# before starting, check if WKT files are available. 
# From now on we always use the full polygons (*.shape) rather than just rectangular boxes. 
for region in regions:
    shapeWktFile = DIVERSITY_INST_DIR + '/wkt/' + region + '.shape'
    if not os.access(shapeWktFile, os.R_OK):
        raise IOError('Unable to access ' + shapeWktFile)

inputFormat = 'GeoTIFF'
outputFormat = 'NetCDF4'

inputs = ['yearlies']
hosts = [('localhost', 16)]
#   , simulation=True
pm = PMonitor(inputs, request='tif2nc_yearly', logdir='log', hosts=hosts, script='template.py')

BASE = '/calvalus/projects/diversity/lakes-WB/'
OUTPUT = '/calvalus/projects/diversity/yearly/'

for year in years:
    for region in regions:
        shapeWktFile = 'wkt/' + region + '.shape'
        boxWktFile = shapeWktFile

        yearlyDir = BASE + region + '/l3-yearly/\${yyyy}-L3-1/'
        l2Params = [
            'year', year,
            'region', region,
            'input', BASE,
            'output', OUTPUT,
            'wkt', 'include:'+boxWktFile
        ]
        params = l2Params + [
            'input', yearlyDir,
        ]
        l2_tif2nc_name = 'l2_tif2nc-yearly' + year + '-' + region
        pm.execute('l2-tif2nc-yearly.xml', ['yearlies'], [l2_tif2nc_name], parameters=params, logprefix=l2_tif2nc_name)

pm.wait_for_completion()
