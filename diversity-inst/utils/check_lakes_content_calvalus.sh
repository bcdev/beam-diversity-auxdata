#!/bin/bash

for lake in `ls /calvalus/projects/diversity/prototype`; do 
    echo $lake 10-year: `ls /calvalus/projects/diversity/prototype/$lake/l3-decade-L3-1/*.tif |wc | awk -F" " '{print $1}'`
    for year in {2003..2011}; do
        echo $lake yearly: `ls /calvalus/projects/diversity/prototype/$lake/l3-yearly/${year}-L3-1/*.tif |wc | awk -F" " '{print $1}'`
        for month in {01..12}; do 
	    echo $lake monthly: `ls /calvalus/projects/diversity/prototype/$lake/l3-monthly/${year}/${month}-L3-1/*.tif |wc | awk -F" " '{print $1}'` 
	done
    done
done
