from pmonitor import PMonitor
from datetime import date
from calendar import monthrange, isleap
from auxdata_arc import arcAuxdata
from auxdata_wkt import lakeBoxPolygon

years = ['2002', '2003', '2004', '2005', '2006', '2007', '2008', '2009', '2010', '2011', '2012']
allMonths = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']


def getMonth(year):
    if year == '2002':
        return ['06', '07', '08', '09', '10', '11', '12']
    if year == '2012':
        return ['01', '02', '03', '04']
    return allMonths


# ears  = [ '2005', '2006', '2007' ]
years  = [ '2005' ]

regions = ['Lake-Balaton']
#   must be one of 'GeoTIFF' 'NetCDF' or 'NetCDF4'
outputFormat = 'GeoTIFF'

extension = 'nc'
if outputFormat == 'GeoTIFF':
    extension = 'tif'

inputs = ['dummy']
hosts = [('localhost', 8)]

pm = PMonitor(inputs, request='lake_products', logdir='log', hosts=hosts, script='template.py')

BASE_OLD = '/calvalus/projects/diversity/prototype/'
BASE_NEW = '/calvalus/home/marcoz/diversity_lakes/'

for region in regions:
    wkt = lakeBoxPolygon(region)
    l3_year_names = []
    for year in years:
        startDate = year + '-01-01'
        stopDate = year + '-12-31'

        # =============== level2 =======================
        geoChildDir = BASE_OLD + region + '/geochilds/\${yyyy}/\${MM}/'
        l2Params = [
            'year', year,
            'region', region,
            'wkt', '"' + wkt + '"'
        ]

        l2IdepixDir = BASE_NEW + region + '/l2-idepix/' + year
        l2_idepix_name = 'l2_idepix-' + year + '-' + region
        params = l2Params + [
            'input', geoChildDir,
            'output', l2IdepixDir,
        ]
        pm.execute('l2-idepix.xml', ['dummy'], [l2_idepix_name], parameters=params, logprefix=l2_idepix_name)

        l2MphDir = BASE_NEW + region + '/l2-mph/' + year
        l2_mph_name = 'l2_mph-' + year + '-' + region
        params = l2Params + [
            'input', l2IdepixDir,
            'output', l2MphDir,
        ]
        pm.execute('l2-mph.xml', [l2_idepix_name], [l2_mph_name], parameters=params, logprefix=l2_mph_name)

        l2FubDir = BASE_NEW + region + '/l2-fub/' + year
        l2_fub_name = 'l2_fub-' + year + '-' + region
        params = l2Params + [
            'input', geoChildDir,
            'output', l2FubDir,
        ]
        pm.execute('l2-fub.xml', ['dummy'], [l2_fub_name], parameters=params, logprefix=l2_fub_name)

        l2Ccl2wDir = BASE_NEW + region + '/l2-ccl2w/' + year
        l2_ccl2w_name = 'l2_ccl2w-' + year + '-' + region
        params = l2Params + [
            'input', geoChildDir,
            'output', l2Ccl2wDir,
        ]
        pm.execute('l2-ccl2w.xml', ['dummy'], [l2_ccl2w_name], parameters=params, logprefix=l2_ccl2w_name)

        all_l2_names = [l2_idepix_name, l2_mph_name, l2_fub_name, l2_ccl2w_name]
        l3_month_names = []
        for month in getMonth(year):
            # =============== l3month =======================
            (_, lastdayofmonth) = monthrange(int(year), int(month))
            startDate = str(date(int(year), int(month), 1))
            stopDate = str(date(int(year), int(month), lastdayofmonth))
            period = str(lastdayofmonth)
            (arcDayFile, arcNightFile, arcBand) = arcAuxdata(region, year, month)
            l3MonthDir = BASE_NEW + region + '/l3-monthly/' + year + '/' + month

            l3_month_name = 'l3-monthly-' + year + '-' + month + '-' + region
            params = [
                'startDate', startDate,
                'stopDate', stopDate,
                'period', period,
                'region', region,
                'wkt', '"' + wkt + '"',
                'inputDir', l2IdepixDir,
                'outputDir', l3MonthDir,
                'outputFormat', outputFormat,
                'mphDir', l2MphDir,
                'fubDir', l2FubDir,
                'ccl2wDir', l2Ccl2wDir,
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
                'wkt', '"' + wkt + '"',
                'input', BASE_NEW + region + '/l3-monthly/' + year + '/.*-1/.*.' + extension + '$',
                'output', l3YearDir,
                'outputFormat', outputFormat
            ]
            pm.execute('l3-from-l3.xml', l3_month_names, [l3_year_name], parameters=params, logprefix=l3_year_name)
            l3_year_names.append(l3_year_name)
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
        'wkt', '"' + wkt + '"',
        'input', BASE_NEW + region + '/l3-yearly/.*-1/.*.' + extension + '$',
        'output', l3DecadeDir,
        'outputFormat', outputFormat
    ]
    pm.execute('l3-from-l3.xml', l3_year_names, [l3_decade_name], parameters=params, logprefix=l3_decade_name)

pm.wait_for_completion()
