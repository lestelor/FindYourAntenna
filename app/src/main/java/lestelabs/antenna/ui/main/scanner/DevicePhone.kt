package lestelabs.antenna.ui.main.scanner

class DevicePhone (
    var type:String? = "",
    var psc: Int? = 0,
    var lac: Int? = 0,
    var mnc: Int? = 0,
    var mcc: Int? = 0,
    var cid: Int? = 0,
    var dbm: Int? = 0,
    var networkType: Int? = 0,
    var totalCellId : String = mcc.toString() + mnc.toString() + cid.toString(),
    var totalCellIdLat: Double?=0.0,
    var totalCellIdLon: Double?=0.0


)