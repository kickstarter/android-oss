package com.kickstarter.viewmodels.outputs;

import android.support.v7.widget.SwitchCompat;

import com.kickstarter.models.User;

import rx.Observable;

public interface SettingsViewModelOutputs {
  Observable<SwitchCompat> sendNewsletterConfirmation();
  Observable<Void> updateSuccess();
  Observable<User> user();
}
