package com.kickstarter.ui.viewholders;

import android.content.Intent;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.models.Project;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.SurveyResponseActivity;
import com.kickstarter.viewmodels.SurveyHolderViewModel;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class SurveyViewHolder extends KSViewHolder {
  private final KSString ksString;
  private final SurveyHolderViewModel.ViewModel viewModel;

  @Bind(R.id.survey_avatar_image) ImageView creatorAvatarImageView;
  @Bind(R.id.survey_text) TextView surveyTextView;
  @Bind(R.id.survey_title) TextView surveyTitleTextView;

  @BindString(R.string.Creator_name_needs_some_information_to_deliver_your_reward_for_project_name) String surveyDescriptionString;

  public SurveyViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);

    this.viewModel = new SurveyHolderViewModel.ViewModel(environment());
    this.ksString = environment().ksString();

    this.viewModel.outputs.creatorAvatarImageUrl()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setCreatorAvatarImage);

    this.viewModel.outputs.creatorNameTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.surveyTitleTextView::setText);

    this.viewModel.outputs.projectForSurveyDescription()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setSurveyDescription);

    this.viewModel.outputs.startSurveyResponseActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startSurveyResponseActivity);
  }

  private void setSurveyDescription(final @NonNull Project projectForSurveyDescription) {
    this.surveyTextView.setText(
      Html.fromHtml(
        this.ksString.format(
          this.surveyDescriptionString,
          "creator_name", projectForSurveyDescription.creator().name(),
          "project_name", projectForSurveyDescription.name()
        )
      )
    );
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final SurveyResponse surveyResponse = requireNonNull((SurveyResponse) data);
    this.viewModel.inputs.configureWith(surveyResponse);
  }

  @Override
  public void onClick(final @NonNull View view) {
    this.viewModel.inputs.surveyClicked();
  }

  private void setCreatorAvatarImage(final @NonNull String creatorAvatarImage) {
    Picasso.get()
      .load(creatorAvatarImage)
      .transform(new CircleTransformation())
      .into(this.creatorAvatarImageView);
  }

  private void startSurveyResponseActivity(final @NonNull SurveyResponse surveyResponse) {
    final Intent intent = new Intent(context(), SurveyResponseActivity.class)
      .putExtra(IntentKey.SURVEY_RESPONSE, surveyResponse);
    context().startActivity(intent);
  }
}
