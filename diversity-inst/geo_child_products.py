from pmonitor import PMonitor
from datetime import date
from calendar import monthrange
import os

years = ['2002', '2003', '2004', '2005', '2006', '2007', '2008', '2009', '2010', '2011', '2012']
allMonths = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']


def getMonth(year):
    if year == '2002':
        return ['06', '07', '08', '09', '10', '11', '12']
    if year == '2012':
        return ['01', '02', '03', '04']
    return allMonths


# years  = [ '2005', '2006', '2007' ]
years = ['2005']

regions = ['Lake-Balaton']

DIVERSITY_INST_DIR = os.environ['DIVERSITY_INST']
# before starting, check if WKT files are available
for region in regions:
    boxWktFile = DIVERSITY_INST_DIR + '/wkt/' + region + '.bbox'
    if not os.access(boxWktFile, os.R_OK):
        raise IOError('Unable to access ' + boxWktFile)


inputs = ['dummy']
hosts = [('localhost', 8)]

pm = PMonitor(inputs, request='geo_child_products', logdir='log', hosts=hosts, script='template.py')

BASE_OLD = '/calvalus/projects/diversity/prototype/'
BASE_NEW = '/calvalus/home/marcoz/diversity_lakes/'

for region in regions:
    boxWktFile = 'wkt/' + region + '.bbox'
    for year in years:
        for month in getMonth(year):
            (_, lastdayofmonth) = monthrange(int(year), int(month))
            startDate = str(date(int(year), int(month), 1))
            stopDate = str(date(int(year), int(month), lastdayofmonth))

            geoChildDir = BASE_NEW + region + '/l1-child/' + year + "/" + month
            l1_geo_name = 'l1_geo-' + year + "-" + month + '-' + region
            params = [
                'startDate', startDate,
                'stopDate', stopDate,
                'region', region,
                'wkt', 'include:'+boxWktFile,
                'output', geoChildDir,
            ]
            pm.execute('l1-child.xml', ['dummy'], [l1_geo_name], parameters=params, logprefix=l1_geo_name)

pm.wait_for_completion()
