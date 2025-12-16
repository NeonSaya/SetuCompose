# --- Ultimate ProGuard Rules for Retrofit/Gson --- #
# This is an aggressive set of rules designed to fix stubborn reflection/generic issues.

# 1. Most Critical Rule: Preserve Generic Signatures & Metadata
# This tells R8 not to discard the "manual" for generic types like List<T>.
-keepattributes Signature,InnerClasses,KotlinMetaData,RuntimeVisibleAnnotations,AnnotationDefault

# 2. App-specific Rules: Protect your API models and services
# This keeps everything in your `data` package safe.
-keep,includedescriptorclasses public class com.neonsaya.setucompose.data.** { *; }
-keep,includedescriptorclasses public interface com.neonsaya.setucompose.data.** { *; }

# Keep the generic type information for all TypeToken subclasses.
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# 3. Library Rules: Protect the libraries themselves
# This prevents R8 from breaking the internal workings of Retrofit, Gson, and OkHttp.
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.**

-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

-keep class okio.** { *; }
-dontwarn okio.**

# 4. Gson-specific field rule (from previous attempts, still good practice)
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# --- Default Android Studio Rules --- #
-keepattributes SourceFile,LineNumberTable
