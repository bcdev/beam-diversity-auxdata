import glob
import os
import datetime
import calendar
from datetime import date
from calendar import monthrange, isleap
from pmonitor import PMonitor

####################################################################

#def getMonth(year):
#    if year == '2002':
#        return ['06', '07', '08', '09', '10', '11', '12']
#    if year == '2012':
#        return ['01', '02', '03', '04']
#    return allMonths
#    #return ['12']  # test

def getMinMaxDate(year, month):
    monthrange = calendar.monthrange(int(year), int(month))
    minDate = datetime.date(int(year), int(month), 1)
    maxDate = datetime.date(int(year), int(month), monthrange[1])
    return (minDate, maxDate)

####################################################################

#### main script: ####

#years   = [ '2002', '2003', '2004', '2005', '2006', '2007', '2008', '2009', '2010', '2011','2012' ]
#years   = [ '2003', '2004', '2005', '2006', '2007', '2008', '2009', '2010', '2011' ]
years   = [ '2003', '2004', '2005', '2006', '2007', '2009', '2010', '2011' ]
#years   = [ '2008' ]

#northRegionsFile = open("./wkt/lakes-WB-north-regions.txt","r")
#northRegionsFile = open("./wkt/lakes-north-regions.txt","r")
northRegionsFile = open("./wkt/lakes-WB-regions-repro2017.txt","r")
northRegions = []
for line in northRegionsFile:
    name = line.strip().replace('\'', '')
    northRegions.append(name)

#southRegionsFile = open("./wkt/lakes-WB-south-regions.txt","r")
#southRegionsFile = open("./wkt/lakes-south-regions.txt","r")
southRegions = []
#for line in southRegionsFile:
#    name = line.strip().replace('\'', '')
#    southRegions.append(name)


regions = northRegions + southRegions
#regions = ['Lake-Aberdeen']
regions = ['Lake-Caspian']

#inputs = ['/calvalus/projects/diversity/lakes-WB']
#inputs = ['/calvalus/projects/diversity/lakes-WB/existing_lakes']
inputs = ['/calvalus/projects/diversity/prototype']
#inputs = ['/calvalus/projects/diversity/lakes-test']
hosts  = [('localhost',64)]
types  = [('l2-tif2nc-yearly-step.sh',56), ('l2-format-tif2nc-yearly-step.sh',8)]

pm = PMonitor(inputs, \
              request='tif2nc_yearly', \
              logdir='log', \
              hosts=hosts, \
              types=types)

for year in years:
    for region in regions:
        (minDate, maxDate) = (year + '-01-01', year + '-12-31')
        pm.execute('l2-tif2nc-yearly-step.sh', \
                   [ '/calvalus/projects/diversity/prototype' ], \
                   [ '/calvalus/projects/diversity/yearly-seq/' + year + '/' + region ], \
                   parameters=[year, region, str(minDate), str(maxDate)])  
        pm.execute('l2-format-tif2nc-yearly-step.sh', \
                   [ '/calvalus/projects/diversity/yearly-seq/' + year + '/' + region ], \
                   [ '/calvalus/projects/diversity/yearly-nc/'  + year + '/' + region ], \
                   parameters=[year, region, str(minDate), str(maxDate)])

pm.wait_for_completion()
