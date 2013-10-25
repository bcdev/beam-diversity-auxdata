;***************************************************************
; cmorph_1.ncl
;
; Concepts illustrated:
;   - Reading big endian binary files
;   - Reading records written by a fortran direct access write
;   - Reading CMORPH 0.25 data
;   - Counting missing values
;   - Calculating areal averages
;   - Adding meta data (attributes and coordinates [time, lat, lon])
;   - Explicitly setting contour levels
;   - Creating a netCDF file
;
;   - Results will be in directory $PWD/yyyy/yyyyMM/nc
;   - In addition, plots for each netcdf product will be in directory $PWD/yyyy/yyyyMM/ps
;
; Example call: ncl year=2001 'month="01"' 'dName="./2001/200101/"' bin2nc_cmorph_all.ncl
;
;***************************************************************
;; 
;*********** Load Libraries ************************************
load "$NCARG_ROOT/lib/ncarg/nclscripts/csm/gsn_code.ncl" 
load "$NCARG_ROOT/lib/ncarg/nclscripts/csm/gsn_csm.ncl" 
load "$NCARG_ROOT/lib/ncarg/nclscripts/csm/contributed.ncl" 
;**************************************************************
begin
;***************************************************************
; User Input
;***************************************************************

   ;if (.not. isvar("dName")) then
   ;   print("Input dir not set - exit!")
   ;   ; exit!!
   ;end if
   if (.not. isvar("year")) then
      print("Year not set - exit!")
      exit
   end if
   if (.not. isvar("month")) then
      print("Month not set - exit!")
      exit
   end if

   dName = "./" + year + "/" + year + month + "/"
   fili = systemfunc("cd "+dName+" ; ls CMORPH*30min_$year$month*")

   netCDF = True                             ; generate netCDF file
   PLOT   = True                             ; generate plots

   if (netCDF) then
       ncDir   = dName + "/nc/"          ; directory for netCDF output
   end if

   if (PLOT) then
       pltDir  = dName + "/ps/"        ; directory for plot output
       pltType = "ps"                                            
   end if

;***************************************************************
; End User Input
;***************************************************************

   ;***************************************************************
   ; Create/Add coordinate variables. See ctl file 'CMORPH_V1.0_RAW_8km-30min.ctl'   
   ;***************************************************************

      setfileoption("bin","ReadByteOrder","LittleEndian")

      nlat  = 1649                                    ; from ctl file  
      mlon  = 4948

      lat       =  59.963614 - ispan(0,nlat-1,1)*0.072771377  
      lon       =  -180.0 +  0.036378335 + ispan(0,mlon-1,1)*0.072756669

      lat!0     = "lat"
      lat&lat   =  lat
      lat@units = "degrees north"

      lon!0     = "lon"
      lon&lon   =  lon
      lon@units = "degrees east"

      dimNames = (/"lat", "lon" /)  
      dimSizes = (/ nlat,  mlon /) 
      dimUnlim = (/ False, False /)   


;***************************************************************
;LOOP over all files of year and month:
;***************************************************************

   nfil = dimsizes(fili)
   do nf=0,nfil-1
   ;do nf=0,3

   ;***************************************************************
   ; Miscellaneous: Parse the file name to extract strings
   ;***************************************************************
      filc        = stringtochar( fili(nf) ) 
      ncFil   = fili(nf) + ".nc"                ; netCDF name output
      date_str    = (/ filc(26:33) /)        ; yyyymmdd as a string
      file_str    = (/ filc(0:35)/)        ; unique
      pltName = "PLOT_" + nf                    ; plot name output

      if (PLOT) then
          pltName = pltName +"_"+file_str
      end if

      print(" ")
      print("Processing file " + nf + " '" + file_str + "' ...")

   ;***************************************************************
   ; Read (big endian) binary file
   ;***************************************************************
      cpc   = fbindirread(dName+fili(nf),1, (/nlat,mlon/),"float")

   ;***************************************************************
   ; Add meta data
   ;***************************************************************
      cpc@_FillValue  = -999.0
      cpc@units       = "mm/hr"
      cpc@long_name   = "CMORPH precip"

   ;***************************************************************
   ; Associate the spatial coordinates with variables
   ;***************************************************************

      cpc!0    = "lat"                   ; 1st ... name the dimensions
      cpc!1    = "lon"
      cpc&lat  =  lat                    ; create coordinate variable
      cpc&lon  =  lon                    ; create coordinate variable

   ;***************************************************************
   ; Simple data exploration: 
   ;    Are there missing data?
   ;    Count the number of missing values in each variable
   ;    Calculate weighted areal averages: ignore missing grid points
   ;    Calculate weighted areal averages of precip occurrence only
   ; Print results
   ;***************************************************************

   ;************************************************
   ; Create plot ?
   ;************************************************
      if (PLOT) then
          wks    = gsn_open_wks(pltType, pltDir+pltName)
          colors = (/"white","black","Snow"     \ ; "WhiteSmoke"  \          
               ,"PaleTurquoise","PaleGreen","SeaGreen3" ,"Yellow"  \
               ,"Orange","Red","Violet","HotPink", "Purple", "Brown"/) 

          gsn_define_colormap(wks, colors)               ; generate new color map
   
