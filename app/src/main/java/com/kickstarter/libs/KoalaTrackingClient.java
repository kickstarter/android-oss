package com.kickstarter.libs;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.util.Base64Utils;
import com.kickstarter.libs.qualifiers.ApplicationContext;
import com.kickstarter.libs.utils.MapUtils;
import com.kickstarter.services.KoalaBackgroundService;
import com.kickstarter.services.firebase.DispatcherKt;
import com.kickstarter.ui.IntentKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public final class KoalaTrackingClient extends TrackingClient {
  private static final String TAG = KoalaTrackingClient.class.getSimpleName();
  @Inject CurrentUserType currentUser;
  @Inject Build build;
  @Inject CurrentConfigType currentConfig;
  private final @NonNull Context context;

  public KoalaTrackingClient(
    final @ApplicationContext @NonNull Context context,
    final @NonNull CurrentUserType currentUser,
    final @NonNull Build build,
    final @NonNull CurrentConfigType currentConfig) {
    super(context, currentUser, build, currentConfig);

    this.context = context;
    this.currentUser = currentUser;
    this.build = build;
    this.currentConfig = currentConfig;
  }

  @Override
  public void track(final @NonNull String eventName, final @NonNull Map<String, Object> additionalProperties) {
    try {
      final String trackingData = getTrackingData(eventName, combinedProperties(additionalProperties));
      final String encodedTrackingData = Base64Utils.encodeUrlSafe(trackingData
        .getBytes());
      final Bundle bundle = new Bundle();
      bundle.putString(IntentKey.KOALA_EVENT_NAME, eventName);
      bundle.putString(IntentKey.KOALA_EVENT, encodedTrackingData);

      final String uniqueJobName = KoalaBackgroundService.BASE_JOB_NAME + System.currentTimeMillis();
      DispatcherKt.dispatchJob(this.context, KoalaBackgroundService.class, uniqueJobName, bundle);
      if (this.build.isDebug()) {
        Log.d(TAG, "Queued event:" + trackingData);
      }
    } catch (JSONException e) {
      if (this.build.isDebug()) {
        Timber.e("Failed to encode event: " + eventName);
      }
      Fabric.getLogger().e(KoalaTrackingClient.TAG, "Failed to encode event: " + eventName);
    }
  }

  private String getTrackingData(final @NonNull String eventName, final @NonNull Map<String, Object> newProperties) throws JSONException {
    final JSONObject trackingEvent = new JSONObject();
    trackingEvent.put("event", eventName);

    final Map<String, Object> compactProperties = MapUtils.compact(newProperties);
    final JSONObject propertiesJSON = new JSONObject();
    for (Map.Entry<String, Object> entry : compactProperties.entrySet()) {
      propertiesJSON.put(entry.getKey(), entry.getValue());
    }
    trackingEvent.put("properties", propertiesJSON);
    final JSONArray trackingArray = new JSONArray();
    trackingArray.put(trackingEvent);

    return trackingArray.toString();
  }

  @Override
  protected boolean cleanPropertiesOnly() {
    return false;
  }

}
