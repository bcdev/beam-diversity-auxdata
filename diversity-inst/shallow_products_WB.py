from pmonitor import PMonitor
from datetime import date
from calendar import monthrange, isleap
import os
from auxdata import ratio490Threshold, arcAuxdata

# same script as lake_products_WB.py, but stops after shallow step

years = ['2002', '2003', '2004', '2005', '2006', '2007', '2008', '2009', '2010', '2011', '2012']
allMonths = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']

def getMonth(year):
    if year == '2002':
        return ['06', '07', '08', '09', '10', '11', '12']
    if year == '2012':
        return ['01', '02', '03', '04']
    return allMonths

# read the regions for the considered lakes:
regions = []
regionsFile1 = open("./wkt/lakes-WB-north-regions.txt","r")
for line in regionsFile1:
    name = line.strip().replace('\'', '')
    regions.append(name)
regionsFile2 = open("./wkt/lakes-WB-south-regions.txt","r")
for line in regionsFile2:
    name = line.strip().replace('\'', '')
    regions.append(name)

#regions = ['Lake-MareGrande','Lake-Veneta']  # request KS/DO, 20170711
### end of test lakes

DIVERSITY_INST_DIR = os.environ['DIVERSITY_INST']
# before starting, check if WKT files are available. 
# From now on we always use the full polygons (*.shape) rather than just rectangular boxes. 
for region in regions:
    shapeWktFile = DIVERSITY_INST_DIR + '/wkt/' + region + '.shape'
    if not os.access(shapeWktFile, os.R_OK):
        raise IOError('Unable to access ' + shapeWktFile)

#   output format must be one of 'GeoTIFF' 'NetCDF' or 'NetCDF4'
outputFormat = 'GeoTIFF'

extension = 'nc'
if outputFormat == 'GeoTIFF':
    extension = 'tif'

inputs = ['childs']
hosts = [('localhost', 16)]
#   , simulation=True
pm = PMonitor(inputs, request='shallow_products_WB', logdir='log', hosts=hosts, script='template.py')

BASE = '/calvalus/projects/diversity/lakes-WB/'

for region in regions:
    shapeWktFile = 'wkt/' + region + '.shape'
    boxWktFile = shapeWktFile

    shallowDir = BASE + region + '/l3-shallow/'
    # =============== shallow =======================
    # L3 aggregation of ratio490
    isNorthRegion = True
    if isNorthRegion:
        shallowStart = date(2008, 05, 01)
        shallowStop = date(2008, 10, 31)
        requiredIdepix = ['l2_idepix-2008-' + region]
    else:
        shallowStart = date(2008, 11, 01)
        shallowStop = date(2009, 04, 30)
        requiredIdepix = ['l2_idepix-2008-' + region, 'l2_idepix-2009-' + region]
    period = shallowStop - shallowStart
    period = period.days + 1

    shallowParams = [
        'startDate', str(shallowStart),
        'stopDate', str(shallowStop),
        'period', str(period),
        'region', region,
        'projectRoot', BASE,
        'wkt', 'include:' + shapeWktFile,
        'ratio490thresh', ratio490Threshold(region),
        'outputDir', shallowDir,
    ]
    shallow_name = 'shallow_name-' + region
    pm.execute('l3-shallow.xml', requiredIdepix, [shallow_name], parameters=shallowParams, logprefix=shallow_name)

    for year in ['2008','2009']:
        startDate = year + '-01-01'
        stopDate = year + '-12-31'

        geoChildDir = BASE + region + '/l1-child/\${yyyy}/\${MM}/'
        # =============== level2 =======================
        l2Params = [
            'year', year,
            'region', region,
            'projectRoot', BASE,
            'wkt', 'include:'+boxWktFile
        ]
        params = l2Params + [
            'input', geoChildDir,
        ]
        l2_idepix_name = 'l2_idepix-' + year + '-' + region
        pm.execute('l2-idepix.xml', ['childs'], [l2_idepix_name], parameters=params, logprefix=l2_idepix_name)


pm.wait_for_completion()
