# Keep any custom View so XML inflation can reflectively construct it.
-keep class com.kickstarter.** extends android.view.View {
  public <init>(android.content.Context);
  public <init>(android.content.Context, android.util.AttributeSet);
  public <init>(android.content.Context, android.util.AttributeSet, int);
  public <init>(android.content.Context, android.util.AttributeSet, int, int);
}

-keep interface com.kickstarter.services.** { *; }
-keepnames interface com.kickstarter.services.**
-keep class com.kickstarter.services.** { *; }
-keepnames class com.kickstarter.services.**

-keep class com.kickstarter.services.apiresponses.** { *; }
-keepnames class com.kickstarter.services.apiresponses.** { *; }

-keep class com.kickstarter.services.apirequests.** { *; }
-keepnames class com.kickstarter.services.apirequests.** { *; }

-keep class com.kickstarter.models.** { *; }
-keepnames class com.kickstarter.models.** { *; }
-keep class com.kickstarter.libs.** { *; }
-keepnames class com.kickstarter.libs.** { *; }
-keep class com.kickstarter.libs.Environment { *; }

-keep class com.kickstarter.models.MessageThread { *; }
-keepnames class com.kickstarter.models.MessageThread { *; }
-keep class com.kickstarter.services.apiresponses.MessageThreadEnvelope { *; }
-keepnames class com.kickstarter.services.apiresponses.MessageThreadEnvelope { *; }
-keep class com.kickstarter.models.Message { *; }
-keep class com.kickstarter.models.User { *; }
-keep class com.kickstarter.models.Project { *; }

# Retrofit HTTP annotations are required on methods
-keepclasseswithmembers interface * { @retrofit2.http.* <methods>; }
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses, MethodParameters

# Gson adapters/factories
-keep class * implements com.google.gson.TypeAdapterFactory { *; }
-keep class * extends com.google.gson.TypeAdapter { *; }
-keep @com.google.gson.annotations.JsonAdapter class * { *; }

# Preserve what Gson needs
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses, MethodParameters

#-keep class com.kickstarter.ApplicationModule { *; }
#-keep class com.kickstarter.InternalApplicationModule { *; }
#-keep class com.kickstarter.ExternalApplicationModule { *; }