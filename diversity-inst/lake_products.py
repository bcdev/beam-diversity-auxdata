from pmonitor import PMonitor
from datetime import date
from calendar import monthrange, isleap
import os
from auxdata import ratio490Threshold, arcAuxdata

years = ['2002', '2003', '2004', '2005', '2006', '2007', '2008', '2009', '2010', '2011', '2012']
#years = ['2004','2010']
#years = ['2008']
allMonths = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']

def getMonth(year):
    if year == '2002':
        return ['06', '07', '08', '09', '10', '11', '12']
    if year == '2012':
        return ['01', '02', '03', '04']
    return allMonths

# read the regions from Northern and Southern hemisphere:
northRegionsFile = open("./wkt/lakes-north-regions.txt","r")
#northRegionsFile = open("./wkt/lakes-north-regions_DtoM.txt","r")
#northRegionsFile = open("./wkt/lakes-north-regions_AtoM.txt","r")
#northRegionsFile = open("./wkt/lakes-north-regions_NtoZ.txt","r")
northRegions = []
for line in northRegionsFile:
    name = line.strip().replace('\'', '')
    northRegions.append(name)

southRegionsFile = open("./wkt/lakes-south-regions.txt","r")
#southRegionsFile = open("./wkt/lakes-south-regions_DtoM.txt","r")
#southRegionsFile = open("./wkt/lakes-south-regions_AtoM.txt","r")
#southRegionsFile = open("./wkt/lakes-south-regions_NtoZ.txt","r")
southRegions = []
for line in southRegionsFile:
    name = line.strip().replace('\'', '')
    southRegions.append(name)

regions = northRegions + southRegions

### definition of single lakes for testing - overwrites regions from files above ###
#regions = ['Lake-Bogoria', 'Lake-Balaton']
#regions = ['Lake-Balaton']
#regions = ['Lake-Bear']
#regions = ['Lake-Aral']
#regions = ['Lake-Elmenteita']
#regions = ['Lake-Bogoria', 'Lake-Elmenteita', 'Lake-Nakuru', 'Lake-Tuusulanjarvi', 'Lake-Ulemiste']
### end of test lakes

# read the regions for the spare lakes:
regionsFile = open("./wkt/lakes-regions-spare.txt","r")
regions = []
for line in regionsFile:
    name = line.strip().replace('\'', '')
    regions.append(name)

regions = ['Lake-Ijsselmeer']

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
pm = PMonitor(inputs, request='lake_products', logdir='log', hosts=hosts, script='template.py')

BASE = '/calvalus/projects/diversity/prototype/'

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


    l3_year_names = []
    for year in years:
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

        l2_mph_name = 'l2_mph-' + year + '-' + region
        pm.execute('l2-mph.xml', [l2_idepix_name], [l2_mph_name], parameters=l2Params, logprefix=l2_mph_name)

        l2_fub_name = 'l2_fub-' + year + '-' + region
        pm.execute('l2-fub.xml', ['childs'], [l2_fub_name], parameters=params, logprefix=l2_fub_name)

        l2_ccl2w_name = 'l2_ccl2w-' + year + '-' + region
        pm.execute('l2-ccl2w.xml', ['childs'], [l2_ccl2w_name], parameters=params, logprefix=l2_ccl2w_name)


        all_l2_names = [shallow_name, l2_idepix_name, l2_mph_name, l2_fub_name, l2_ccl2w_name]
        l3_month_names = []
        for month in getMonth(year):
            # =============== l3month =======================
            (_, lastdayofmonth) = monthrange(int(year), int(month))
            startDate = str(date(int(year), int(month), 1))
            stopDate = str(date(int(year), int(month), lastdayofmonth))
            period = str(lastdayofmonth)

            (arcDayFile, arcNightFile, arcBand) = arcAuxdata(region, year, month)
            if arcDayFile != '""':
                arcDayFile = '/calvalus/projects/diversity/aux/' + arcDayFile
                arcNightFile = '/calvalus/projects/diversity/aux/' + arcNightFile

            l3MonthDir = BASE + region + '/l3-monthly/' + year + '/' + month

            shallowFile = region + '-shallow_' + str(shallowStart) + '_' + str(shallowStop) + '.nc'

            l3_month_name = 'l3-monthly-' + year + '-' + month + '-' + region
            params = [
                'startDate', startDate,
                'stopDate', stopDate,
                'period', period,
                'region', region,
                'year', year,
                'projectRoot', BASE,
                'wkt', 'include:'+boxWktFile,
                'outputDir', l3MonthDir,
                'outputFormat', outputFormat,
                'shallowFile', shallowFile,
                'arcDayProduct', arcDayFile,
                'arcNightProduct', arcNightFile,
                'arcBand', arcBand
            ]
            pm.execute('l3-monthly.xml', all_l2_names, [l3_month_name], parameters=params, logprefix=l3_month_name)
            l3_month_names.append(l3_month_name)
        # =============== l3year =======================
        if year != '2002' and year != '2012':
            startDate = year + '-01-01'
            stopDate = year + '-12-31'
            period = '366' if isleap(int(year)) else '365'
            l3YearDir = BASE + region + '/l3-yearly/' + year

            l3_year_name = 'l3-yearly-' + year + '-' + region
            params = [
                'startDate', startDate,
                'stopDate', stopDate,
                'period', period,
                'region', region,
                'wkt', 'include:'+boxWktFile,
                'input', BASE + region + '/l3-monthly/' + year + '/.*-1/.*.' + extension + '$',
                'output', l3YearDir,
                'outputFormat', outputFormat
            ]
            pm.execute('l3-from-l3.xml', l3_month_names, [l3_year_name], parameters=params, logprefix=l3_year_name)
            l3_year_names.append(l3_year_name)

    if l3_year_names:
        # =============== l3decade =======================
        startDate = '2003-01-01'
        stopDate = '2011-12-31'
        period = '3287'
        l3DecadeDir = BASE + region + '/l3-decade/'

        l3_decade_name = 'l3-decade-' + region
        params = [
            'startDate', startDate,
            'stopDate', stopDate,
            'period', period,
            'region', region,
            'wkt', 'include:'+boxWktFile,
            'input', BASE + region + '/l3-yearly/.*-1/.*.' + extension + '$',
            'output', l3DecadeDir,
            'outputFormat', outputFormat
        ]
        pm.execute('l3-from-l3.xml', l3_year_names, [l3_decade_name], parameters=params, logprefix=l3_decade_name)

pm.wait_for_completion()
