-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.github.kittinunf.fuse.**$$serializer { *; }
-keepclassmembers class com.github.kittinunf.fuse.** {
    *** Companion;
}
-keepclasseswithmembers class com.github.kittinunf.fuse.** {
    kotlinx.serialization.KSerializer serializer(...);
}
