from datetime import date

# enter here k490 thresholds other than default for certain lakes:
ratio490Threshholds = {
'Lake-Acigol': '0.0',
'Lake-Alakol': '0.5',
'Lake-Alexandrina': '0.5',
'Lake-Aral': '0.0',
'Lake-Argentino': '0.0',
'Lake-Argyle': '0.5',
'Lake-Atlin': '0.5',
'Lake-Ayakkum': '0.0',
'Lake-Balaton': '0.5',
'Lake-Balkhash': '0.5',
'Lake-Barun-Torey': '0.5',
'Lake-Bear': '0.0',
'Lake-Beysehir': '0.0',
'Lake-Bisina': '0.6',
'Lake-Bogoria': '0.6',
'Lake-Bosten': '0.0',
'Lake-Brienz': '0.0',
'Lake-Brisas': '0.5',
'Lake-Buenos_aires': '0.0',
'Lake-Burdur': '0.5',
'Lake-Buyr': '0.6',
'Lake-Cabora_bassa': '0.6',
'Lake-Canitzan': '0.6',
'Lake-Caspian': '0.5',
'Lake-Chany': '0.0',
'Lake-Chiemsee': '0.5',
'Lake-Chiquita': '0.6',
'Lake-Chocon': '0.5',
'Lake-Comacchio': '0.0',
'Lake-Constance': '0.5',
'Lake-Cuitzeo': '0.0',
'Lake-Dianchi': '0.5',
'Lake-Ebi': '0.5',
'Lake-Egirdir': '0.5',
'Lake-Emborcacao': '0.5',
'Lake-Enriquillo': '0.5',
'Lake-Er': '0.5',
'Lake-Erie': '0.6',
'Lake-Fort_peck': '0.5',
'Lake-Gavkhouni': '0.0',
'Lake-Geneva': '0.6',
'Lake-George': '0.0',
'Lake-Ginebra': '0.5',
'Lake-Great_salt': '0.0',
'Lake-Gyaring': '0.5',
'Lake-Har': '0.0',
'Lake-Har_us': '0.5',
'Lake-Hyargas': '0.6',
'Lake-Ijsselmeer': '0.60',
'Lake-Iseo': '0.6',
'Lake-Kapchagayskoyevodo': '0.5',
'Lake-Kara-Bogaz-Gol': '0.5',
'Lake-Keban_baraji': '0.5',
'Lake-Kulundinskoye': '0.0',
'Lake-Kus': '0.5',
'Lake-Kyaring': '0.5',
'Lake-Kyoga': '0.5',
'Lake-Larga': '0.5',
'Lake-Logipi': '0.0',
'Lake-Luang': '0.0',
'Lake-Lucerne': '0.5',
'Lake-Manitoba': '0.5',
'Lake-Manych-Gudilo': '0.5',
'Lake-Maracaibo': '0.6',
'Lake-Markermeer': '0.5',
'Lake-Mono': '0.0',
'Lake-Muggelsee': '0.0',
'Lake-Naivasha': '0.5',
'Lake-Naknek': '0.5',
'Lake-Nam': '0.5',
'Lake-Nasser': '0.5',
'Lake-Natron': '0.0',
'Lake-Neuchatel': '0.5',
'Lake-Ngoring': '0.5',
'Lake-Nicaragua': '0.7',
'Lake-Patzcuaro': '0.5',
'Lake-Poopo': '0.5',
'Lake-Prespa': '0.5',
'Lake-Qinghai': '0.5',
'Lake-Razazza': '0.0',
'Lake-Razelm': '0.5',
'Lake-Rogaguado': '0.0',
'Lake-Saint_clair': '0.5',
'Lake-Salton': '0.5',
'Lake-San_martin': '0.0',
'Lake-Sarykamyshskoye': '0.6',
'Lake-Scutari': '0.6',
'Lake-Sempach': '0.5',
'Lake-Songkhla': '0.0',
'Lake-Tangra': '0.5',
'Lake-Tengiz': '0.0',
'Lake-Terinam': '0.0',
'Lake-Thun': '0.5',
'Lake-Trasimeno': '0.0',
'Lake-Tsimanampetsotsa': '0.0',
'Lake-Tsimlyanskoye': '0.6',
'Lake-Turkana': '0.6',
'Lake-Ulungur': '0.0',
'Lake-Urmia': '0.5',
'Lake-Uvs': '0.0',
'Lake-Van': '0.5',
'Lake-Veneta': '0.0',
'Lake-Viedma': '0.5',
'Lake-Winnipeg': '0.6',
'Lake-Winnipegosis': '0.0',
'Lake-Xavantes': '0.5',
'Lake-Yamdrok': '0.0',
'Lake-Ziling': '0.0',
'Lake-Zug': '0.6',
'Lake-Zurich': '0.6',
'Lake-BALIMELA': '0.5',
'Lake-BAN': '0.0',
'Lake-RANN_OF_KUTCH': '0.0',
'Lake-AZUCAR': '0.6',
'Lake-CUCHILLO': '0.6',
'Lake-DON_MARTIN': '0.5',
'Lake-MIGUEL_ALEMAN': '0.5',
'Lake-SANTIAGUILLO': '0.0',
'Lake-TERMINOS': '0.6',
'Lake-ALICURA': '0.6',
'Lake-BARREALES': '0.5',
'Lake-CARDIEL': '0.0',
'Lake-CASA_DE_PIEDRA': '0.5',
'Lake-CHEEPELMUTH': '0.0',
'Lake-COCHRANE': '0.0',
'Lake-FUTALEUFU': '0.0',
'Lake-GHIO': '0.0',
'Lake-HUECHULAFQUEN': '0.0',
'Lake-LACAR': '0.0',
'Lake-MINA': '0.0',
'Lake-MUSTERS': '0.0',
'Lake-PALENA': '0.0',
'Lake-TRAFUL': '0.0',
'Lake-TUNAS_GRANDES': '0.5',
'Lake-YACYRETA': '0.5',
'Lake-YEHUIN': '0.0'
}

