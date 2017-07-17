package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.ToolbarUtils;
import com.kickstarter.libs.utils.TransitionUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Project;
import com.kickstarter.ui.IntentKey;
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

  protected @Bind(R.id.messages_app_bar_layout) AppBarLayout appBarLayout;
  protected @Bind(R.id.messages_toolbar_back_button) IconButton backButton;
  protected @Bind(R.id.backing_amount_text_view) TextView backingAmountTextViewText;
  protected @Bind(R.id.backing_info_view) View backingInfoView;
  protected @Bind(R.id.messages_toolbar_close_button) IconButton closeButton;
  protected @Bind(R.id.messages_participant_name_text_view) TextView participantNameTextView;
  protected @Bind(R.id.message_edit_text) EditText messageEditText;
  protected @Bind(R.id.messages_project_name_text_view) TextView projectNameTextView;
  protected @Bind(R.id.messages_project_name_collapsed_text_view) TextView projectNameToolbarTextView;
  protected @Bind(R.id.messages_recycler_view) RecyclerView recyclerView;
  protected @Bind(R.id.send_message_button) Button sendMessageButton;
  protected @Bind(R.id.messages_view_pledge_button) Button viewPledgeButton;

  protected @BindString(R.string.project_creator_by_creator) String byCreatorString;
  protected @BindString(R.string.Message_user_name) String messageUserNameString;
  protected @BindString(R.string.pledge_amount_pledged_on_pledge_date) String pledgeAmountPledgedOnPledgeDateString;
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

    final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    layoutManager.setStackFromEnd(true);
    this.recyclerView.setLayoutManager(layoutManager);

    this.viewPledgeButton.setText(this.viewPledgeString);

    ToolbarUtils.INSTANCE.fadeToolbarTitleOnExpand(this.appBarLayout, this.projectNameToolbarTextView);

    RxView.focusChanges(this.messageEditText)
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.viewModel.inputs::messageEditTextIsFocused);

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

    this.viewModel.outputs.messageEditTextShouldRequestFocus()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.requestFocusAndOpenKeyboard());

    this.viewModel.outputs.messages()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.adapter::messages);

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

    this.viewModel.outputs.projectNameToolbarTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.projectNameToolbarTextView::setText);

    this.viewModel.outputs.scrollRecyclerViewToBottom()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.recyclerView.scrollToPosition(this.adapter.getItemCount() - 1));

    this.viewModel.outputs.setMessageEditText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.messageEditText::setText);

    this.viewModel.outputs.sendMessageButtonIsEnabled()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.sendMessageButton::setEnabled);

    this.viewModel.outputs.showMessageErrorToast()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(error -> ViewUtils.showToast(this, error));

    this.viewModel.outputs.startViewPledgeActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startViewPledgeActivity);

    this.viewModel.outputs.toolbarIsExpanded()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.appBarLayout::setExpanded);

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
  protected void sendMessageButtonClicked() {
    this.viewModel.inputs.sendMessageButtonClicked();
  }

  @OnClick(R.id.messages_view_pledge_button)
  protected void viewPledgeButtonClicked() {
    this.viewModel.inputs.viewPledgeButtonClicked();
  }

  @OnTextChanged(R.id.message_edit_text)
  public void onMessageEditTextChanged(final @NonNull CharSequence message) {
    this.viewModel.inputs.messageEditTextChanged(message.toString());
  }

  private void requestFocusAndOpenKeyboard() {
    this.messageEditText.requestFocus();
    this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
  }

  private void setBackingInfoView(final @NonNull Pair<Backing, Project> backingAndProject) {
    final String pledgeAmount = this.ksCurrency.format(backingAndProject.first.amount(), backingAndProject.second);
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
    this.messageEditText.setHint(this.ksString.format(this.messageUserNameString, "user_name", name));
  }

  private void startViewPledgeActivity(final @NonNull Project project) {
    final Intent intent = new Intent(this, ViewPledgeActivity.class)
      .putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.IS_FROM_MESSAGES_ACTIVITY, true);

    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }
}
