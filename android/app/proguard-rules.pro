# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ================================================================================================
# GENERAL RULES
# ================================================================================================

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotations
-keepattributes *Annotation*

# Keep generic signatures for reflection
-keepattributes Signature

# Keep exception info
-keepattributes Exceptions

# ================================================================================================
# KOTLIN
# ================================================================================================

# Kotlin metadata
-keep class kotlin.Metadata { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.** {
    volatile <fields>;
}

# ================================================================================================
# ANDROID & ANDROIDX
# ================================================================================================

# Keep all native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep View constructors (needed for inflation from XML)
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep Activity method parameters (for onClick handlers in XML)
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# Keep Parcelables
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep Enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ================================================================================================
# JETPACK COMPOSE
# ================================================================================================

# Keep Composables
-keep class androidx.compose.** { *; }
-keep @androidx.compose.runtime.Composable class * { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# Keep Composable lambda names for debugging
-keep class androidx.compose.runtime.internal.ComposableLambdaImpl { *; }

# ================================================================================================
# ROOM DATABASE
# ================================================================================================

# Keep Room entities
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Room DAOs
-keep interface * extends androidx.room.Dao {
    *;
}

# Keep Room type converters
-keep class * {
    @androidx.room.TypeConverter <methods>;
}

# ================================================================================================
# GSON
# ================================================================================================

# Gson uses generic type information stored in a class file when working with fields
-keepattributes Signature

# Gson specific classes
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class sun.misc.Unsafe { *; }

# Keep all model classes used with Gson
-keep class com.redscreenfilter.core.model.** { *; }
-keep class com.redscreenfilter.data.** { *; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Retain generic signatures for Gson
-keepattributes Signature

# ================================================================================================
# DATASTORE
# ================================================================================================

-keep class androidx.datastore.*.** { *; }

# ================================================================================================
# WORKMANAGER
# ================================================================================================

-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.InputMerger
-keep class androidx.work.impl.WorkManagerImpl { *; }
-keep class androidx.work.** { *; }

# Keep our Worker classes
-keep class com.redscreenfilter.worker.** { *; }

# ================================================================================================
# APP-SPECIFIC CLASSES
# ================================================================================================

# Keep our service classes
-keep class com.redscreenfilter.service.** { *; }

# Keep our receiver classes
-keep class com.redscreenfilter.receiver.** { *; }

# Keep Activity and Fragment classes
-keep class com.redscreenfilter.MainActivity { *; }
-keep class com.redscreenfilter.SplashActivity { *; }

# Keep all custom views
-keep class com.redscreenfilter.service.OverlayView { *; }

# Keep data classes and models
-keep class com.redscreenfilter.data.** { *; }
-keep class com.redscreenfilter.core.model.** { *; }

# Keep feature modules
-keep class com.redscreenfilter.feature.** { *; }

# Keep utility classes with public methods
-keep class com.redscreenfilter.utils.** { *; }

# ================================================================================================
# GOOGLE PLAY SERVICES
# ================================================================================================

-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ================================================================================================
# MATERIAL COMPONENTS
# ================================================================================================

-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# ================================================================================================
# LOTTIE
# ================================================================================================

-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# ================================================================================================
# SECURITY & CRYPTO
# ================================================================================================

-keep class androidx.security.crypto.** { *; }
-dontwarn com.google.crypto.tink.util.KeysDownloader
-dontwarn com.google.api.client.http.**
-dontwarn org.joda.time.Instant

# ================================================================================================
# REMOVE LOGGING IN RELEASE
# ================================================================================================

# Remove all Log calls (except Log.e for errors)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}

# ================================================================================================
# OPTIMIZATION
# ================================================================================================

# Allow aggressive optimization
-optimizationpasses 5
-dontusemixedcaseclassnames
-verbose

# Optimization options
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
