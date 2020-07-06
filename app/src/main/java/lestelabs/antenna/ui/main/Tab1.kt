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
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.anastr.speedviewlib.SpeedView
import com.google.android.gms.ads.*
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.IRepeatListener
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
    private var internetSpeedDownload: Float = 0.0f
    private var internetSpeedUpload: Float = 0.0f
    private var speedTestRunningStep = 0
    lateinit var mAdView : AdView

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
        val tvDownload = view.findViewById<View>(R.id.tvSpeedtestDownload) as TextView
        val tvUpload = view.findViewById<View>(R.id.tvSpeedtestUpload) as TextView
        val tvLatency = view.findViewById<View>(R.id.tvLatency) as TextView
        val speedometer = view.findViewById<SpeedView>(R.id.speedView)
        val button: Button = view.findViewById(R.id.btSpeedTest)

        mAdView = view.findViewById(R.id.adViewFragment1)
        val adView = AdView(requireActivity())

        MobileAds.initialize(requireActivity())
        val adRequest = AdRequest.Builder().build()

        mAdView.loadAd(adRequest)
        mAdView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdFailedToLoad(errorCode : Int) {
                // Code to be executed when an ad request fails.
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        }

        speedometer.unit = "Mbps"
        speedometer.minSpeed = 0.0f
        speedometer.maxSpeed = 300.0f
        speedometer.withTremble = false
        speedometer.sections[0].color = Color.RED
        speedometer.sections[0].endOffset = 0.1f
        speedometer.sections[1].color = Color.YELLOW
        speedometer.sections[1].startOffset = 0.1f
        speedometer.sections[1].endOffset = 0.3333333f
        speedometer.sections[2].color = Color.GREEN
        speedometer.sections[2].startOffset = 0.3333333f

        speedTestSocket.downloadSetupTime = 1000
        speedTestSocket.uploadSetupTime = 1000
        speedTestSocket.socketTimeout = 5000

        // Avoids exception android.os.NetworkOnMainThreadException
        //at android.os.StrictMode$AndroidBlockGuardPolicy.onNetwork(StrictMode.java --- Only for debug use Async in production
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        button.setOnClickListener {
            if (speedTestRunningStep==0) {
                //speedTestSocket.startDownload("https://ipv4.scaleway.testdebit.info:8080/10M.iso",1000)
                speedTestSocket.startDownloadRepeat("https://ipv4.scaleway.testdebit.info:8080/1M.iso",
                    5000, 1000, object : IRepeatListener {
                        override fun onCompletion(report: SpeedTestReport) {

                            internetSpeedDownload = report.transferRateBit.toFloat()/1000000.0f
                            requireActivity().runOnUiThread(Runnable {
                                speedometer.speedTo(0.0f,1000)
                                speedTestRunningStep = 1
                                Log.d("cfauli","[COMPLETED] step" + speedTestRunningStep)
                                tvDownload.text = "%.1f".format(internetSpeedDownload)

                                // Start upload test (once the download test finishes)---------------------------------------------------------
                                speedTestSocket.startUploadRepeat("http://ipv4.ikoula.testdebit.info/",
                                    5000, 1000, 100000000, object : IRepeatListener {
                                        override fun onCompletion(report: SpeedTestReport) {

                                            internetSpeedDownload = report.transferRateBit.toFloat()/1000000.0f
                                            requireActivity().runOnUiThread(Runnable {
                                                speedometer.speedTo(0.0f,1000)
                                                speedTestRunningStep = 2
                                                tvUpload.text = "%.1f".format(internetSpeedUpload)


                                                // Test latency (once the upload test finishes)-----------------
                                                tvLatency.text = pingg("http://www.google.com").toString()
                                                speedTestRunningStep = 0
                                                button.setBackgroundResource(R.drawable.ic_switch_on_off)
                                                // End test latency  --------------------------------------------
                                            })
                                        }
                                        override fun onReport(report: SpeedTestReport) {
                                            // called when a upload report is dispatched
                                            internetSpeedUpload= report.transferRateBit.toFloat()/1000000.0f
                                            requireActivity().runOnUiThread(Runnable {
                                                speedometer.speedTo(internetSpeedUpload,1000)
                                            })
                                        }
                                    })
                                // End upload test---------------------------------------------------------------------------------------------
                            })
                            //
                        }

                        override fun onReport(report: SpeedTestReport) {
                            // called when a download report is dispatched
                            internetSpeedDownload = report.transferRateBit.toFloat()/1000000.0f
                            requireActivity().runOnUiThread(Runnable {
                                speedometer.speedTo(internetSpeedDownload,1000)
                            })
                        }
                    })
                button.setBackgroundResource(R.drawable.ic_switch_on_off_grey)
            }
        }

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

    fun pingg(domain: String): Long {
        val runtime = Runtime.getRuntime()
        var timeofping: Array<Long> = arrayOf(0,0,0,0)
        try {
            for (i in 0..3) {
                var a = System.currentTimeMillis() % 1000000
                var ipProcess = runtime.exec("/system/bin/ping -c 1 $domain")
                ipProcess.waitFor()
                var b = System.currentTimeMillis() % 1000000
                timeofping[i] = if (b <= a) {
                    1000000 - a + b
                } else {
                    b - a
                }
            }
        } catch (e: Exception) {
        }
        return timeofping.average().toLong()/2
    }

}
