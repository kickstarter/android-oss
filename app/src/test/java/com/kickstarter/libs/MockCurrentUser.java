package com.kickstarter.libs;

import com.kickstarter.models.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import rx.Observable;
import rx.subjects.BehaviorSubject;

public class MockCurrentUser extends CurrentUserType {
  private final BehaviorSubject<User> user = BehaviorSubject.create((User) null);
  private @Nullable String accessToken;

  public MockCurrentUser() {
    this.user.onNext(null);
  }

  public MockCurrentUser(final @NonNull User initialUser) {
    this.user.onNext(initialUser);
  }

  @Override
  public void login(final @NonNull User newUser, final @NonNull String accessToken) {
    this.user.onNext(newUser);
    this.accessToken = accessToken;
  }

  @Override
  public void logout() {
    this.user.onNext(null);
    this.accessToken = null;
  }

  @Override
  public @Nullable String getAccessToken() {
    return this.accessToken;
  }

  @Override
  public void refresh(final @NonNull User freshUser) {
    this.user.onNext(freshUser);
  }

  @Override
  public Observable<User> observable() {
    return this.user;
  }

  @Nullable
  @Override
  public User getUser() {
    return this.user.getValue();
  }
}
