package com.kickstarter.libs;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.kickstarter.libs.preferences.StringPreference;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.User;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

public class CurrentUser {
  private final StringPreference accessTokenPreference;
  private final DeviceRegistrarType deviceRegistrar;
  private final StringPreference userPreference;

  private final BehaviorSubject<User> user = BehaviorSubject.create();
  private User currentUser;

  public CurrentUser(final @NonNull StringPreference accessTokenPreference,
    final @NonNull DeviceRegistrarType deviceRegistrar,
    final @NonNull Gson gson,
    final @NonNull StringPreference userPreference) {
    this.accessTokenPreference = accessTokenPreference;
    this.deviceRegistrar = deviceRegistrar;
    this.userPreference = userPreference;

    user.subscribe(user -> currentUser = user);
    user
      .skip(1)
      .filter(user -> user != null)
      .subscribe(user -> userPreference.set(gson.toJson(user, User.class)));

    user.onNext(gson.fromJson(userPreference.get(), User.class));
  }

  @Deprecated
  public User getUser() {
    return currentUser;
  }

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

  /**
   * Emits every time the user's logged in state changes. It will emit `true`
   * if the user went from logged out to logged in, and `false` otherwise.
   */
  public Observable<Boolean> loginChange() {
    return user.buffer(2, 1)
      .map(prevAndNewUser -> {
        final User[] users = prevAndNewUser.toArray(new User[prevAndNewUser.size()]);
        return users[0] == null && users[1] != null;
      });
  }

  /**
   * Emits a boolean that determines if the user is logged in or not. The returned
   * observable will emit immediately with the logged in state, and then again
   * each time the current user is updated.
   */
  public @NonNull Observable<Boolean> isLoggedIn() {
    return user.map(ObjectUtils::isNotNull);
  }

  /**
   * Emits only values of a logged in user. The returned observable may never emit.
   */
  public Observable<User> loggedInUser() {
    return observable().filter(ObjectUtils::isNotNull);
  }

  /**
   * Emits only values of a logged out user. The returned observable may never emit.
   */
  public Observable<User> loggedOutUser() {
    return observable().filter(ObjectUtils::isNull);
  }
}
