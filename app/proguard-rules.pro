# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.

# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

-keep class com.squareup.* { *; }
-keep interface com.squareup.* { *; }
-keep class retrofit2.* { *; }
-keep interface retrofit2.* { *;}
-keep interface com.squareup.* { *; }


-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}


-dontwarn rx.**
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes Signature
-keepattributes *Annotation*
-keep class lestelabs.antenna.ui.main.rest.* { *; }
-keep interface lestelabs.antenna.ui.main.rest.* { *; }
-keep class okhttp3.* { *; }
-keep interface okhttp3.* { *; }

-dontwarn okhttp3.**