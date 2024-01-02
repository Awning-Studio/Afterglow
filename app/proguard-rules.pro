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

# 调试
# 移除调试信息
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
}

# Jsoup
-keep class org.jsoup.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembernames class com.awning.afterglow.module.edusystem.api.Timetable {
    <fields>; <methods>;
}
-keepclassmembernames class com.awning.afterglow.module.edusystem.api.TimetableItem {
    <fields>; <methods>;
}
-keepclassmembernames class com.awning.afterglow.module.edusystem.api.ExamPlan {
    <fields>; <methods>;
}
-keepclassmembernames class com.awning.afterglow.module.edusystem.api.ExamPlanItem {
    <fields>; <methods>;
}
-keepclassmembernames class com.awning.afterglow.module.edusystem.api.SchoolReport {
    <fields>; <methods>;
}
-keepclassmembernames class com.awning.afterglow.module.edusystem.api.SchoolReportItem {
    <fields>; <methods>;
}
-keepclassmembernames class com.awning.afterglow.module.edusystem.api.LevelReport {
    <fields>; <methods>;
}
-keepclassmembernames class com.awning.afterglow.module.edusystem.api.LevelReportItem {
    <fields>; <methods>;
}
-keepclassmembernames class com.awning.afterglow.module.secondclass.SecondClass {
    <fields>; <methods>;
}
-keepclassmembernames class com.awning.afterglow.module.secondclass.SecondClassItem {
    <fields>; <methods>;
}
-keepclassmembernames class com.awning.afterglow.request.waterfall.Waterfall$Session {
    <fields>; <methods>;
}
-keepclassmembernames class com.awning.afterglow.type.User {
    <fields>; <methods>;
}
-keepclassmembernames class com.awning.afterglow.module.quicknetwork.NetworkUser {
    <fields>; <methods>;
}
-keepclassmembernames class com.awning.afterglow.module.edusystem.api.TimetableAll {
    <fields>; <methods>;
}
-keepclassmembernames class com.awning.afterglow.module.edusystem.api.CourseInfo {
    <fields>; <methods>;
}
-keepclassmembernames class com.awning.afterglow.module.edusystem.api.TimetableAllItem {
    <fields>; <methods>;
}

# Volley
-keepclassmembers,allowshrinking,allowobfuscation class com.android.volley.NetworkDispatcher {
    void processRequest();
}
-keepclassmembers,allowshrinking,allowobfuscation class com.android.volley.CacheDispatcher {
    void processRequest();
}

-dontwarn javax.annotation.Nonnull
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault
-dontwarn javax.annotation.WillClose
-dontwarn javax.annotation.meta.TypeQualifierDefault
-dontwarn com.google.errorprone.annotations.MustBeClosed
-dontwarn org.jspecify.annotations.NullMarked