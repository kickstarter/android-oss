package com.kickstarter.libs;

import com.google.gson.Gson;
import com.kickstarter.libs.preferences.StringPreference;
import com.kickstarter.models.User;

import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class CurrentUser {
  private final StringPreference accessTokenPreference;
  private static final Gson gson = new Gson();
  private final StringPreference userPreference;

  private final PublishSubject<User> userSubject = PublishSubject.create();
  private User currentUser;

  public CurrentUser(final StringPreference accessTokenPreference,
    final StringPreference userPreference) {
    this.accessTokenPreference = accessTokenPreference;
    this.userPreference = userPreference;

    userSubject.subscribe(user -> currentUser = user);
    userSubject.onNext(gson.fromJson(userPreference.get(), User.class));
  }

  public User getUser() {
    return currentUser;
  }

  public String getAccessToken() {
    return accessTokenPreference.get();
  }

  public void set(final User newUser, final String accessToken) {
    Timber.d("Set current user to %s", newUser.name());

    userPreference.set(gson.toJson(newUser, User.class));
    userSubject.onNext(newUser);
  }

  public void unset() {
    Timber.d("Unset current user");

    userPreference.delete();
    userSubject.onNext(null);
  }

  public boolean exists() {
    return userPreference.isSet();
  }

  public Observable<User> userSubject() {
    return userSubject.mergeWith(Observable.just(currentUser));
  }

  public Observable<Boolean> loginEvent() {
    return userSubject.buffer(2, 1)
      .map(prevAndNewUser -> {
        final Object[] users = prevAndNewUser.toArray();
        return users[0] == null && users[1] != null;
      });
  }
}
