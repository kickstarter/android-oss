package com.kickstarter.libs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.User;

import rx.Observable;

public abstract class CurrentUserType {

  public abstract void login(final @NonNull User newUser, final @NonNull String accessToken);
  public abstract void logout();
  public abstract @Nullable String getAccessToken();
  public abstract void refresh(final @NonNull User freshUser);

  /**
   * Returns an observable representing the current user. It emits immediately
   * with the current user, and then again each time the user is updated.
   */
  public abstract @NonNull Observable<User> observable();

  /**
   * Returns the most recently emitted user from the user observable.
   * * @deprecated Prefer {@link #observable()}
   */
  @Deprecated
  public abstract @Nullable User getUser();

  /**
   * Returns a boolean that determines if there is a currently logged in user or not.
   * @deprecated Prefer {@link #observable()}
   */
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
  public @NonNull Observable<User> loggedInUser() {
    return observable().filter(ObjectUtils::isNotNull);
  }

  /**
   * Emits only values of a logged out user. The returned observable may never emit.
   */
  public @NonNull Observable<User> loggedOutUser() {
    return observable().filter(ObjectUtils::isNull);
  }
}
