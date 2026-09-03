# FX Always – R8 rules for the release build (issue #6).
# Libraries ship their own consumer rules (Firebase, RevenueCat, Ktor, CameraX, ML Kit, WorkManager);
# what follows covers app code that is reached through reflection or serialization.

# --- kotlinx.serialization: keep serializers for app models (domain/*, data/*, AppSettingsPrefs models) ---
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.fxalways.**$$serializer { *; }
-keepclassmembers class com.fxalways.** { *** Companion; }
-keepclasseswithmembers class com.fxalways.** { kotlinx.serialization.KSerializer serializer(...); }
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> { static <1>$Companion Companion; }
-if @kotlinx.serialization.Serializable class ** { static **$* *; }
-keepclassmembers class <2>$<3> { kotlinx.serialization.KSerializer serializer(...); }
-if @kotlinx.serialization.Serializable class ** { public static ** INSTANCE; }
-keepclassmembers class <1> { public static <1> INSTANCE; kotlinx.serialization.KSerializer serializer(...); }

# --- Android components referenced from the manifest / system (widgets, FCM service, WorkManager) ---
-keep class com.fxalways.app.MainActivity { *; }
-keep class com.fxalways.app.FxMessagingService { *; }
-keep class com.fxalways.app.FxAlwaysWidgetProvider { *; }
-keep class com.fxalways.app.FxTravelerWidgetProvider { *; }
-keep class * extends androidx.work.ListenableWorker { <init>(android.content.Context, androidx.work.WorkerParameters); }

# --- Enums persisted by name in SharedPreferences / Firestore (UserProfile, ThemeMode, alert types…) ---
-keepclassmembers enum com.fxalways.** { public static **[] values(); public static ** valueOf(java.lang.String); }

# --- Firestore maps app data classes via reflection (toObject / set(data class)) ---
-keepclassmembers class com.fxalways.app.** { public <init>(); }
-keep class com.google.firebase.firestore.** { *; }

# --- ML Kit text recognition + CameraX (native / dynamic loading) ---
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_** { *; }
-dontwarn com.google.mlkit.**
-keep class androidx.camera.** { *; }

# --- RevenueCat KMP ---
-keep class com.revenuecat.purchases.** { *; }
-dontwarn com.revenuecat.purchases.**

# --- Ktor / OkHttp / coroutines ---
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn org.slf4j.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# --- Guava (only used transitively; silence missing-class warnings) ---
-dontwarn com.google.common.**
-dontwarn javax.annotation.**
-dontwarn javax.lang.model.**
-dontwarn org.checkerframework.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.j2objc.annotations.**

# --- Crashlytics: readable stack traces ---
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
-keep public class * extends java.lang.Exception
