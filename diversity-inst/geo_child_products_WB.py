from pmonitor import PMonitor
from datetime import date
from calendar import monthrange
import os

years = ['2002', '2003', '2004', '2005', '2006', '2007', '2008', '2009', '2010', '2011', '2012']
#years = ['2002', '2003', '2004', '2005', '2006', '2007', '2008']
allMonths = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']

def getMonth(year):
    if year == '2002':
        return ['06', '07', '08', '09', '10', '11', '12']
    if year == '2012':
        return ['01', '02', '03', '04']
    return allMonths


# reprocess lakes which had 'wrong' r490 tresh (DO, Nov 2017):
regions = []
regionsFile1 = open("./wkt/lakes-WB-regions-repro2017-geochilds.txt","r")
for line in regionsFile1:
    name = line.strip().replace('\'', '')
    regions.append(name)

#regions = ['Lake-Veneta']  # request KS/DO, 20170711
#regions = ['Lake-Tsimanampetsotsa', 'Lake-Terinam', 'Lake-Turkana']  # failed in first run
#regions = ['Lake-TUNAS_GRANDES']  # failed in first run
#regions = ['Lake-Razelm']  # forgotten in first run
regions = ['Lake-Kyoga','Lake-Kyaring']  # updated shapefiles, 20180329

DIVERSITY_INST_DIR = os.environ['DIVERSITY_INST']
# before starting, check if WKT files are available
# From now on we always use the full polygons (*.shape) rather than just rectangular boxes.
for region in regions:
    boxWktFile = DIVERSITY_INST_DIR + '/wkt/' + region + '.shape'
    if not os.access(boxWktFile, os.R_OK):
        raise IOError('Unable to access ' + boxWktFile)

inputs = ['dummy']
hosts = [('localhost', 16)]

pm = PMonitor(inputs, request='geo_child_products_WB', logdir='log', hosts=hosts, script='template.py', simulation=False)

#BASE = '/calvalus/projects/diversity/lakes-WB/'
BASE = '/calvalus/projects/diversity/lakes-WB/existing_lakes/'

for region in regions:
    boxWktFile = 'wkt/' + region + '.shape'
    for year in years:
        for month in getMonth(year):
            geoChildDir = BASE + region + '/l1-child/' + year + "/" + month
            l1_geo_name = 'l1_geo-' + year + "-" + month + '-' + region
            params = [
                'year', year,
                'month', month,
                'region', region,
                'wkt', 'include:'+boxWktFile,
                'input', '/calvalus/eodata/MER_FSG_1P/v2013/' + year + '/' + month,
                'output', geoChildDir,
            ]
            pm.execute('l1-child.xml', ['dummy'], [l1_geo_name], parameters=params, logprefix=l1_geo_name)

pm.wait_for_completion()