#arcFileIdByRegion = {'Lake-Balaton' : '0310',
#                     'Lake-Aral' : '0004'}


# todo: check this for one lake which has ARCs
arcFileIdByRegion = {}
for line in open("./ARC/arc-lakes-alids.txt","r"):
    name, alid = line.split(':')
    name = name.strip().replace('\'', '')
    alid = alid.strip().replace('\'', '')
    arcFileIdByRegion[name] = alid
    #print 'arcFiles: ',arcFileIdByRegion[name]
#print 'arcFiles: ',arcFileIdByRegion

# the ARC product netcdf files:
lakes_alid_ncfiles = []
for line in open("./ARC/alid-nc-list.txt","r"):
    # alid-nc-list.txt is a directory listing retrieved on cvfeeder01 from:
    # for i in `ls /calvalus/projects/diversity/aux/ALID*PLREC*.nc`; do basename $i; done > alid_nc_list.txt
    ncfile = line.strip()
    lakes_alid_ncfiles.append(ncfile)

# ARC band times for v3: start 1995-06-16, end 2012-06-16
arcBandTime = [
                            9327.5, 9358.5, 9389.5, 9419.5, 9450.5, 9480.5,
    9511.5, 9541.5, 9571.5, 9602.5, 9632.5, 9663.5, 9693.5, 9724.5, 9755.5,
    9785.5, 9815.5, 9846.5, 9877.5, 9906.5, 9936.5, 9966.5, 9997.5, 10027.5,
    10058.5, 10089.5, 10119.5, 10150.5, 10180.5, 10211.5, 10242.5, 10271.5,
    10301.5, 10331.5, 10362.5, 10392.5, 10423.5, 10454.5, 10484.5, 10515.5,
    10545.5, 10576.5, 10607.5, 10636.5, 10666.5, 10696.5, 10727.5, 10757.5,
    10788.5, 10819.5, 10849.5, 10880.5, 10910.5, 10941.5, 10972.5, 11002.5,
    11032.5, 11062.5, 11093.5, 11123.5, 11154.5, 11185.5, 11215.5, 11246.5,
    11276.5, 11307.5, 11338.5, 11367.5, 11397.5, 11427.5, 11458.5, 11488.5,
    11519.5, 11550.5, 11580.5, 11611.5, 11641.5, 11672.5, 11703.5, 11732.5,
    11762.5, 11792.5, 11823.5, 11853.5, 11884.5, 11915.5, 11945.5, 11976.5,
    12006.5, 12037.5, 12068.5, 12097.5, 12127.5, 12157.5, 12188.5, 12218.5,
    12249.5, 12280.5, 12310.5, 12341.5, 12371.5, 12402.5, 12433.5, 12463.5,
    12493.5, 12523.5, 12554.5, 12584.5, 12615.5, 12646.5, 12676.5, 12707.5,
    12737.5, 12768.5, 12799.5, 12828.5, 12858.5, 12888.5, 12919.5, 12949.5,
    12980.5, 13011.5, 13041.5, 13072.5, 13102.5, 13133.5, 13164.5, 13193.5,
    13223.5, 13253.5, 13284.5, 13314.5, 13345.5, 13376.5, 13406.5, 13437.5,
    13467.5, 13498.5, 13529.5, 13558.5, 13588.5, 13618.5, 13649.5, 13679.5,
    13710.5, 13741.5, 13771.5, 13802.5, 13832.5, 13863.5, 13894.5, 13924.5,
    13954.5, 13984.5, 14015.5, 14045.5, 14076.5, 14107.5, 14137.5, 14168.5,
    14198.5, 14229.5, 14260.5, 14289.5, 14319.5, 14349.5, 14380.5, 14410.5,
    14441.5, 14472.5, 14502.5, 14533.5, 14563.5, 14594.5, 14625.5, 14654.5,
    14684.5, 14714.5, 14745.5, 14775.5, 14806.5, 14837.5, 14867.5, 14898.5,
    14928.5, 14959.5, 14990.5, 15019.5, 15049.5, 15079.5, 15110.5, 15140.5,
    15171.5, 15202.5, 15232.5, 15263.5, 15293.5, 15324.5,
    15355.5, 15386.5, 15415.5, 15446.5, 15476.5
  ]


base = date(1970,01,01)
arcMap =  {}
index = 1
for aBandtime in arcBandTime:
    d = date.fromordinal(int(base.toordinal() + aBandtime))
    arcMap[d.strftime('%Y-%m')] = str(index)
    index += 1

############################################################################

def ratio490Threshold(region):
    if region in ratio490Threshholds:
        return ratio490Threshholds.get(region)
    else:
        return  '0.65'  # default

def arcAuxdata(region, year, month):
    if region in arcFileIdByRegion:
        arcDayFile = 'ALID' + arcFileIdByRegion[region] + '_PLREC9D_TS012SR.nc'
        arcNightFile = 'ALID' + arcFileIdByRegion[region] + '_PLREC9N_TS012SR.nc'
        arcBand = 'lswt_time' + arcMap[year + '-' + month]

        # check also if arcDayFile AND arcNightFile exist as netcdf products:
        if not arcDayFile in lakes_alid_ncfiles or not arcNightFile in lakes_alid_ncfiles:
            return ('""', '""', '""')

        return (arcDayFile, arcNightFile, arcBand)
    else:
        return ('""', '""', '""')

 
