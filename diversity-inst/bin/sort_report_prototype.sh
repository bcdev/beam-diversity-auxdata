#!/bin/bash

echo "sort prototype.report > prototype.report.sorted"
sort prototype.report > prototype.report.sorted
echo "mv prototype.report.sorted  prototype.report"
mv prototype.report.sorted  prototype.report
