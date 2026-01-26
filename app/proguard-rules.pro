# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.chonkcheck.android.**$$serializer { *; }
-keepclassmembers class com.chonkcheck.android.** {
    *** Companion;
}
-keepclasseswithmembers class com.chonkcheck.android.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit
-keepattributes Signature
-keepattributes Exceptions

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# OkHttp
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Auth0
-keep class com.auth0.** { *; }
-dontwarn com.auth0.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Sentry
-keep class io.sentry.** { *; }
-dontwarn io.sentry.**

# WorkManager workers
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class com.chonkcheck.android.data.sync.SyncWorker { *; }

# Sync classes
-keep class com.chonkcheck.android.data.sync.** { *; }
-keep class com.chonkcheck.android.domain.model.SyncStatus { *; }
-keep class com.chonkcheck.android.domain.model.SyncStatus$* { *; }

# Milestone DTOs
-keep class com.chonkcheck.android.data.api.dto.MilestoneDataDto { *; }
-keep class com.chonkcheck.android.data.api.dto.MilestonesResponse { *; }
-keep class com.chonkcheck.android.data.api.dto.MarkMilestoneViewedRequest { *; }
-keep class com.chonkcheck.android.data.api.dto.SuccessResponse { *; }

# Milestone domain models
-keep class com.chonkcheck.android.domain.model.Milestone* { *; }
-keep class com.chonkcheck.android.domain.model.PendingMilestones { *; }
