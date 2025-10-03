-dontobfuscate

# Keep any custom View so XML inflation can reflectively construct it.
#-keep class com.kickstarter.** extends android.view.View {
#  public <init>(android.content.Context);
#  public <init>(android.content.Context, android.util.AttributeSet);
#  public <init>(android.content.Context, android.util.AttributeSet, int);
#  public <init>(android.content.Context, android.util.AttributeSet, int, int);
#}
#
## Retrofit services + interfaces
#-keep interface com.kickstarter.services.** { *; }
#-keepnames interface com.kickstarter.services.**
#
## Envelope models
#-keep class com.kickstarter.services.apirequests.** { *; }
#-keepnames class com.kickstarter.services.apirequests.** { *; }
#
#-keep class com.kickstarter.libs.** { *; }
#-keepnames class com.kickstarter.libs.** { *; }
#
## Reflection method for play services dependency
#-keep class com.google.android.gms.ads.identifier.** { *; }
#
## Keeping source file and line number on error stacktraces
#-keepattributes SourceFile,LineNumberTable