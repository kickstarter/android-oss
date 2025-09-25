# Keep any custom View so XML inflation can reflectively construct it.
-keep class com.kickstarter.** extends android.view.View {
  public <init>(android.content.Context);
  public <init>(android.content.Context, android.util.AttributeSet);
  public <init>(android.content.Context, android.util.AttributeSet, int);
  public <init>(android.content.Context, android.util.AttributeSet, int, int);
}

-keep interface com.kickstarter.services.** { *; }
-keepnames interface com.kickstarter.services.**

-keep class com.kickstarter.services.apirequests.** { *; }
-keepnames class com.kickstarter.services.apirequests.** { *; }

-keep class com.kickstarter.libs.** { *; }
-keepnames class com.kickstarter.libs.** { *; }