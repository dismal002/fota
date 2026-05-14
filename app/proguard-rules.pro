# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/dismal/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

-dontwarn com.adups.fota.**
-keep class com.adups.fota.** { *; }
-keep interface com.adups.fota.** { *; }
