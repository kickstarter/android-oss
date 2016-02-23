package com.kickstarter.libs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.User;

import rx.Observable;

public abstract class CurrentUserType {

  public abstract void login(final @NonNull User newUser, final @NonNull String accessToken);
  public abstract void logout();
  public abstract String getAccessToken();
  public abstract void refresh(final @NonNull User freshUser);
  public abstract Observable<User> observable();

  @Deprecated
  public abstract @Nullable User getUser();

  @Deprecated
  public boolean exists() {
    return getUser() != null;
  }

  /**
   * Emits a boolean that determines if the user is logged in or not. The returned
   * observable will emit immediately with the logged in state, and then again
   * each time the current user is updated.
   */
  public @NonNull Observable<Boolean> isLoggedIn() {
    return observable().map(ObjectUtils::isNotNull);
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
