package lestelabs.antenna.ui.main

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.anastr.speedviewlib.SpeedView
import com.github.anastr.speedviewlib.components.Section
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import kotlinx.android.synthetic.main.fragment_tab1.view.*
import lestelabs.antenna.R


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [Tab1.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [Tab1.newInstance] factory method to
 * create an instance of this fragment.
 */
class Tab1 : Fragment() {
    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var mListener: OnFragmentInteractionListener? = null
    private val speedTestSocket = SpeedTestSocket()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val view: View = inflater.inflate(R.layout.fragment_tab1, container, false)
        val tv: TextView = view.findViewById(R.id.tvSpeedtest)
        val speedometer = view.findViewById<SpeedView>(R.id.speedView)
        var speedDowndEnd: Boolean = false
        speedometer.unit = "Mbps"
        speedometer.minSpeed = 0.0f
        speedometer.maxSpeed = 300.0f
        speedometer.withTremble = false
        /*speedometer.clearSections()
        speedometer.addSections(
            Section(0f, .1f, Color.LTGRAY)
            , Section(.1f, .4f, Color.YELLOW)
            , Section(.4f, .75f, Color.BLUE)
            , Section(.75f, .9f, Color.RED))*/
        speedometer.sections[0].color = Color.RED
        speedometer.sections[1].color = Color.BLUE
        speedometer.sections[2].color = Color.YELLOW
        //speedometer.sections[3].color = Color.LTGRAY


        // Avoids exception android.os.NetworkOnMainThreadException
        //at android.os.StrictMode$AndroidBlockGuardPolicy.onNetwork(StrictMode.java --- Only for debug use Async in production
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        view.btSpeetTest.setOnClickListener { view ->
            speedDowndEnd = false
            //speedTestSocket.startFixedDownload("http://ipv4.ikoula.testdebit.info/100M.iso", 10000);
            speedTestSocket.startDownload("https://ipv4.scaleway.testdebit.info:8080/10M.iso", 1000);
            //speedTestSocket.startUpload("http://ipv4.ikoula.testdebit.info/", 10000000, 1500);
        }



        // add a listener to wait for speedtest completion and progress
        speedTestSocket.addSpeedTestListener(object : ISpeedTestListener {
            override fun onCompletion(report: SpeedTestReport) {

                // called when download/upload is complete
                Log.d("cfauli","[COMPLETED] rate in octet/s : " + report.transferRateOctet)
                Log.d("cfauli","[COMPLETED] rate in bit/s   : " + report.transferRateBit)

                val internet8080Speed: Float = (report.transferRateBit.toFloat()/1000000.0f)
                tv.text = "%.4f".format(internet8080Speed)
            }

            override fun onError(speedTestError: SpeedTestError, errorMessage: String) {
                Log.d("cfauli", "[ERROR] rate 0")
            }

            override fun onProgress(percent: Float, report: SpeedTestReport) {
                // called to notify download/upload progress
                Log.d("cfauli","[PROGRESS] progress : $percent%")
                Log.d("cfauli","[PROGRESS] rate in octet/s : " + report.transferRateOctet)
                Log.d("cfauli","[PROGRESS] rate in bit/s   : " + report.transferRateBit)
                val internet8080Speed: Float = (report.transferRateBit.toFloat()/1000000.0f)
                Log.d("cfauli","Speedometer" + internet8080Speed.toString())


                    requireActivity().runOnUiThread(Runnable {
                            speedometer.speedTo(internet8080Speed,1000)
                    })
            }


        })

        // Inflate the layout for this fragment
        return view
    }



    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri?) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnFragmentInteractionListener) {
            context
        } else {
            throw RuntimeException(
                context.toString()
                        + " must implement OnFragmentInteractionListener"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri?)
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Tab1.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String?, param2: String?): Tab1 {
            val fragment = Tab1()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

}