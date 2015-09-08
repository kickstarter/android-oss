package com.kickstarter.ui.viewholders;

import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.libs.DateTimeUtils;
import com.kickstarter.libs.Money;
import com.kickstarter.models.Activity;
import com.kickstarter.presenters.ActivityFeedPresenter;
import com.kickstarter.ui.viewholders.ActivityListViewHolder;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProjectStateChangedPositiveViewHolder extends ActivityListViewHolder {
  @Bind(R.id.card_view) CardView cardView;
  @Bind(R.id.left_stat_first) TextView leftStatFirstTextView;
  @Bind(R.id.left_stat_second) TextView leftStatSecondTextView;
  @Bind(R.id.project_photo) ImageView projectPhotoImageView;
  @Bind(R.id.right_stat_first) TextView rightStatFirstTextView;
  @Bind(R.id.right_stat_second) TextView rightStatSecondTextView;
  @Bind(R.id.title) TextView titleTextView;
  @Inject Money money;

  public ProjectStateChangedPositiveViewHolder(final View view, final ActivityFeedPresenter presenter) {
    super(view, presenter);
    ButterKnife.bind(this, view);
    ((KsrApplication) view.getContext().getApplicationContext()).component().inject(this);
  }

  @Override
  public void onBind(final Activity activity) {
    super.onBind(activity);

    switch (activity.category()) {
      case LAUNCH:
        cardView.setCardBackgroundColor(view.getResources().getColor(R.color.blue_darken_10));
        leftStatFirstTextView.setText(money.formattedCurrency(activity.project().goal(), activity.project()
          .currencyOptions()));
        leftStatSecondTextView.setText(view.getResources().getString(R.string.goal));
        rightStatFirstTextView.setText(view.getResources().getString(R.string.Launched));
        rightStatSecondTextView.setText(activity.project().launchedAt().toString(DateTimeUtils.defaultFormatter()));
        titleTextView.setText(view.getResources().getString(
          R.string.creator_launched_a_project, activity.user().name(), activity.project().name()));
        break;
      case SUCCESS:
        cardView.setCardBackgroundColor(view.getResources().getColor(R.color.green_darken_10));
        leftStatFirstTextView.setText(money.formattedCurrency(activity.project().pledged(), activity.project()
          .currencyOptions()));
        leftStatSecondTextView.setText(view.getResources().getString(
          R.string.pledged_of_goal,
          money.formattedCurrency(activity.project().goal(), activity.project().currencyOptions(), true)));
        rightStatFirstTextView.setText(view.getResources().getString(R.string.funded));
        rightStatSecondTextView.setText(activity.createdAt().toString(DateTimeUtils.defaultFormatter()));
        titleTextView.setText(view.getResources()
          .getString(R.string.project_was_successfully_funded, activity.project().name()));
        break;
      default:
        cardView.setCardBackgroundColor(view.getResources().getColor(R.color.green_darken_10));
        leftStatFirstTextView.setText("");
        leftStatSecondTextView.setText("");
        rightStatFirstTextView.setText("");
        rightStatSecondTextView.setText("");
        titleTextView.setText("");
    }
    // TODO: Switch to "You launched a project" if current user launched
    //return view.getResources().getString(R.string.creator_launched_a_project, activity.user().name(), activity.project().name());

    Picasso.with(view.getContext())
      .load(activity.project().photo().full())
      .into(projectPhotoImageView);
  }
}
