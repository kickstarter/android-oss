package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.kickstarter.R;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.libs.utils.ObjectUtils.coalesce;

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

  private final KSCurrency ksCurrency;
  private final KSString ksString;
  private final @Nullable Delegate delegate;

  public interface Delegate {
    void projectStateChangedPositiveClicked(ProjectStateChangedPositiveViewHolder viewHolder, Activity activity);
  }

  public ProjectStateChangedPositiveViewHolder(final @NonNull View view, final @Nullable Delegate delegate) {
    super(view);
    this.delegate = delegate;
    this.ksCurrency = environment().ksCurrency();
    this.ksString = environment().ksString();
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind() {
    final Context context = context();

    final Project project = activity().project();
    if (project == null) {
      return;
    }
    final User user = activity().user();
    if (user == null) {
      return;
    }
    final Photo photo = project.photo();
    if (photo == null) {
      return;
    }

    switch (activity().category()) {
      case Activity.CATEGORY_LAUNCH:
        final DateTime launchedAt = coalesce(project.launchedAt(), new DateTime());
        this.cardView.setCardBackgroundColor(this.blueDarken10Color);
        this.leftStatFirstTextView.setText(this.ksCurrency.format(project.goal(), project));
        this.leftStatSecondTextView.setText(this.goalString);
        this.rightStatFirstTextView.setText(this.launchedString);
        this.rightStatSecondTextView.setText(DateTimeUtils.mediumDate(launchedAt));
        this.titleTextView.setText(this.ksString.format(
          this.creatorLaunchedProjectString,
          "creator_name",
          user.name(),
          "project_name",
          project.name()
        ));
        break;
      case Activity.CATEGORY_SUCCESS:
        this.cardView.setCardBackgroundColor(this.greenDarken10Color);
        this.leftStatFirstTextView.setText(this.ksCurrency.format(project.pledged(), project));
        this.leftStatSecondTextView.setText(this.ksString.format(
          this.pledgedOfGoalString,
          "goal",
          this.ksCurrency.format(project.goal(), project)
        ));
        this.rightStatFirstTextView.setText(this.fundedString);
        this.rightStatSecondTextView.setText(DateTimeUtils.mediumDate(activity().createdAt()));
        this.titleTextView.setText(this.ksString.format(
          this.projectSuccessfullyFundedString,
          "project_name",
          project.name()
        ));
        break;
      default:
        this.cardView.setCardBackgroundColor(this.greenDarken10Color);
        this.leftStatFirstTextView.setText("");
        this.leftStatSecondTextView.setText("");
        this.rightStatFirstTextView.setText("");
        this.rightStatSecondTextView.setText("");
        this.titleTextView.setText("");
    }
    // TODO: Switch to "You launched a project" if current user launched
    //return context.getString(R.string.creator_launched_a_project, activity.user().name(), activity.project().name());

    Picasso.get()
      .load(photo.full())
      .into(this.projectPhotoImageView);
  }

  @OnClick(R.id.card_view)
  public void onClick() {
    if (this.delegate != null) {
      this.delegate.projectStateChangedPositiveClicked(this, activity());
    }
  }
}
