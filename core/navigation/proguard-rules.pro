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

## Keep `kotlinx.serialization` runtime classes
#-keep class kotlinx.serialization.** { *; }
#
## Keep classes that are annotated with `@Serializable`
#-keep @kotlinx.serialization.Serializable class * { *; }
#
## Keep companion objects of serializable classes
#-keepclassmembers class * {
#    @kotlinx.serialization.Serializable <methods>;
#    public static final ** Companion;
#}
#
## Keep `serializer()` function in companion objects of serializable classes
#-keepclassmembers class * {
#    public final static Lkotlinx/serialization/KSerializer; serializer(...);
#}
