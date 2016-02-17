package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

@RequiresViewModel(ProjectNotificationViewModel.class)
public final class ProjectNotificationViewHolder extends KSViewHolder {
  protected @Bind(R.id.project_name) TextView projectNameTextView;
  protected @Bind(R.id.notification_switch) SwitchCompat notificationSwitch;

  protected @BindString(R.string.profile_settings_error) String unableToSaveString;

  private final PublishSubject<ProjectNotificationViewModel> viewModel = PublishSubject.create();

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
      .switchMap(vm -> vm.errors.unableToSavePreferenceError())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> ViewUtils.showToast(view.getContext(), unableToSaveString));
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final ProjectNotificationViewModel viewModel = requireNonNull((ProjectNotificationViewModel) data, ProjectNotificationViewModel.class);
    this.viewModel.onNext(viewModel);
  }

  @Override
  public void onBind() {
  }

  public void renderNotification(final @NonNull Notification notification) {
    projectNameTextView.setText(notification.project().name());
    SwitchCompatUtils.setCheckedWithoutAnimation(notificationSwitch, notification.email() && notification.mobile());
  }
}
