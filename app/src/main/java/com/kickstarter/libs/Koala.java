package com.kickstarter.libs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.utils.MapUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Koala {
  private @NonNull final TrackingType client;

  public Koala(@NonNull final TrackingType client) {
    this.client = client;
  }

  // DISCOVERY
  public void trackDiscovery(@NonNull final DiscoveryParams params) {
    client.trackMap("Discover List View", discoveryParamsProperties(params));
  }

  public void trackDiscoveryFilters() {
    client.trackMap("Discover Switch Modal", new HashMap<String, Object>(){{
      put("modal_type", "filters");
    }});
  }

  public void trackDiscoveryFilterSelected(@NonNull final DiscoveryParams params) {
    client.trackMap("Discover Modal Selected Filter", discoveryParamsProperties(params));
  }

  // PROJECT STAR
  public void trackProjectStar(@NonNull final Project project) {
    if (project.isStarred()) {
      client.track("Project Star");
    } else {
      client.track("Project Unstar");
    }
  }

  // SESSION EVENTS
  public void trackLoginRegisterTout(@Nullable final String intent) {
    if (intent == null) {
      client.track("Application Login or Signup");
    } else {
      client.trackMap("Application Login or Signup", new HashMap<String, Object>() {{
        put("intent", intent); // do we have a "compact" equiv to get rid of nulls?
      }});
    }
  }

  public void trackLoginSuccess() {
    client.track("Login");
  }

  public void trackLoginError() {
    client.track("Errored User Login");
  }

  public void trackTwoFactorAuthView() {
    client.track("Two-factor Authentication Confirm View");
  }

  public void trackTwoFactorResendCode() {
    client.track("Two-factor Authentication Resend Code");
  }

  public void trackRegisterFormView() {
    client.track("User Signup");
  }

  public void trackRegisterError() {
    client.track("Errored User Signup");
  }

  public void trackRegisterSuccess() {
    client.track("New User");
  }

  public void trackForgotPasswordFormView() {
    client.track("Forgot Password View");
  }

  public void trackForgotPasswordRequestSuccess() {
    client.track("Forgot Password Requested");
  }

  public void trackForgotPasswordRequestFailed() {
    client.track("Forgot Password Errored");
  }

  public void trackFacebookConfirmation() {
    client.track("Facebook Confirm");
  }

  public void trackFacebookLoginSuccess() {
    client.track("Facebook Login");
  }

  public void trackFacebookLoginError() {
    client.track("Errored Facebook Login");
  }

  public void trackLogout() {
    client.track("Logout");
  }

  public void trackSignupNewsletterToggle(final boolean sendNewsletters) {
    client.trackMap("Signup Newsletter Toggle", new HashMap<String, Object>() {{
      put("send_newsletters", sendNewsletters);
    }});
  }

  // HELPERS
  @NonNull private static Map<String, Object> discoveryParamsProperties(@NonNull final DiscoveryParams params) {

    final Map<String, Object> properties = Collections.unmodifiableMap(new HashMap<String, Object>(){{

      put("staff_picks", String.valueOf(params.staffPicks()));
      put("sort", params.sort().toString());
      put("page", String.valueOf(params.page()));
      put("per_page", String.valueOf(params.perPage()));

      Category category = params.category();
      if (category != null) {
        putAll(categoryProperties(category));
      }

    }});

    return MapUtils.prefixKeys(properties, "discover_");
  }

  @NonNull private static Map<String, Object> categoryProperties(@NonNull final Category category) {
    return Collections.unmodifiableMap(new HashMap<String, Object>() {{
      put("category_id", String.valueOf(category.id()));
      put("category_name", String.valueOf(category.name()));
    }});
  }
}
