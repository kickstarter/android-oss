package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.TransitionUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Project;
import com.kickstarter.ui.adapters.MessagesAdapter;
import com.kickstarter.ui.views.IconButton;
import com.kickstarter.viewmodels.MessagesViewModel;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

@RequiresActivityViewModel(MessagesViewModel.ViewModel.class)
public final class MessagesActivity extends BaseActivity<MessagesViewModel.ViewModel> {
  private KSCurrency ksCurrency;
  private KSString ksString;
  private MessagesAdapter adapter;

  protected @Bind(R.id.messages_toolbar_back_button) IconButton backButton;
  protected @Bind(R.id.backing_amount_text_view) TextView backingAmountTextViewText;
  protected @Bind(R.id.backing_info_view) View backingInfoView;
  protected @Bind(R.id.messages_toolbar_close_button) IconButton closeButton;
  protected @Bind(R.id.messages_participant_name_text_view) TextView participantNameTextView;
  protected @Bind(R.id.message_edit_text) EditText messageEditText;
  protected @Bind(R.id.messages_project_name_text_view) TextView projectNameTextView;
  protected @Bind(R.id.messages_recycler_view) RecyclerView recyclerView;
  protected @Bind(R.id.view_pledge_button) Button viewPledgeButton;

  protected @BindString(R.string.project_creator_by_creator) String byCreatorString;
  protected @BindString(R.string.pledge_amount_pledged_on_pledge_date) String pledgeAmountPledgedOnPledgeDateString;
  protected @BindString(R.string.project_view_button) String viewPledgeString;
  protected @BindString(R.string.Reply_to_user_name) String replyToUserNameString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.messages_layout);
    ButterKnife.bind(this);

    this.ksCurrency = this.environment().ksCurrency();
    this.ksString = this.environment().ksString();

    this.adapter = new MessagesAdapter();
    this.recyclerView.setAdapter(this.adapter);

    final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    layoutManager.setStackFromEnd(true);
    this.recyclerView.setLayoutManager(layoutManager);

    this.viewPledgeButton.setText(viewPledgeString);

    this.viewModel.outputs.backButtonIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.backButton));

    this.viewModel.outputs.backingAndProject()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setBackingInfoView);

    this.viewModel.outputs.backingInfoViewIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.backingInfoView));

    this.viewModel.outputs.closeButtonIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.closeButton));

    this.viewModel.outputs.goBack()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> back());

    this.viewModel.outputs.messageEditTextHint()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setMessageEditTextHint);

    this.viewModel.outputs.messages()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(m -> {
        this.adapter.messages(m);
        this.recyclerView.invalidate();
      });

    this.viewModel.outputs.participantNameTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(name ->
        this.participantNameTextView.setText(this.ksString.format(this.byCreatorString, "creator_name", name))
      );

    this.viewModel.outputs.projectNameTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.projectNameTextView::setText);

    this.viewModel.outputs.setMessageEditText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.messageEditText::setText);

    this.viewModel.outputs.showMessageErrorToast()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(error -> ViewUtils.showToast(this, error));

    this.viewModel.outputs.viewPledgeButtonIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.viewPledgeButton));
  }

  @Override
  protected @Nullable Pair<Integer, Integer> exitTransition() {
    return this.backButton.getVisibility() == View.VISIBLE ? TransitionUtils.slideInFromLeft() : null;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.recyclerView.setAdapter(null);
  }

  @OnClick({R.id.messages_toolbar_back_button, R.id.messages_toolbar_close_button})
  protected void backOrCloseButtonClicked() {
    this.viewModel.inputs.backOrCloseButtonClicked();
  }

  @OnClick(R.id.send_message_button)
  public void sendMessageButtonClicked() {
    this.viewModel.inputs.sendMessageButtonClicked();
  }

  @OnTextChanged(R.id.message_edit_text)
  public void onMessageEditTextChanged(final @NonNull CharSequence message) {
    this.viewModel.inputs.messageEditTextChanged(message.toString());
  }

  private void setBackingInfoView(final @NonNull Pair<Backing, Project> backingAndProject) {
    final String pledgeAmount = ksCurrency.format(backingAndProject.first.amount(), backingAndProject.second);
    final String pledgeDate = DateTimeUtils.relative(this, this.ksString, backingAndProject.first.pledgedAt());

    this.backingAmountTextViewText.setText(
      Html.fromHtml(
        this.ksString.format(
          this.pledgeAmountPledgedOnPledgeDateString, "pledge_amount", pledgeAmount, "pledge_date", pledgeDate
        )
      )
    );
  }

  private void setMessageEditTextHint(final @NonNull String name) {
    this.messageEditText.setHint(this.ksString.format(this.replyToUserNameString, "user_name", name));
  }
}
