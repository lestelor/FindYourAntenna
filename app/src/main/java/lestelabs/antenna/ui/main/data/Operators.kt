package lestelabs.antenna.ui.main.data

import lestelabs.antenna.R

object Operators {

    data class Operator(val mnc: Int, val description: String, val icono: Int)
    val pairList: Array<Operator> = arrayOf(
        Operator(1,"Vodafone", R.drawable.ic_vodafone),
        Operator(3,"Orange", R.drawable.ic_orange),
        Operator(4,"MasMovil", R.drawable.ic_vodafone),
        Operator(7,"Telefonica", R.drawable.ic_vodafone),
        Operator(0,"Otros", R.drawable.ic_vodafone)
    )

    fun getOperatorByMnc (mnc:Int):String {
        val find = pairList.first { it.mnc==mnc}
        return find.description
    }

    fun getIconoByOperator (operator:String): Int {
        val find = pairList.first { it.description==operator}
        return find.icono
    }
}