;          plot = new ( 2 , "graphic")
          plot = new ( 1 , "graphic")
     
          res                      = True     ; plot mods desired
          res@gsnDraw              = False    ; don't draw
          res@gsnFrame             = False    ; don't advance frame
     
          res@cnFillOn             = True     ; turn on color fill
          res@cnLinesOn            = False    ; turn of contour lines
         ;res@cnFillMode           = "CellFill"           ; Cell Mode
          res@cnFillMode           = "RasterFill"         ; Raster Mode
          res@cnLinesOn            =  False       ; Turn off contour lines
          res@cnLineLabelsOn       =  False       ; Turn off contour lines
          res@cnLevelSelectionMode = "ExplicitLevels"              
          res@cnLevels             = (/0.005,0.01,0.05,0.1,0.5,1.0,2.0,5.0,10.0,20.0/) ; "mm/day" 
          res@cnMissingValFillPattern = 0         ; make 'missing' black
          res@cnMissingValFillColor   = "black"
   
          res@lbLabelBarOn         = False    ; turn off individual cb's
     
          res@mpMinLatF            = -60.     ; CMORPH limits [approx]
          res@mpMaxLatF            =  60.
          res@mpCenterLonF         = 210.
;          res@mpCenterLonF         = 0.
          res@mpFillOn             = False
   
      ;res@mpShapeMode          = "FreeAspect"
      ;res@vpWidthF             = 0.8
      ;res@vpHeightF            = 0.4
      
       ;res@gsnCenterString = "Areal Mean="+sprintf("%4.2f",  cpcAvg_025)
          plot(0) = gsn_csm_contour_map_ce(wks,cpc, res)
   
      ;delete(work)
     
          resP = True
          resP@gsnMaximize         = True                ; make ps/eps/pdf large [no effect x11]
          resP@gsnPaperOrientation = "Seascape"          ; force portrait
          resP@gsnPanelLabelBar    = True                ; add common colorbar
          resP@lbLabelAutoStride   = True
          resP@lbLabelFontHeightF  = 0.0175              ; change font size
          resP@txString            = "CMORPH: "+fili(nf)
;       gsn_panel(wks,plot,(/2,1/),resP)               ; now draw as one plot
          gsn_panel(wks,plot,(/1,1/),resP)               ; now draw as one plot
      end if    ; PLOT
  
   ;************************************************
   ; Create netCDF ?
   ; Recommend to always create a 'time' dimension 
   ;************************************************
  
      if (netCDF) then
       
          nline  = inttochar(10)

          globeAtt              = 1
          globeAtt@title        = "CMORPH: 8km 30min"    
          globeAtt@ftp          = "ftp://ftp.cpc.ncep.noaa.gov/precip/CMORPH_V1.0/RAW/8km-30min"
          globeAtt@acronym      = "CMORPH: CPC Morphing Technique"
          globeAtt@description  = "http://www.cpc.noaa.gov/products/janowiak/cmorph_description.html"
          globeAtt@referenceon  = nline + \
                                 "Joyce, R. J., J. E. Janowiak, P. A. Arkin, and P. Xie, 2004:       "+nline+\
                                 "CMORPH: A method that produces global precipitation estimates      "+nline+\
                                 "        from passive microwave and infrared data at high spatial   "+nline+\
                                 "        and temporal resolution. J. Hydromet., 5, 487-503.         "+nline
     
          globeAtt@creation_date= systemfunc ("date" )
     
          NCFILE = ncDir + ncFil
          system ("/bin/rm -f " +  NCFILE)    ; remove any pre-exist file 
           
          ncdf   = addfile(NCFILE,"c")     
     
      ;setfileoption(ncdf, "definemode", True)
     
          fileattdef( ncdf, globeAtt )        ; create the global [file] attributes
                                         
          filedimdef(ncdf, dimNames  , dimSizes,  dimUnlim )
          
          filevardef   (ncdf, "lat", typeof(lat), getvardims(lat))
          filevarattdef(ncdf, "lat", lat)
          
          filevardef   (ncdf, "lon", typeof(lon), getvardims(lon))
          filevarattdef(ncdf, "lon", lon)
          
          filevardef    (ncdf, "PRECIP_HOURLY", typeof(cpc ) , (/ "lat", "lon" /) )

          filevarattdef (ncdf, "PRECIP_HOURLY", cpc )
     
          ncdf->lat    = (/ lat /)
          ncdf->lon    = (/ lon /)

          ncdf->PRECIP_HOURLY = (/ cpc /) 

      end if    ; netCDF

      print("Done processing file '" + file_str + "' .")
   end do
end
