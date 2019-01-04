package com.kickstarter.libs;

import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import rx.Observable;

public abstract class CurrentUserType {

  /**
   * Call when a user has logged in. The implementation of `CurrentUserType` is responsible
   * for persisting the user and access token.
   */
  public abstract void login(final @NonNull User newUser, final @NonNull String accessToken);

  /**
   * Call when a user should be logged out.
   */
  public abstract void logout();

  /**
   * Get the logged in user's access token.
   */
  public abstract @Nullable String getAccessToken();

  /**
   * Updates the persisted current user with a fresh, new user.
   */
  public abstract void refresh(final @NonNull User freshUser);

  /**
   * Returns an observable representing the current user. It emits immediately
   * with the current user, and then again each time the user is updated.
   */
  public abstract @NonNull Observable<User> observable();

  /**
   * Returns the most recently emitted user from the user observable.
   * @deprecated Prefer {@link #observable()}
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
