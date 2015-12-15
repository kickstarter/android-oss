package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.kickstarter.R;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.SwitchCompatUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Notification;
import com.kickstarter.viewmodels.ProjectNotificationViewModel;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

@RequiresViewModel(ProjectNotificationViewModel.class)
public final class ProjectNotificationViewHolder extends KSViewHolder {
  protected @Bind(R.id.project_name) TextView projectNameTextView;
  protected @Bind(R.id.notification_switch) SwitchCompat notificationSwitch;

  protected @BindString(R.string.___Unable_to_save) String unableToSaveString;
  protected @BindString(R.string.___Updated) String updatedString;

  final PublishSubject<ProjectNotificationViewModel> viewModel = PublishSubject.create();

  public ProjectNotificationViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);

    // TODO: bind to lifecycle
    viewModel
      .compose(Transformers.takeWhen(RxView.clicks(this.notificationSwitch)))
      .subscribe(vm -> vm.inputs.switchClick(this.notificationSwitch.isChecked()));

    viewModel
      .switchMap(vm -> vm.outputs.notification())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::renderNotification);

    viewModel
      .switchMap(vm -> vm.outputs.updateSuccess())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> ViewUtils.showToast(view.getContext(), updatedString));

    viewModel
      .switchMap(vm -> vm.errors.unableToSavePreferenceError())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> ViewUtils.showToast(view.getContext(), unableToSaveString));
  }

  @Override
  public void onBind(final @NonNull Object datum) {
    final ProjectNotificationViewModel viewModel = (ProjectNotificationViewModel) datum;
    this.viewModel.onNext(viewModel);
  }

  public void renderNotification(final @NonNull Notification notification) {
    projectNameTextView.setText(notification.project().name());
    SwitchCompatUtils.setCheckedWithoutAnimation(notificationSwitch, notification.email() && notification.mobile());
  }
}
