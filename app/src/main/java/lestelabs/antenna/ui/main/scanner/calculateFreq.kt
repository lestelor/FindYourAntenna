package lestelabs.antenna.ui.main.scanner

fun calculateFreq (type:String?, arfcn:Int?):Double {
    var offsetDL = 0
    var initialDL: Int = 0
    var FREFOffs: Double = 0.0
    var FGlobal: Int = 0
    var NREFOff: Int = 0

    when (type) {
        "2G" -> {
            return when {
                arfcn!! <= 599 -> ((arfcn * 0.2 + 890) + 45)
                arfcn <= 1023 -> (890 + 0.2 * (arfcn - 1024) + 45)
                else -> 0.0
            }
        }

        "3G" -> {
            return when {
                arfcn!! <=763 -> 735.0+arfcn/5 // band19
                arfcn <=912 -> 1326.0+arfcn/5 // band21
                arfcn <=1513 -> 1575.0+arfcn/5 // band3
                arfcn <=1738 -> 1805.0+arfcn/5 // band4
                arfcn <=2563 -> 2175.0+arfcn/5 // band7
                arfcn <=3088 -> 340.0+arfcn/5 // band8
                arfcn <=3388 -> 1490.0+arfcn/5 // band10
                arfcn <=3787 -> 736.0+arfcn/5 // band11
                arfcn <=3903 -> -37.0+arfcn/5 // band12
                arfcn <=4043 -> -55.0+arfcn/5 // band13
                arfcn <=4143 -> -63.0+arfcn/5 // band14
                arfcn <=4638 -> -109.0+arfcn/5 // band20
                arfcn <=5038 -> 2580.0+arfcn/5 // band22
                arfcn <=5413 -> 910.0+arfcn/5 // band25
                arfcn <=5913 -> -291.0+arfcn/5 // band26
                arfcn <=6813 -> 131.0+arfcn/5 // band32
                else -> 0.0
            }
        } // https://www.sqimway.com/umts_band.php

        "4G" -> {
            when {
                arfcn!! <= 599 -> {
                    initialDL = 2110
                    offsetDL = 0
                }// band 1

                arfcn <= 1949 -> {
                    initialDL = 1805
                    offsetDL = 1200
                }// band 3

                arfcn <= 3450 -> {
                    initialDL = 2620
                    offsetDL = 2750
                }// band 7

                arfcn <= 3800 -> {
                    initialDL = 925
                    offsetDL = 3450
                }// band 8

                arfcn <= 6450 -> {
                    initialDL = 791
                    offsetDL = 6150
                }// band 20

                arfcn <= 38250 -> {
                    initialDL = 2570
                    offsetDL = 37750
                }// band 38

                else -> {
                    initialDL = 0
                    offsetDL = 0
                }
            }
            return (initialDL + 0.1 * (arfcn - offsetDL))
        }

        // The formula for 5G NR ARFCN is described in 3GPP TS 38.104 chapter 5.4.2.1.
        "5G" -> {
            when {
                arfcn!! <= 599999 -> {
                    FREFOffs = 0.0
                    FGlobal = 5
                    NREFOff = 0
                }// band 0-3000MHz
                arfcn <= 2016666 -> {
                    FREFOffs = 3000.0
                    FGlobal = 15
                    NREFOff = 600000
                }// band 3000-24.500MHz
                arfcn <= 3279165 -> {
                    FREFOffs = 24250.08
                    FGlobal = 60
                    NREFOff = 2016667
                }// 24.500-100.000MHz
            }
            return FREFOffs + FGlobal * (arfcn!! - NREFOff)
        }

        else -> return 0.0
    }
}


