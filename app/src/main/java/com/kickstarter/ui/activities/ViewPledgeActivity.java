package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Project;
import com.kickstarter.viewmodels.ViewPledgeViewModel;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;

@RequiresViewModel(ViewPledgeViewModel.class)
public final class ViewPledgeActivity extends BaseActivity<ViewPledgeViewModel> {
  protected @Bind(R.id.avatar) ImageView avatarImageView;
  protected @Bind(R.id.context_photo) ImageView contextPhotoImageView;
  protected @Bind(R.id.creator_name) TextView creatorNameTextView;
  protected @Bind(R.id.name) TextView nameTextView;
  protected @Bind(R.id.sequence) TextView sequenceTextView;
  protected @Bind(R.id.pledge_info) TextView pledgeInfoTextView;
  protected @Bind(R.id.pledge_status) TextView pledgeStatusTextView;
  protected @Bind(R.id.project_name) TextView projectNameTextView;
  protected @Bind(R.id.reward_info) TextView rewardInfoTextView;
  protected @Bind(R.id.shipping_info) TextView shippingInfoTextView;
  protected @Bind(R.id.shipping_amount) TextView shippingAmountTextView;

  protected @BindString(R.string.backer_modal_backer_number) String backerNumberString;
  protected @BindString(R.string.backer_modal_status_backing_status) String backerStatusString;
  protected @BindString(R.string.backer_modal_pledge_amount_on_pledge_date) String pledgeAmountPledgeDateString;
  protected @BindString(R.string.backer_modal_reward_amount_reward_description) String rewardAmountRewardDescriptionString;
  protected @BindString(R.string.project_creator_by_creator) String projectCreatorByCreatorString;

  @Inject KSCurrency ksCurrency;
  @Inject KSString ksString;

  @Override
  public void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.view_pledge_layout);
    ButterKnife.bind(this);
    ((KSApplication) getApplication()).component().inject(this);

    viewModel.outputs.backing()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::show);
  }

  public void show(final @NonNull Backing backing) {
    final Project project = backing.project();

    Picasso.with(this).load(project.photo().full()).into(contextPhotoImageView);
    projectNameTextView.setText(project.name());
    creatorNameTextView.setText(ksString.format(
      projectCreatorByCreatorString,
      "creator_name",
      project.creator().name()
    ));

    Picasso.with(this).load(backing.backer().avatar().medium())
      .transform(new CircleTransformation())
      .into(avatarImageView);
    nameTextView.setText(backing.backer().name());
    sequenceTextView.setText(ksString.format(
      backerNumberString,
      "backer_number",
      NumberUtils.format(backing.sequence())
    ));
    pledgeStatusTextView.setText(ksString.format(
      backerStatusString,
      "backing_status",
      backing.status()
    ));

    if (project != null && backing.reward() != null) {
      pledgeInfoTextView.setText(ksString.format(
        pledgeAmountPledgeDateString,
        "pledge_amount",
        ksCurrency.format(backing.amount(), project),
        "pledge_date",
        DateTimeUtils.fullDate(backing.pledgedAt())
      ));
      rewardInfoTextView.setText(ksString.format(
        rewardAmountRewardDescriptionString,
        "reward_amount",
        ksCurrency.format(backing.reward().minimum(), project),
        "reward_description",
        backing.reward().reward()
      ));

      if (backing.reward().shippingEnabled() != null && backing.reward().shippingEnabled()) {
        shippingInfoTextView.setText(backing.location().displayableName());
        shippingAmountTextView.setText(
          ksCurrency.format(backing.shippingAmount(), project)
        );
      }
    }
  }

  @OnClick(R.id.project_context_view)
  public void projectContextClicked() {
    onBackPressed();
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }
}
