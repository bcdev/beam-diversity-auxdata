from pmonitor import PMonitor
from datetime import date
from calendar import monthrange, isleap
import os
from auxdata import ratio490Threshold, arcAuxdata

years = ['2002', '2003', '2004', '2005', '2006', '2007', '2008', '2009', '2010', '2011', '2012']
allMonths = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']


def getMonth(year):
    if year == '2002':
        return ['06', '07', '08', '09', '10', '11', '12']
    if year == '2012':
        return ['01', '02', '03', '04']
    return allMonths


regions = ['Lake-Balaton']

DIVERSITY_INST_DIR = os.environ['DIVERSITY_INST']
# before starting, check if WKT files are available
for region in regions:
    boxWktFile = DIVERSITY_INST_DIR + '/wkt/' + region + '.bbox'
    if not os.access(boxWktFile, os.R_OK):
        raise IOError('Unable to access ' + boxWktFile)
    shapeWktFile = DIVERSITY_INST_DIR + '/wkt/' + region + '.shape'
    if not os.access(shapeWktFile, os.R_OK):
        raise IOError('Unable to access ' + shapeWktFile)

#   must be one of 'GeoTIFF' 'NetCDF' or 'NetCDF4'
outputFormat = 'GeoTIFF'

extension = 'nc'
if outputFormat == 'GeoTIFF':
    extension = 'tif'

inputs = ['childs']
hosts = [('localhost', 8)]
#   , simulation=True
pm = PMonitor(inputs, request='lake_products', logdir='log', hosts=hosts, script='template.py')

BASE_OLD = '/calvalus/projects/diversity/prototype/'
BASE_NEW = '/calvalus/home/marcoz/diversity_lakes_4/'

for region in regions:
    shapeWktFile = 'wkt/' + region + '.shape'
    boxWktFile = 'wkt/' + region + '.bbox'

    shallowDir = BASE_NEW + region + '/l3-shallow/'
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
        'projectRoot', BASE_NEW,
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

        geoChildDir = BASE_OLD + region + '/geochilds/\${yyyy}/\${MM}/'
        # =============== level2 =======================
        l2Params = [
            'year', year,
            'region', region,
            'projectRoot', BASE_NEW,
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
            l3MonthDir = BASE_NEW + region + '/l3-monthly/' + year + '/' + month

            shallowFile = region + '-shallow_' + str(shallowStart) + '_' + str(shallowStop) + '.nc'

            l3_month_name = 'l3-monthly-' + year + '-' + month + '-' + region
            params = [
                'startDate', startDate,
                'stopDate', stopDate,
                'period', period,
                'region', region,
                'year', year,
                'projectRoot', BASE_NEW,
                'wkt', 'include:'+boxWktFile,
                'outputDir', l3MonthDir,
                'outputFormat', outputFormat,
                'shallowFile', shallowFile,
                'arcDayProduct', arcDayFile,
                'arcNightProduct', arcNightFile,
                'arcBand', arcBand,
            ]
            pm.execute('l3-monthly.xml', all_l2_names, [l3_month_name], parameters=params, logprefix=l3_month_name)
            l3_month_names.append(l3_month_name)
        # =============== l3year =======================
        if year != '2002' and year != '2012':
            startDate = year + '-01-01'
            stopDate = year + '-12-31'
            period = '366' if isleap(int(year)) else '365'
            l3YearDir = BASE_NEW + region + '/l3-yearly/' + year

            l3_year_name = 'l3-yearly-' + year + '-' + region
            params = [
                'startDate', startDate,
                'stopDate', stopDate,
                'period', period,
                'region', region,
                'wkt', 'include:'+boxWktFile,
                'input', BASE_NEW + region + '/l3-monthly/' + year + '/.*-1/.*.' + extension + '$',
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
        l3DecadeDir = BASE_NEW + region + '/l3-decade/'

        l3_decade_name = 'l3-decade-' + region
        params = [
            'startDate', startDate,
            'stopDate', stopDate,
            'period', period,
            'region', region,
            'wkt', 'include:'+boxWktFile,
            'input', BASE_NEW + region + '/l3-yearly/.*-1/.*.' + extension + '$',
            'output', l3DecadeDir,
            'outputFormat', outputFormat
        ]
        pm.execute('l3-from-l3.xml', l3_year_names, [l3_decade_name], parameters=params, logprefix=l3_decade_name)

pm.wait_for_completion()
