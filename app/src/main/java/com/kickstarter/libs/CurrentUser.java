package com.kickstarter.libs;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.kickstarter.libs.preferences.StringPreference;
import com.kickstarter.models.User;

import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class CurrentUser {
  private final StringPreference accessTokenPreference;
  private final Gson gson;
  private final PushNotifications pushNotifications;
  private final StringPreference userPreference;

  private final PublishSubject<User> userSubject = PublishSubject.create();
  private User currentUser;

  public CurrentUser(@NonNull final StringPreference accessTokenPreference,
    @NonNull final Gson gson,
    @NonNull final PushNotifications pushNotifications,
    @NonNull final StringPreference userPreference) {
    this.accessTokenPreference = accessTokenPreference;
    this.pushNotifications = pushNotifications;
    this.gson = gson;
    this.userPreference = userPreference;

    userSubject.subscribe(user -> currentUser = user);
    userSubject
      .skip(1)
      .filter(user -> user != null)
      .subscribe(user -> userPreference.set(gson.toJson(user, User.class)));

    userSubject.onNext(gson.fromJson(userPreference.get(), User.class));
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

  public void login(@NonNull final User newUser, @NonNull final String accessToken) {
    Timber.d("Login user %s", newUser.name());

    accessTokenPreference.set(accessToken);
    userSubject.onNext(newUser);
    pushNotifications.registerDevice();
  }

  public void logout() {
    Timber.d("Logout current user");

    userPreference.delete();
    accessTokenPreference.delete();
    userSubject.onNext(null);
    pushNotifications.unregisterDevice();
  }

  public Observable<User> observable() {
    return userSubject.mergeWith(Observable.just(currentUser));
  }

  public Observable<Boolean> loginChange() {
    return userSubject.buffer(2, 1)
      .map(prevAndNewUser -> {
        final User[] users = prevAndNewUser.toArray(new User[prevAndNewUser.size()]);
        return users[0] == null && users[1] != null;
      });
  }

  @Deprecated
  public Observable<User> loggedInUser() {
    return observable().filter(user -> user != null);
  }

  @Deprecated
  public Observable<User> loggedOutUser() {
    return observable().filter(user -> user == null);
  }
}
