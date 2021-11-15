package lestelabs.antenna.ui.main.data

import android.util.Log
import lestelabs.antenna.R

object Operators {

    data class Operator(val mnc: Int, val description: String, val icono: Int)
    val pairList: Array<Operator> = arrayOf(
        Operator(1,"Vodafone", R.drawable.ic_vodafone),
        Operator(3,"Orange", R.drawable.ic_orange),
        Operator(4,"MasMovil", R.drawable.circle_dot_yellow_icon),
        Operator(7,"Telefonica", R.drawable.ic_movistar),
        Operator(0,"OMV", R.drawable.ic_omv_green)
    )

    val mvnos: Array<String> = arrayOf("Euskaltel","Aire","Cota","Laurentino", "Tele Caravaca", "TeleAst" )
    val frecuencias: Array<String> = arrayOf("700","800","900","1800","2100","2600","3500")

    fun getOperatorByMnc (mnc:Int):String {
        val find = pairList.first { it.mnc==mnc}
        return find.description
    }

    fun getIconoByOperator (operator:String): Int {
        var find:Operator? = null
        find = pairList.firstOrNull { it.description == operator }
        if (find==null) find = pairList.first { it.description == "OMV" }

        return find.icono
    }
}
