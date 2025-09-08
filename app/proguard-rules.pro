# === Firebase and Firestore DTO Rules ===



-keep class com.example.** { *; }

-keep class io.agora.** { *; }
-dontwarn io.agora.**

# Notification-related DTOs
-keep class com.iota.campusX.Feature.Notification.data.** { *; }
-keep class com.iota.campusX.Feature.Notification.domain.** { *; }
-keep class com.iota.campusX.Screens.Setting.** { *; }

# Referenced User model
#-keep class com.iota.campusX.Feature.Post.domain.Models.User { *; }

# Post-related DTOs (already mostly present)
-keep class com.iota.campusX.Feature.Post.data.model.** { *; }
-keep class com.iota.campusX.Feature.UserProfile.data.** { *; }
-keep class com.iota.campusX.Feature.Search.Domain.Models.** { *; }
-keep class com.iota.campusX.Feature.Society.domain.models.** { *; }



# Firebase SDK
# Firebase Firestore rules
-keep class com.google.firebase.** { *; }
-keep class com.google.firestore.** { *; }
-keepattributes Signature
-keepattributes *Annotation*


# General rule to preserve constructors for all classes
-keepclassmembers class * {
    public <init>();
}

# Optional: Keep everything in your model packages (helpful during dev/debugging)
# You can scope this down for production to only required classes
-keep class com.iota.campusX.Feature.**.domain.** { *; }
-keep class com.iota.campusX.Feature.**.data.** { *; }


# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn java.lang.management.ManagementFactory
-dontwarn javax.management.InstanceNotFoundException
-dontwarn javax.management.MBeanRegistrationException
-dontwarn javax.management.MBeanServer
-dontwarn javax.management.MalformedObjectNameException
-dontwarn javax.management.ObjectInstance
-dontwarn javax.management.ObjectName
-dontwarn javax.servlet.ServletContainerInitializer
-dontwarn org.codehaus.janino.ClassBodyEvaluator
-dontwarn org.codehaus.janino.ScriptEvaluator
-dontwarn sun.reflect.Reflection

-dontwarn io.getstream.video.android.mock.StreamPreviewDataUtils
-dontwarn io.getstream.video.android.mock.StreamPreviewDataUtilsKt