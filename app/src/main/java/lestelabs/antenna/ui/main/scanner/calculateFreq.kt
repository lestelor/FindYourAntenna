package lestelabs.antenna.ui.main.scanner

fun calculateFreq (type:String?, arfcn:Int?):Int {
var offsetDL:Int = 0
    var initialDL:Int = 0

    when (type) {
        "LTE" -> {
            when {
                arfcn!!<=599 -> { initialDL = 2110
                    offsetDL = 0}// band 1

                arfcn!!<=1949 -> { initialDL = 1805
                    offsetDL = 1200}// band 3

                arfcn!!<=3450 -> { initialDL = 2620
                    offsetDL = 2750}// band 7

                arfcn!!<=3800 -> { initialDL = 925
                    offsetDL = 3450}// band 8

                arfcn!!<=6450 -> { initialDL = 791
                    offsetDL = 6150}// band 20

                arfcn!!<=38250-> { initialDL = 2570
                    offsetDL = 37750}// band 38

                else -> { initialDL = 0
                    offsetDL = 0}
            }
            return (initialDL + 0.1 * (arfcn!! - offsetDL)).toInt()
        }

        else -> return 0
    }


}