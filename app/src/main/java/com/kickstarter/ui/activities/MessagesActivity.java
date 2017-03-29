package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Project;
import com.kickstarter.ui.adapters.MessagesAdapter;
import com.kickstarter.viewmodels.MessagesViewModel;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresActivityViewModel(MessagesViewModel.ViewModel.class)
public final class MessagesActivity extends BaseActivity<MessagesViewModel.ViewModel> {
  private KSCurrency ksCurrency;
  private KSString ksString;
  private MessagesAdapter adapter;

  protected @Bind(R.id.backing_amount_text_view) TextView backingAmountTextViewText;
  protected @Bind(R.id.backing_info_view) View backingInfoView;
  protected @Bind(R.id.messages_creator_name_text_view) TextView creatorNameTextView;
  protected @Bind(R.id.messages_project_name_text_view) TextView projectNameTextView;
  protected @Bind(R.id.messages_recycler_view) RecyclerView recyclerView;
  protected @Bind(R.id.view_pledge_button) Button viewPledgeButton;

  protected @BindString(R.string.project_view_button) String viewPledgeString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.messages_layout);
    ButterKnife.bind(this);

    this.ksCurrency = this.environment().ksCurrency();
    this.ksString = this.environment().ksString();

    this.adapter = new MessagesAdapter();
    this.recyclerView.setAdapter(this.adapter);
    this.recyclerView.setLayoutManager(new LinearLayoutManager(this));

    this.viewPledgeButton.setText(viewPledgeString);

    this.viewModel.outputs.backingAndProject()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setBackingInfoView);

    this.viewModel.outputs.backingInfoViewHidden()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.backingInfoView));

    this.viewModel.outputs.creatorNameTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.creatorNameTextView::setText);

    this.viewModel.outputs.messages()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.adapter::messages);

    this.viewModel.outputs.projectNameTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.projectNameTextView::setText);
  }

  @Override
  protected @Nullable Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.recyclerView.setAdapter(null);
  }

  private void setBackingInfoView(final @NonNull Pair<Backing, Project> backingAndProject) {
    final String pledgeAmount = ksCurrency.format(backingAndProject.first.amount(), backingAndProject.second);
    final String dateTime = DateTimeUtils.relative(this, this.ksString, backingAndProject.first.pledgedAt());
    this.backingAmountTextViewText.setText(pledgeAmount + " pledged on " + dateTime); // todo: localize
  }
}
