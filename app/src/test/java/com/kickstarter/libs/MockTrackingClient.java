package com.kickstarter.libs;

import com.kickstarter.libs.utils.extensions.ConfigExtension;
import com.kickstarter.mock.factories.ConfigFactory;
import com.kickstarter.models.Location;
import com.kickstarter.models.User;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class MockTrackingClient extends TrackingClientType {
  private static final long DEFAULT_TIME = DateTime.parse("2018-11-02T18:42:05Z").getMillis() / 1000;
  private final Type type;
  private final ExperimentsClientType optimizely;
  @Nullable private User loggedInUser;
  private Config config = ConfigFactory.config();

  public MockTrackingClient(final @NonNull CurrentUserType currentUser, final @NonNull CurrentConfigType currentConfig, final Type type, final @NonNull ExperimentsClientType optimizely) {
    this.type = type;
    this.optimizely = optimizely;
    currentUser.observable().subscribe(this::propagateUser);
    currentConfig.observable().subscribe(c -> this.config = c);
  }

  private void propagateUser(final @Nullable User user) {
    if (user != null) {
      identify(user);
    }
    this.loggedInUser = user;
  }

  @Override
  public void identify(final @NotNull User user) {
    this.identifiedId.onNext(user.id());
  }

  @Override
  public void reset() {
    this.loggedInUser = null;
    this.identifiedId.onNext(null);
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public static class Event {
    private final String name;
    private final Map<String, Object> properties;
    Event(final String name, final Map<String, Object> properties) {
      this.name = name;
      this.properties = properties;
    }
  }

  private final @NonNull PublishSubject<Event> events = PublishSubject.create();
  public final @NonNull Observable<String> eventNames = this.events.map(e -> e.name);
  public final @NonNull Observable<Map<String, Object>> eventProperties = this.events.map(e -> e.properties);
  public final @NonNull BehaviorSubject<Long> identifiedId = BehaviorSubject.create();

  @Override
  public void track(final @NotNull String eventName, final @NotNull Map<String, ?> additionalProperties) {
    this.events.onNext(new Event(eventName, combinedProperties(additionalProperties)));
  }

  @Override
  public ExperimentsClientType optimizely() {
    return this.optimizely;
  }

  @Override
  @NonNull
  public Type type() {
    return this.type;
  }

  //Default property values
  @Override
  protected String brand() {
    return "Google";
  }

  @Override
  protected int buildNumber() {
    return 9999;
  }

  @Override
  protected String[] currentVariants() {
    return ConfigExtension.currentVariants(this.config);
  }

  @Override
  protected String deviceDistinctId() {
    return "uuid";
  }

  @Override
  protected String deviceFormat() {
    return "phone";
  }

  @Override
  protected String deviceOrientation() {
    return "portrait";
  }

  @Override
  protected JSONArray enabledFeatureFlags() {
    final JSONArray combinedFeatures = new JSONArray(this.optimizely.enabledFeatures(this.loggedInUser));
    final JSONArray configFeatures = ConfigExtension.enabledFeatureFlags(this.config);
    if (configFeatures != null) {
      for (int i = 0; i < configFeatures.length(); i++) {
        try {
          combinedFeatures.put(configFeatures.get(i));
        } catch (JSONException ignored) {
        }
      }
    }
    return combinedFeatures;
  }

  @Override
  protected boolean isGooglePlayServicesAvailable() {
    return false;
  }

  @Override
  protected boolean isTalkBackOn() {
    return false;
  }

  @Override
  protected String manufacturer() {
    return "Google";
  }

  @Override
  protected String model() {
    return "Pixel 3";
  }

  @Override
  protected String OSVersion() {
    return "9";
  }

  @Override
  protected long time() {
    return DEFAULT_TIME;
  }

  @Override
  public User loggedInUser() {
    return this.loggedInUser;
  }

  @Override
  protected String userAgent() {
    return "agent";
  }

  @Override
  protected String userCountry(final @NonNull User user) {
    final Location location = user.location();
    final String configCountry = this.config != null ? this.config.countryCode() : null;
    return location != null ? location.country() : configCountry;
  }

  @Override
  protected String sessionCountry() {
    return "US";
  }
  @Override
  protected String versionName() {
    return "9.9.9";
  }

  @Override
  protected boolean wifiConnection() {
    return false;
  }
}
