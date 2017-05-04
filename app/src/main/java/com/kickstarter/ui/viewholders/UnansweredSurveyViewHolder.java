package com.kickstarter.ui.viewholders;

import android.support.annotation.Nullable;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.viewmodels.UnansweredSurveyHolderViewModel;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

public class UnansweredSurveyViewHolder extends KSViewHolder {

  private final KSString ksString;
  @Bind(R.id.survey_avatar_image) ImageView creatorAvatarImageView;
  @Bind(R.id.survey_title) TextView surveyTitleTextView;
  @Bind(R.id.survey_text) TextView surveyTextView;

  @BindString(R.string.Creator_name_needs_some_information_to_deliver_your_reward_for_project_name) String surveyDescriptionString;

  private final UnansweredSurveyHolderViewModel.ViewModel viewModel;
  public UnansweredSurveyViewHolder(final View view) {
    super(view);

    this.viewModel = new UnansweredSurveyHolderViewModel.ViewModel(environment());
    this.ksString = environment().ksString();

    ButterKnife.bind(this, view);

    this.viewModel.outputs.creatorAvatarImage()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setCreatorAvatarImage);

    this.viewModel.outputs.creatorName()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.surveyTitleTextView::setText);

    this.viewModel.outputs.surveyDescription()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(sds -> setSurveyDescription(sds));

  }
  private void setSurveyDescription(List<String> surveyDescriptionStrings) {
    this.surveyTextView.setText(Html.fromHtml(
      ksString.format(
        surveyDescriptionString,
        "creator_name", surveyDescriptionStrings.get(0),
        "project_name", surveyDescriptionStrings.get(1)
      )));
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final SurveyResponse configData = ObjectUtils.requireNonNull(
      (SurveyResponse) data
    );
    this.viewModel.inputs.configureWith(configData);
  }

  public void setCreatorAvatarImage(String creatorAvatarImage) {
    Picasso.with(context()).load(creatorAvatarImage).into(this.creatorAvatarImageView);
  }
}
