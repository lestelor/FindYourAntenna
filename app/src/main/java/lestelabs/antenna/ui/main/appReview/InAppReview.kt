package lestelabs.antenna.ui.main

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import com.google.android.play.core.tasks.Task
import com.squareup.okhttp.Dispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope


object InAppReview {


    fun inAppReview(context: Context, activity:Activity) {

        //val manager = ReviewManagerFactory.create(context)
        val manager = FakeReviewManager(context)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = request.result
                Log.d("cfauli", "inAppReview reviewinfo " + reviewInfo)

                val flow = manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener { _ ->
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                    Log.d("cfauli", "inAppReview flow ")
                }
            } else {
                // There was some problem, continue regardless of the result.
                Log.d("cfauli", "inAppReview problem")
            }
        }
    }
}

