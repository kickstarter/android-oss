package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.Notification;
import com.kickstarter.services.ApiClient;
import com.kickstarter.ui.activities.ManageNotificationActivity;
import com.kickstarter.ui.adapters.ManageNotificationsAdapter;
import com.kickstarter.ui.viewholders.ManageNotificationsViewHolder;
import com.kickstarter.viewmodels.errors.ManageNotificationsViewModelErrors;
import com.kickstarter.viewmodels.outputs.ManageNotificationsViewModelOutputs;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class ManageNotificationsViewModel extends ViewModel<ManageNotificationActivity> implements
  ManageNotificationsViewModelErrors, ManageNotificationsViewModelOutputs, ManageNotificationsAdapter.Delegate {
  @Inject ApiClient client;

  // INPUTS
  private PublishSubject<Void> switchClick = PublishSubject.create();

  // OUTPUTS
  private final BehaviorSubject<Notification> projectNotification = BehaviorSubject.create();
  public Observable<Notification> projectNotification() {
    return projectNotification;
  }

  private final PublishSubject<Void> toggleSuccess = PublishSubject.create();
  public Observable<Void> toggleSuccess() {
    return toggleSuccess;
  }

  // ERRORS
  private final PublishSubject<Throwable> savePreferenceErrors = PublishSubject.create();
  public Observable<String> unableToSavePreferenceError() {
    return savePreferenceErrors
      .map(__ -> null); // todo: correct error string
  }

  public final ManageNotificationsViewModelOutputs outputs = this;
  public final ManageNotificationsViewModelErrors errors = this;

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<List<Notification>> initialNotifications = refreshNotifications();

    view
      .compose(Transformers.takePairWhen(initialNotifications)) // grab list of notifications
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vn -> {
        vn.first.loadProjects(vn.second); // load in adapter
      });
  }

  public Observable<List<Notification>> refreshNotifications() {
    return client.fetchProjectNotifications()
      .compose(Transformers.neverApiError());
  }

  @Override
  public void switchClicked(final @NonNull ManageNotificationsViewHolder viewHolder,
    final @NonNull Notification notification, final boolean toggleValue) {

    client.updateProjectNotifications(notification.id(), toggleValue)
      .compose(Transformers.pipeErrorsTo(savePreferenceErrors))
      .subscribe(projectNotification);  // this needs to update the whole list
  }
}
