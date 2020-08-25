

-dontusemixedcaseclassnames lestelabs.antenna.ui.main.**
-dontusemixedcaseclassnames lestelabs.antenna.ui.main.rest.models.**

-keepclassmembers class * implements java.io.Serializable


-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-keep public class lestelabs.antenna.ui.main.rest.models.** {*;}






