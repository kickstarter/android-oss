package com.kickstarter.libs;

import com.google.gson.Gson;
import com.kickstarter.libs.preferences.StringPreferenceType;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

public class CurrentUser extends CurrentUserType {
  private final StringPreferenceType accessTokenPreference;
  private final DeviceRegistrarType deviceRegistrar;
  private final StringPreferenceType userPreference;

  private final BehaviorSubject<User> user = BehaviorSubject.create();

  public CurrentUser(final @NonNull StringPreferenceType accessTokenPreference,
    final @NonNull DeviceRegistrarType deviceRegistrar,
    final @NonNull Gson gson,
    final @NonNull StringPreferenceType userPreference) {
    this.accessTokenPreference = accessTokenPreference;
    this.deviceRegistrar = deviceRegistrar;
    this.userPreference = userPreference;

    this.user
      .skip(1)
      .filter(ObjectUtils::isNotNull)
      .subscribe(u -> userPreference.set(gson.toJson(u, User.class)));

    this.user.onNext(gson.fromJson(userPreference.get(), User.class));
  }

  @Override
  public @Nullable User getUser() {
    return this.user.getValue();
  }

  @Override
  public boolean exists() {
    return getUser() != null;
  }

  public String getAccessToken() {
    return this.accessTokenPreference.get();
  }

  @Override
  public void login(final @NonNull User newUser, final @NonNull String accessToken) {
    Timber.d("Login user %s", newUser.name());

    this.accessTokenPreference.set(accessToken);
    this.user.onNext(newUser);
    this.deviceRegistrar.registerDevice();
  }

  @Override
  public void logout() {
    Timber.d("Logout current user");

    this.userPreference.delete();
    this.accessTokenPreference.delete();
    this.user.onNext(null);
    this.deviceRegistrar.unregisterDevice();
  }

  @Override
  public void refresh(final @NonNull User freshUser) {
    this.user.onNext(freshUser);
  }

  @Override
  public @NonNull Observable<User> observable() {
    return this.user;
  }
}
