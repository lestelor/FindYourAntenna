package lestelabs.antenna.ui.main.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics

object Crashlytics {

    fun controlPointCrashlytics(tabName: String, crashlyticsKey: Array<StackTraceElement>, crashlyticsKeyPrevious:String): String {
        val crashlyticsKeyAnt: String = crashlyticsKey[2].methodName + " " + crashlyticsKey[2].lineNumber.toString()
        FirebaseCrashlytics.getInstance().setCustomKey(tabName + "Previous", crashlyticsKeyPrevious)
        FirebaseCrashlytics.getInstance().setCustomKey(tabName + "End", crashlyticsKeyAnt)
        return crashlyticsKeyAnt
    }

}