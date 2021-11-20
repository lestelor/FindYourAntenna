package lestelabs.antenna.ui.main.tools


object Constants {

    val SPEEDTESTDOWNLOAD: Array<String> = arrayOf(
        "http://ipv4.ikoula.testdebit.info/1M.iso",
        "http://ipv4.ikoula.testdebit.info/10M.iso",
        //default
        "http://ipv4.ikoula.testdebit.info/100M.iso",
        "https://ipv4.scaleway.testdebit.info:6881/1M.iso",
        "https://ipv4.scaleway.testdebit.info:6881/10M.iso",
        "https://ipv4.scaleway.testdebit.info:6881/100M.iso",
        "ftp://speedtest.tele2.net/1MB.zip",
        "ftp://speedtest.tele2.net/10MB.zip",
        "ftp://speedtest.tele2.net/100MB.zip"
    )


    val SPEEDTESTUPLOAD: Array<String> = arrayOf(
        //default
        "http://ipv4.ikoula.testdebit.info/",
        "ftp://speedtest.tele2.net/upload/"
    )

    const val MINMOBILESIGNALBLACK: String = "120"
    const val MINMOBILESIGNALRED: String = "115"
    const val MINMOBILESIGNALYELLOW: String = "100"
    const val MINMOBILESIGNALGREEN: String = "50"

    const val MINWIFISIGNALBLACK: String = "90"
    const val MINWIFISIGNALRED: String = "80"
    const val MINWIFISIGNALYELLOW: String = "67"
    const val MINWIFISIGNALGREEN: String = "30"


}



