package com.kickstarter.ui.viewholders;

import android.view.View;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.kickstarter.R;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.SwitchCompatUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.ProjectNotification;
import com.kickstarter.viewmodels.ProjectNotificationViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

@RequiresActivityViewModel(ProjectNotificationViewModel.ViewModel.class)
public final class ProjectNotificationViewHolder extends KSViewHolder {
  private final ProjectNotificationViewModel.ViewModel viewModel;

  protected @Bind(R.id.enabled_switch) SwitchCompat enabledSwitch;
  protected @Bind(R.id.project_name) TextView projectNameTextView;

  protected @BindString(R.string.profile_settings_error) String unableToSaveString;

  public ProjectNotificationViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);

    this.viewModel = new ProjectNotificationViewModel.ViewModel(environment());

    RxView.clicks(this.enabledSwitch)
      .map(__ -> this.enabledSwitch.isChecked())
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this.viewModel.inputs::enabledSwitchClick);

    this.viewModel.outputs.projectName()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this.projectNameTextView::setText);

    this.viewModel.outputs.enabledSwitch()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(SwitchCompatUtils.setCheckedWithoutAnimation(this.enabledSwitch));

    this.viewModel.outputs.showUnableToSaveProjectNotificationError()
      .map(__ -> this.unableToSaveString)
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ViewUtils.showToast(context()));
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final ProjectNotification projectNotification = requireNonNull((ProjectNotification) data, ProjectNotification.class);
    this.viewModel.projectNotification(projectNotification);
  }
}
