package com.kickstarter.libs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.models.User;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public class MockCurrentUser extends CurrentUserType {
  private final BehaviorSubject<User> user = BehaviorSubject.create();
  private @Nullable String accessToken;

  public MockCurrentUser() {
    user.onNext(null);
  }

  public MockCurrentUser(final @NonNull User initialUser) {
    user.onNext(initialUser);
  }

  @Override
  public void login(final @NonNull User newUser, final @NonNull String accessToken) {
    user.onNext(newUser);
    this.accessToken = accessToken;
  }

  @Override
  public void logout() {
    user.onNext(null);
    accessToken = null;
  }

  @Override
  public @Nullable String getAccessToken() {
    return accessToken;
  }

  @Override
  public void refresh(final @NonNull User freshUser) {
    user.onNext(freshUser);
  }

  @Override
  public Observable<User> observable() {
    return user;
  }

  @Nullable
  @Override
  public User getUser() {
    return user.getValue();
  }
}
