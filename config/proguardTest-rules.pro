####### General #######

-ignorewarnings

# Keep annotations
-keepattributes *Annotation*

# JUnit
-dontnote junit.framework.**
-dontnote junit.runner.**
-dontwarn org.junit.**

# Espresso
-dontwarn android.test.**
-dontwarn androidx.test.**

# Misc
-dontwarn org.hamcrest.**
-dontwarn org.mockito.**
-dontwarn com.squareup.javawriter.JavaWriter

####### Project Specific #######

