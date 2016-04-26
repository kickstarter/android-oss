package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.kickstarter.R;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.SwitchCompatUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.ProjectNotification;
import com.kickstarter.viewmodels.ProjectNotificationViewModel;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

@RequiresActivityViewModel(ProjectNotificationViewModel.class)
public final class ProjectNotificationViewHolder extends KSViewHolder {
  private final ProjectNotificationViewModel viewModel;

  protected @Bind(R.id.enabled_switch) SwitchCompat enabledSwitch;
  protected @Bind(R.id.project_name) TextView projectNameTextView;

  protected @BindString(R.string.profile_settings_error) String unableToSaveString;

  public ProjectNotificationViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);

    viewModel = new ProjectNotificationViewModel(environment());

    RxView.clicks(enabledSwitch)
      .map(__ -> enabledSwitch.isChecked())
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(viewModel.inputs::enabledSwitchClick);

    viewModel.outputs.projectName()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(projectNameTextView::setText);

    viewModel.outputs.enabledSwitch()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(SwitchCompatUtils.setCheckedWithoutAnimation(enabledSwitch));

    viewModel.errors.showUnableToSaveProjectNotificationError()
      .map(__ -> unableToSaveString)
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ViewUtils.showToast(context()));
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final ProjectNotification projectNotification = requireNonNull((ProjectNotification) data, ProjectNotification.class);
    viewModel.projectNotification(projectNotification);
  }
}
