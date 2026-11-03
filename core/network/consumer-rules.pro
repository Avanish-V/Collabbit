# === Kotlinx Serialization Consumer Keep Rules ===

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
