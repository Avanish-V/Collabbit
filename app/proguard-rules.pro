# === Kotlinx Serialization & Ktor ProGuard Rules ===
-keepattributes *Annotation*,ElementValue*,AnnotationDefault*,EnclosingMethod,InnerClasses,Signature

# Keep all @Serializable annotated classes and all their fields
-keep @kotlinx.serialization.Serializable class * { *; }

-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <init>(...);
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable static ** Companion;
}
-keepclassmembers class **$serializer {
    public static ** INSTANCE;
}

# Keep custom serializers
-keep class * implements kotlinx.serialization.KSerializer { *; }
-keep interface kotlinx.serialization.KSerializer { *; }

# Keep all DTOs, data models, requests, responses in all packages
-keep class com.iota.campusX.** { *; }
-keepclassmembers class com.iota.campusX.** { *; }

# Ktor Client
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Firebase & Firestore
-keep class com.google.firebase.** { *; }
-keep class com.google.firestore.** { *; }

# Keep default constructors for reflection/serialization
-keepclassmembers class * {
    public <init>();
}

# SLF4J and management warnings
-dontwarn java.lang.management.**
-dontwarn javax.management.**
-dontwarn org.slf4j.**
-dontwarn io.agora.**
