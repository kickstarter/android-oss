package com.kickstarter.ui.view_holders;

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
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProjectStateChangedPositiveViewHolder extends ActivityListViewHolder {
  @InjectView(R.id.card_view) CardView card_view;
  @InjectView(R.id.left_stat_first) TextView left_stat_first;
  @InjectView(R.id.left_stat_second) TextView left_stat_second;
  @InjectView(R.id.project_photo) ImageView project_photo;
  @InjectView(R.id.right_stat_first) TextView right_stat_first;
  @InjectView(R.id.right_stat_second) TextView right_stat_second;
  @InjectView(R.id.title) TextView title;
  @Inject Money money;

  public ProjectStateChangedPositiveViewHolder(final View view, final ActivityFeedPresenter presenter) {
    super(view, presenter);
    ButterKnife.inject(this, view);
    ((KsrApplication) view.getContext().getApplicationContext()).component().inject(this);
  }

  @Override
  public void onBind(final Activity activity) {
    super.onBind(activity);

    switch (activity.category()) {
      case LAUNCH:
        card_view.setCardBackgroundColor(view.getResources().getColor(R.color.blue_darken_10));
        left_stat_first.setText(money.formattedCurrency(activity.project().goal(), activity.project().currencyOptions()));
        left_stat_second.setText(view.getResources().getString(R.string.goal));
        right_stat_first.setText(view.getResources().getString(R.string.Launched));
        right_stat_second.setText(activity.project().launchedAt().toString(DateTimeUtils.defaultFormatter()));
        title.setText(view.getResources().getString(
          R.string.creator_launched_a_project, activity.user().name(), activity.project().name()));
        break;
      case SUCCESS:
        card_view.setCardBackgroundColor(view.getResources().getColor(R.color.green_darken_10));
        left_stat_first.setText(money.formattedCurrency(activity.project().pledged(), activity.project().currencyOptions()));
        left_stat_second.setText(view.getResources().getString(
          R.string.pledged_of_goal,
          money.formattedCurrency(activity.project().goal(), activity.project().currencyOptions(), true)));
        right_stat_first.setText(view.getResources().getString(R.string.funded));
        right_stat_second.setText(activity.createdAt().toString(DateTimeUtils.defaultFormatter()));
        title.setText(view.getResources().getString(R.string.project_was_successfully_funded, activity.project().name()));
        break;
      default:
        card_view.setCardBackgroundColor(view.getResources().getColor(R.color.green_darken_10));
        left_stat_first.setText("");
        left_stat_second.setText("");
        right_stat_first.setText("");
        right_stat_second.setText("");
        title.setText("");
    }
    // TODO: Switch to "You launched a project" if current user launched
    //return view.getResources().getString(R.string.creator_launched_a_project, activity.user().name(), activity.project().name());

    Picasso.with(view.getContext())
      .load(activity.project().photo().full())
      .into(project_photo);
  }
}
