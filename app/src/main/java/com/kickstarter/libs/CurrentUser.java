package com.kickstarter.libs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.kickstarter.libs.preferences.StringPreference;
import com.kickstarter.models.User;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

public class CurrentUser extends CurrentUserType {
  private final StringPreference accessTokenPreference;
  private final DeviceRegistrarType deviceRegistrar;
  private final StringPreference userPreference;

  private final BehaviorSubject<User> user = BehaviorSubject.create();

  public CurrentUser(final @NonNull StringPreference accessTokenPreference,
    final @NonNull DeviceRegistrarType deviceRegistrar,
    final @NonNull Gson gson,
    final @NonNull StringPreference userPreference) {
    this.accessTokenPreference = accessTokenPreference;
    this.deviceRegistrar = deviceRegistrar;
    this.userPreference = userPreference;

    user
      .skip(1)
      .filter(user -> user != null)
      .subscribe(user -> userPreference.set(gson.toJson(user, User.class)));

    user.onNext(gson.fromJson(userPreference.get(), User.class));
  }

  /**
   * @deprecated Prefer {@link #observable()} instead.
   */
  @Deprecated
  public @Nullable User getUser() {
    return user.getValue();
  }

  /**
   * @deprecated Prefer {@link #observable()} instead.
   */
  @Deprecated
  public boolean exists() {
    return getUser() != null;
  }

  public String getAccessToken() {
    return accessTokenPreference.get();
  }

  public void login(final @NonNull User newUser, final @NonNull String accessToken) {
    Timber.d("Login user %s", newUser.name());

    accessTokenPreference.set(accessToken);
    user.onNext(newUser);
    deviceRegistrar.registerDevice();
  }

  public void logout() {
    Timber.d("Logout current user");

    userPreference.delete();
    accessTokenPreference.delete();
    user.onNext(null);
    deviceRegistrar.unregisterDevice();
  }

  public void refresh(final @NonNull User freshUser) {
    user.onNext(freshUser);
  }

  /**
   * Returns an observable representing the current user. It emits immediately
   * with the current user, and then again each time the user is updated.
   */
  public Observable<User> observable() {
    return user;
  }
}
