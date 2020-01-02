package com.kickstarter.libs;

import android.content.Context;

import com.firebase.jobdispatcher.JobService;
import com.kickstarter.libs.qualifiers.ApplicationContext;
import com.kickstarter.libs.utils.MapUtils;
import com.kickstarter.services.KoalaBackgroundService;
import com.kickstarter.ui.IntentKey;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.NonNull;

public final class KoalaTrackingClient extends TrackingClient {
  @Inject CurrentUserType currentUser;
  @Inject Build build;
  @Inject CurrentConfigType currentConfig;

  public KoalaTrackingClient(
    final @ApplicationContext @NonNull Context context,
    final @NonNull CurrentUserType currentUser,
    final @NonNull Build build,
    final @NonNull CurrentConfigType currentConfig) {
    super(context, currentUser, build, currentConfig);

    this.currentUser = currentUser;
    this.build = build;
    this.currentConfig = currentConfig;
  }

  @Override
  public @NotNull Class<? extends JobService> backgroundServiceClass() {
    return KoalaBackgroundService.class;
  }

  @Override
  protected boolean cleanPropertiesOnly() {
    return false;
  }

  @Override
  public @NotNull String eventKey() {
    return IntentKey.KOALA_EVENT;
  }

  @Override
  public @NotNull String eventNameKey() {
    return IntentKey.KOALA_EVENT_NAME;
  }

  @Override
  public @NotNull String tag() {
    return KoalaTrackingClient.class.getSimpleName();
  }

  @Override
  public @NotNull String trackingData(final @NotNull String eventName, final @NotNull Map<String, ?> newProperties) throws JSONException {
    final JSONObject trackingEvent = new JSONObject();
    trackingEvent.put("event", eventName);

    final Map<String, ?> compactProperties = MapUtils.compact(newProperties);
    final JSONObject propertiesJSON = new JSONObject();
    for (Map.Entry<String, ?> entry : compactProperties.entrySet()) {
      propertiesJSON.put(entry.getKey(), entry.getValue());
    }
    trackingEvent.put("properties", propertiesJSON);
    final JSONArray trackingArray = new JSONArray();
    trackingArray.put(trackingEvent);

    return trackingArray.toString();
  }
}
