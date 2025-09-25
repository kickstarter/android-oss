# Keep any custom View so XML inflation can reflectively construct it.
-keep class com.kickstarter.** extends android.view.View {
  public <init>(android.content.Context);
  public <init>(android.content.Context, android.util.AttributeSet);
  public <init>(android.content.Context, android.util.AttributeSet, int);
  public <init>(android.content.Context, android.util.AttributeSet, int, int);
}

#-keep interface com.kickstarter.services.** { *; }
-keep interface com.kickstarter.services.ApiService
-keep class com.kickstarter.services.apiresponses.** { *; }
-keep class com.kickstarter.services.apirequests.** { *; }
-keep class com.kickstarter.models.** { *; }
-keep class com.kickstarter.libs.** { *; }