#!/usr/bin/python
# template.py templateName [param value] [param2 value2] input requestName

import sys
import os
from string import Template
import subprocess

BASE_DIR = os.environ['DIVERSITY_INST']


def generateRequest(templateFileName, requestFileName, keywords):
    with open(templateFileName, 'r') as templateFile:
        template_data = templateFile.read()
    content = Template(template_data).safe_substitute(keywords)
    with open(requestFileName, 'w') as requestFile:
        requestFile.write(content + '\n')


def main(templateName, requestName, parameters):
    parameterDict = {}
    for key, value in zip(parameters[0::2], parameters[1::2]):
        if value.startswith('include:'):
            includeFile = value[len('include:'):]
            if includeFile[0] != '/':
                includeFile = BASE_DIR + '/' + includeFile
            with open(includeFile, 'r') as textFile:
                valueFromFile = textFile.read()
            parameterDict[key] = valueFromFile
        else:
            parameterDict[key] = value
    templateFileName = BASE_DIR + '/etc/' + templateName
    fileExtension = os.path.splitext(templateFileName)[1]
    requestFileName = BASE_DIR + '/requests/' + requestName + fileExtension
    generateRequest(templateFileName, requestFileName, parameterDict)
    return subprocess.call(['submitproductionrequest.sh', requestFileName])


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print 'Usage: template.py template [param value] [param2 value2] input output'
        sys.exit(1)
    templateName = sys.argv[1]
    parameters = sys.argv[2:len(sys.argv)-1]
    requestName = sys.argv[-1]
    sys.exit(main(templateName, requestName, parameters))