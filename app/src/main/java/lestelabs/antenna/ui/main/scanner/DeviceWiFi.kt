package lestelabs.antenna.ui.main.scanner

class DeviceWiFi(
    var ssid:String? = "",
    var mac:String? = "",
    var security: String? = "",
    var channel: Int? = 0,
    var centerFreq1: Int? = 0,
    var centerFreq2: Int? = 0,
    var channelWidth: Int? = 0,
    var level: Int?=0,
    var operator: String? =""
)