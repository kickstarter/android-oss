package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.Money;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProjectStateChangedPositiveViewHolder extends ActivityListViewHolder {
  @Bind(R.id.card_view) CardView cardView;
  @Bind(R.id.left_stat_first) TextView leftStatFirstTextView;
  @Bind(R.id.left_stat_second) TextView leftStatSecondTextView;
  @Bind(R.id.project_photo) ImageView projectPhotoImageView;
  @Bind(R.id.right_stat_first) TextView rightStatFirstTextView;
  @Bind(R.id.right_stat_second) TextView rightStatSecondTextView;
  @Bind(R.id.title) TextView titleTextView;
  @BindColor(R.color.blue_darken_10) int blueDarken10Color;
  @BindColor(R.color.green_darken_10) int greenDarken10Color;
  @Inject Money money;

  private final Delegate delegate;

  public interface Delegate {
    void projectStateChangedPositiveClicked(@NonNull final ProjectStateChangedPositiveViewHolder viewHolder,
      @NonNull final Project project);
  }

  public ProjectStateChangedPositiveViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
  }

  @Override
  public void onBind(@NonNull final Object datum) {
    super.onBind(datum);

    final Context context = view.getContext();

    switch (activity.category()) {
      case Activity.CATEGORY_LAUNCH:
        cardView.setCardBackgroundColor(blueDarken10Color);
        leftStatFirstTextView.setText(money.formattedCurrency(activity.project().goal(), activity.project()
          .currencyOptions()));
        leftStatSecondTextView.setText(context.getString(R.string.goal));
        rightStatFirstTextView.setText(context.getString(R.string.Launched));
        rightStatSecondTextView.setText(activity.project().launchedAt().toString(DateTimeUtils.defaultFormatter()));
        titleTextView.setText(context.getString(
          R.string.creator_launched_a_project, activity.user().name(), activity.project().name()));
        break;
      case Activity.CATEGORY_SUCCESS:
        cardView.setCardBackgroundColor(greenDarken10Color);
        leftStatFirstTextView.setText(money.formattedCurrency(activity.project().pledged(), activity.project()
          .currencyOptions()));
        leftStatSecondTextView.setText(context.getString(
          R.string.pledged_of_goal,
          money.formattedCurrency(activity.project().goal(), activity.project().currencyOptions(), true)));
        rightStatFirstTextView.setText(context.getString(R.string.funded));
        rightStatSecondTextView.setText(activity.createdAt().toString(DateTimeUtils.defaultFormatter()));
        titleTextView.setText(context
          .getString(R.string.project_was_successfully_funded, activity.project().name()));
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
    delegate.projectStateChangedPositiveClicked(this, activity.project());
  }
}
