package com.kickstarter.viewmodels.errors;

import rx.Observable;

public interface SettingsViewModelErrors {
  Observable<String> settingsErrors();
}
