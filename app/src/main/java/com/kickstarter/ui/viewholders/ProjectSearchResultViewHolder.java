package com.kickstarter.ui.viewholders;

import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.viewmodels.ProjectSearchResultHolderViewModel;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

public class ProjectSearchResultViewHolder extends KSViewHolder {
  protected final Delegate delegate;
  private final KSString ksString;
  private final ProjectSearchResultHolderViewModel.ViewModel viewModel;

  @Bind(R.id.search_result_deadline_countdown_text_view) TextView deadlineCountdownValueTextView;
  @Bind(R.id.search_result_deadline_unit_text_view) TextView deadlineCountdownUnitTextView;
  @Bind(R.id.project_name_text_view) TextView projectNameTextView;
  @Bind(R.id.project_image_view) ImageView projectImageView;
  @Bind(R.id.search_result_percent_funded_text_view) TextView percentFundedTextView;
  @Bind(R.id.search_result_funded_text_view) TextView fundedTextView;

  @BindString(R.string.discovery_baseball_card_stats_funded) String fundedString;
  @BindString(R.string.discovery_baseball_card_time_left_to_go) String toGoString;

  public interface Delegate {
    void projectSearchResultClick(ProjectSearchResultViewHolder viewHolder, Project project);
  }

  public ProjectSearchResultViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);

    this.viewModel = new ProjectSearchResultHolderViewModel.ViewModel(environment());
    this.delegate = delegate;
    this.ksString = environment().ksString();

    ButterKnife.bind(this, view);

    this.viewModel.outputs.deadlineCountdownValueTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.deadlineCountdownValueTextView::setText);

    this.viewModel.outputs.notifyDelegateOfResultClick()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(project -> this.delegate.projectSearchResultClick(this, project));

    this.viewModel.outputs.percentFundedTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.percentFundedTextView::setText);

    this.viewModel.outputs.projectForDeadlineCountdownUnitTextView()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(p ->
        this.deadlineCountdownUnitTextView.setText(ProjectUtils.deadlineCountdownDetail(p, context(), this.ksString))
      );

    this.viewModel.outputs.projectNameTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.projectNameTextView::setText);

    this.viewModel.outputs.projectPhotoUrl()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setProjectImageUrl);

    this.fundedTextView.setText(this.fundedString);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    @SuppressWarnings("unchecked")
    final Pair<Project, Boolean> projectAndIsFeatured = ObjectUtils.requireNonNull((Pair<Project, Boolean>) data);
    this.viewModel.inputs.configureWith(projectAndIsFeatured);
  }

  private void setProjectImageUrl(final @NonNull String imageUrl) {
    Picasso.with(context()).load(imageUrl).into(this.projectImageView);
  }

  @Override
  public void onClick(final @NonNull View view) {
    this.viewModel.inputs.projectClicked();
  }
}

