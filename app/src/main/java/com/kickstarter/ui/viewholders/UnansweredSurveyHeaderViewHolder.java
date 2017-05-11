package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.utils.ObjectUtils;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class UnansweredSurveyHeaderViewHolder extends KSViewHolder {

  private final KSString ksString;

  @Bind(R.id.heading) TextView headingTextView;
  @BindString(R.string.token_reward_surveys) String rewardSurveysToken;
  @BindString(R.string.token_reward_survey_count) String rewardSurveyCountToken;

  public UnansweredSurveyHeaderViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);

    ksString = environment().ksString();
  }
  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final int unansweredSurveyCount = ObjectUtils.requireNonNull(
      (int) data
    );
    if (unansweredSurveyCount > 0) {
      headingTextView.setText(
        ksString.format(
          rewardSurveysToken,
          unansweredSurveyCount,
          rewardSurveyCountToken,
          String.valueOf(unansweredSurveyCount)));
    }
  }
}
