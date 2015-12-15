package com.kickstarter.viewmodels.outputs;

import com.kickstarter.models.User;

import rx.Observable;

public interface SettingsViewModelOutputs {
  Observable<Void> updateSuccess();
  Observable<User> user();
}
