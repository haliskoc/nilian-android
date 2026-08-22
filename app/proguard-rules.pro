# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /path/to/proguard-android-optimize.txt

-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}
