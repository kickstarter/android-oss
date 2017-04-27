package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.viewmodels.ProjectSearchResultHolderViewModel;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

public class ProjectSearchResultViewHolder extends KSViewHolder {
  private final ProjectSearchResultHolderViewModel.ViewModel viewModel;

  protected final Delegate delegate;

  @Bind(R.id.project_stats_text_view) TextView projectStatsTextView;
  @Bind(R.id.project_name_text_view) TextView projectNameTextView;
  @Bind(R.id.project_image_view) ImageView projectImageView;

  @BindString(R.string.search_stats) String searchStatsString;

  protected @Inject KSString ksString;

  public interface Delegate {
    void projectSearchResultClick(ProjectSearchResultViewHolder viewHolder, Project project);
  }

  public ProjectSearchResultViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);

    this.viewModel = new ProjectSearchResultHolderViewModel.ViewModel(environment());
    this.delegate = delegate;

    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);

    this.viewModel.outputs.projectImage()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setProjectImage);

    this.viewModel.outputs.projectName()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setProjectName);

    this.viewModel.outputs.projectStats()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setProjectStats);

    this.viewModel.outputs.notifyDelegateOfResultClick()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(project -> this.delegate.projectSearchResultClick(this, project));
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final ProjectSearchResultHolderViewModel.Data configData = ObjectUtils.requireNonNull(
      (ProjectSearchResultHolderViewModel.Data)data
    );
    this.viewModel.inputs.configureWith(configData);
  }

  void setProjectImage(final String imageUrl) {
    this.projectImageView.setVisibility(imageUrl == null ? View.INVISIBLE : View.VISIBLE);
    Picasso.with(context()).load(imageUrl).into(projectImageView);
  }

  void setProjectName(final String name) {
    this.projectNameTextView.setText(name);
  }

  void setProjectStats(final Pair<Integer, Integer> stats) {
    final String html = ksString.format(this.searchStatsString,
      "percent_funded", String.valueOf(stats.first),
      "days_to_go", String.valueOf(stats.second),
      "color", "#" + Integer.toHexString(context().getResources().getColor(R.color.green)).substring(2));

    this.projectStatsTextView.setText(Html.fromHtml(html));
  }

  @Override
  public void onClick(final @NonNull View view) {
    this.viewModel.inputs.onClick();
  }
}

