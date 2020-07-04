package lestelabs.antenna.ui.main

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import kotlinx.android.synthetic.main.fragment_tab1.*
import kotlinx.android.synthetic.main.fragment_tab1.view.*
import lestelabs.antenna.R
import org.w3c.dom.Text

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

        view.btSpeetTest.setOnClickListener { view ->
            speedTestSocket.startDownload("ftp://speedtest.tele2.net/1MB.zip", 1500);
        }
        val tv: TextView = view.findViewById(R.id.tvSpeedtest);

        // add a listener to wait for speedtest completion and progress
        speedTestSocket.addSpeedTestListener(object : ISpeedTestListener {
            override fun onCompletion(report: SpeedTestReport) {
                tv.text = (report.transferRateBit.toInt()/1024).toString()
                // called when download/upload is complete
                //println("[COMPLETED] rate in octet/s : " + report.transferRateOctet)
                //println("[COMPLETED] rate in bit/s   : " + report.transferRateBit)

                //
            }

            override fun onError(speedTestError: SpeedTestError, errorMessage: String) {
                // called when a download/upload error occur
            }

            override fun onProgress(percent: Float, report: SpeedTestReport) {
                // called to notify download/upload progress
                //println("[PROGRESS] progress : $percent%")
                //println("[PROGRESS] rate in octet/s : " + report.transferRateOctet)
                //println("[PROGRESS] rate in bit/s   : " + report.transferRateBit)
                tv.text = (report.transferRateBit.toInt()/1024).toString()
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