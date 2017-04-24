package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class ProjectSearchResultViewHolder extends KSViewHolder {
  protected Project project;
  protected final Delegate delegate;

  @Bind(R.id.project_stats_text_view) TextView projectStatsTextView;
  @Bind(R.id.project_name_text_view) TextView projectNameTextView;
  @Bind(R.id.project_image_view) ImageView projectImageView;

  @BindString(R.string.search_stats) String searchStats;

  protected @Inject KSString ksString;

  public interface Delegate {
    void projectSearchResultClick(ProjectSearchResultViewHolder viewHolder, Project project);
  }

  public ProjectSearchResultViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;

    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Context context = context();

    project = ObjectUtils.requireNonNull((Project) data, Project.class);
    projectNameTextView.setText(project.name());
    projectStatsTextView.setText(createProjectStatsSpannable(context), TextView.BufferType.SPANNABLE);

    final Photo photo = project.photo();
    if (photo != null) {
      projectImageView.setVisibility(View.VISIBLE);
      Picasso.with(context).load(photo.small()).into(projectImageView);
    } else {
      projectImageView.setVisibility(View.INVISIBLE);
    }
  }

  private @NonNull SpannableString createProjectStatsSpannable(final Context context) {
    final String percentFunded = String.valueOf((int) project.percentageFunded());
    final String daysToGo = " " + NumberUtils.format(ProjectUtils.deadlineCountdownValue(project)) + " ";

    final SpannableString projectStatsSpannable = new SpannableString(ksString.format(searchStats,
      "percent_funded", percentFunded,
      "days_to_go", daysToGo
    ));

    final int percentFundedIndex = projectStatsSpannable.toString().indexOf(percentFunded + "% ");
    final int daysToGoIndex = projectStatsSpannable.toString().indexOf(daysToGo);

    projectStatsSpannable.setSpan(new TextAppearanceSpan(context, R.style.BodyGreen), percentFundedIndex, percentFundedIndex + percentFunded.length() + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    projectStatsSpannable.setSpan(new TextAppearanceSpan(context, R.style.BodyPrimaryMedium), daysToGoIndex, daysToGoIndex + daysToGo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    return projectStatsSpannable;
  }

  @Override
  public void onClick(final @NonNull View view) {
    delegate.projectSearchResultClick(this, project);
  }
}

