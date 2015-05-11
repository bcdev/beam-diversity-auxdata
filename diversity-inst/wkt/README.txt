- to obtain the shape files, open the 'real' shapefiles *.shp (several at once!) from fs1 in OpenJump and save as 'Datensatz unter...' with name 'Lake-XXX.shape'
- after copying them here, change from Polygon to Multipolygon with:
## sed -i -- 's/POLYGON ((/MULTIPOLYGON (((/g' *.shape
## sed -i -- 's/))/)))/g' *.shape
- make sure you do NOT do this for lakes you already did (e.g. do it in a working subdir) 
