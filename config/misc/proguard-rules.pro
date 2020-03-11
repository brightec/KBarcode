####### General #######

-ignorewarnings

# For Guava:
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe

# Keep annotations
-keepattributes *Annotation*

# Put in place to ensure crashlytics gets good reports - see https://docs.fabric.io/android/crashlytics/dex-and-proguard.html
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

# Misc - Adhoc list
-keep class androidx.drawerlayout.widget.DrawerLayout { *; }
-keep class androidx.test.espresso.IdlingResource { *; }
-keep class androidx.test.espresso.IdlingRegistry { *; }
-keep class com.google.common.base.Preconditions { *; }
-keep class com.google.common.collect.Lists { *; }
-keep class androidx.recyclerview.widget.RecyclerView { *; }
-keep class androidx.appcompat.widget.AlertDialogLayout { *; }
-keep class com.google.firebase.iid.R { *; }
