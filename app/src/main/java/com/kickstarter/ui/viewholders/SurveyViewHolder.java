package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.models.Project;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.viewmodels.SurveyHolderViewModel;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class SurveyViewHolder extends KSViewHolder {
  private final @Nullable Delegate delegate;
  private final KSString ksString;
  private SurveyResponse survey;
  private final SurveyHolderViewModel.ViewModel viewModel;

  @Bind(R.id.survey_avatar_image) ImageView creatorAvatarImageView;
  @Bind(R.id.survey_text) TextView surveyTextView;
  @Bind(R.id.survey_title) TextView surveyTitleTextView;

  @BindString(R.string.Creator_name_needs_some_information_to_deliver_your_reward_for_project_name) String surveyDescriptionString;

  public interface Delegate {
    void surveyClicked(SurveyViewHolder viewHolder, SurveyResponse surveyResponse);
  }

  public SurveyViewHolder(final @NonNull View view, final @Nullable Delegate delegate) {
    super(view);
    ButterKnife.bind(this, view);

    this.delegate = delegate;
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

    this.viewModel.outputs.startSurveyWebViewActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startSurveyWebViewActivity);
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
    final SurveyResponse configData = requireNonNull(
      (SurveyResponse) data
    );

    this.survey = configData;
    this.viewModel.inputs.configureWith(configData);
  }

  @Override
  public void onClick(final @NonNull View view) {
    if (this.delegate != null) {
      this.delegate.surveyClicked(this, this.survey);
    }
  }

  private void setCreatorAvatarImage(final @NonNull String creatorAvatarImage) {
    Picasso.with(context())
      .load(creatorAvatarImage)
      .transform(new CircleTransformation())
      .into(this.creatorAvatarImageView);
  }

  private void startSurveyWebViewActivity(final @NonNull SurveyResponse surveyResponse) {

  }
}
