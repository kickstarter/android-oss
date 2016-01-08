package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.models.Activity;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class ProjectStateChangedPositiveViewHolder extends ActivityListViewHolder {
  protected @Bind(R.id.card_view) CardView cardView;
  protected @Bind(R.id.left_stat_first) TextView leftStatFirstTextView;
  protected @Bind(R.id.left_stat_second) TextView leftStatSecondTextView;
  protected @Bind(R.id.project_photo) ImageView projectPhotoImageView;
  protected @Bind(R.id.right_stat_first) TextView rightStatFirstTextView;
  protected @Bind(R.id.right_stat_second) TextView rightStatSecondTextView;
  protected @Bind(R.id.title) TextView titleTextView;

  protected @BindColor(R.color.blue_darken_10) int blueDarken10Color;
  protected @BindColor(R.color.green_darken_10) int greenDarken10Color;

  protected @BindString(R.string.activity_project_state_change_creator_launched_a_project) String creatorLaunchedProjectString;
  protected @BindString(R.string.activity_project_state_change_goal) String goalString;
  protected @BindString(R.string.activity_project_state_change_launched) String launchedString;
  protected @BindString(R.string.activity_project_state_change_pledged_of_goal) String pledgedOfGoalString;
  protected @BindString(R.string.project_status_funded) String fundedString;
  protected @BindString(R.string.activity_project_state_change_project_was_successfully_funded) String projectSuccessfullyFundedString;

  @Inject KSCurrency ksCurrency;
  @Inject KSString ksString;

  private final Delegate delegate;

  public interface Delegate {
    void projectStateChangedPositiveClicked(ProjectStateChangedPositiveViewHolder viewHolder, Activity activity);
  }

  public ProjectStateChangedPositiveViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
  }

  @Override
  public void onBind(final @NonNull Object datum) {
    super.onBind(datum);

    final Context context = view.getContext();

    switch (activity.category()) {
      case Activity.CATEGORY_LAUNCH:
        cardView.setCardBackgroundColor(blueDarken10Color);
        leftStatFirstTextView.setText(ksCurrency.format(activity.project().goal(), activity.project()));
        leftStatSecondTextView.setText(goalString);
        rightStatFirstTextView.setText(launchedString);
        rightStatSecondTextView.setText(DateTimeUtils.mediumDate(activity.project().launchedAt()));
        titleTextView.setText(ksString.format(
          creatorLaunchedProjectString,
          "creator_name",
          activity.user().name(),
          "project_name",
          activity.project().name()
        ));
        break;
      case Activity.CATEGORY_SUCCESS:
        cardView.setCardBackgroundColor(greenDarken10Color);
        leftStatFirstTextView.setText(ksCurrency.format(activity.project().pledged(), activity.project()));
        leftStatSecondTextView.setText(ksString.format(
          pledgedOfGoalString,
          "goal",
          ksCurrency.format(activity.project().goal(), activity.project(), true)
        ));
        rightStatFirstTextView.setText(fundedString);
        rightStatSecondTextView.setText(DateTimeUtils.mediumDate(activity.createdAt()));
        titleTextView.setText(ksString.format(
          projectSuccessfullyFundedString,
          "project_name",
          activity.project().name()
        ));
        break;
      default:
        cardView.setCardBackgroundColor(greenDarken10Color);
        leftStatFirstTextView.setText("");
        leftStatSecondTextView.setText("");
        rightStatFirstTextView.setText("");
        rightStatSecondTextView.setText("");
        titleTextView.setText("");
    }
    // TODO: Switch to "You launched a project" if current user launched
    //return context.getString(R.string.creator_launched_a_project, activity.user().name(), activity.project().name());

    Picasso.with(context)
      .load(activity.project().photo().full())
      .into(projectPhotoImageView);
  }

  @OnClick(R.id.card_view)
  public void onClick() {
    delegate.projectStateChangedPositiveClicked(this, activity);
  }
}
