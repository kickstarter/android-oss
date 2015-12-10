package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxCompoundButton;
import com.kickstarter.R;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Notification;
import com.kickstarter.viewmodels.ManageNotificationsViewModel;
import com.kickstarter.viewmodels.ProjectNotificationViewModel;
import com.kickstarter.viewmodels.ProjectViewModel;
import com.kickstarter.viewmodels.SignupViewModel;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import timber.log.Timber;

@RequiresViewModel(ProjectNotificationViewModel.class)
public final class ProjectNotificationViewHolder extends KSViewHolder {
  protected @Bind(R.id.project_name) TextView projectNameTextView;
  protected @Bind(R.id.notification_switch) SwitchCompat notificationSwitch;

  final PublishSubject<ProjectNotificationViewModel> viewModel = PublishSubject.create();

  public ProjectNotificationViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);

    viewModel
      .compose(Transformers.takeWhen(RxView.clicks(this.notificationSwitch)))
      .subscribe(vm -> {
          vm.inputs.switchClick(this.notificationSwitch.isChecked());
        });

    viewModel
      .switchMap(vm -> vm.outputs.notification())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(notification -> {
        renderNotification(notification);
      });
  }

  @Override
  public void onBind(final @NonNull Object datum) {
    final ProjectNotificationViewModel viewModel = (ProjectNotificationViewModel) datum;
    this.viewModel.onNext(viewModel);
  }

  public void renderNotification(final @NonNull Notification notification) {
    projectNameTextView.setText(notification.project().name());
    notificationSwitch.setChecked(notification.email() && notification.mobile());
  }
}
