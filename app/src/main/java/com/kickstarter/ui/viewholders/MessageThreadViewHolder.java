package com.kickstarter.ui.viewholders;

import android.content.Intent;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.jakewharton.rxbinding.view.RxView;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.KoalaContext;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.libs.utils.extensions.StringExt;
import com.kickstarter.models.MessageThread;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.MessagesActivity;
import com.kickstarter.viewmodels.MessageThreadHolderViewModel;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class MessageThreadViewHolder extends KSViewHolder {
  private final MessageThreadHolderViewModel.ViewModel viewModel;

  protected @Bind(R.id.message_thread_date_text_view) TextView dateTextView;
  protected @Bind(R.id.message_thread_body_text_view) TextView messageBodyTextView;
  protected @Bind(R.id.message_thread_container) ConstraintLayout messageThreadContainer;
  protected @Bind(R.id.participant_avatar_image_view) ImageView participantAvatarImageView;
  protected @Bind(R.id.participant_name_text_view) TextView participantNameTextView;
  protected @Bind(R.id.message_thread_unread_count_text_view) TextView unreadCountTextView;

  protected @BindString(R.string.unread_count_unread) String unreadCountUnreadString;

  private final KSString ksString;

  public MessageThreadViewHolder(final @NonNull View view) {
    super(view);

    this.ksString = environment().ksString();
    this.viewModel = new MessageThreadHolderViewModel.ViewModel(environment());

    ButterKnife.bind(this, view);

    RxView.clicks(this.messageThreadContainer)
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.viewModel.inputs.messageThreadCardViewClicked());

    this.viewModel.outputs.dateDateTime()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setDateTextView);

    this.viewModel.outputs.dateTextViewIsBold()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(bold -> setTypeface(this.dateTextView, bold));

    this.viewModel.outputs.messageBodyTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.messageBodyTextView::setText);

    this.viewModel.outputs.messageBodyTextIsBold()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(bold -> setTypeface(this.messageBodyTextView, bold));

    this.viewModel.outputs.participantAvatarUrl()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setParticipantAvatarImageView);

    this.viewModel.outputs.participantNameTextViewIsBold()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(bold -> setTypeface(this.participantNameTextView, bold));

    this.viewModel.outputs.participantNameTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.participantNameTextView::setText);

    this.viewModel.outputs.startMessagesActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startMessagesActivity);

    this.viewModel.outputs.unreadCountTextViewIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.unreadCountTextView));

    this.viewModel.outputs.unreadCountTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setUnreadCountTextView);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final MessageThread messageThread = requireNonNull((MessageThread) data);
    this.viewModel.inputs.configureWith(messageThread);
  }

  private void setDateTextView(final @NonNull DateTime date) {
    this.dateTextView.setText(DateTimeUtils.relative(context(), this.ksString, date));
  }

  private void setTypeface(final @NonNull TextView textView, final boolean bold) {
    final int style = bold ? Typeface.BOLD : Typeface.NORMAL;
    textView.setTypeface(Typeface.create(textView.getTypeface(), style));
  }

  private void startMessagesActivity(final @NonNull MessageThread messageThread) {
    final Intent intent = new Intent(context(), MessagesActivity.class)
      .putExtra(IntentKey.MESSAGE_THREAD, messageThread)
      .putExtra(IntentKey.KOALA_CONTEXT, KoalaContext.Message.MESSAGES);

    context().startActivity(intent);
  }

  private void setParticipantAvatarImageView(final @NonNull String avatarUrl) {
    Picasso.get().load(avatarUrl)
      .transform(new CircleTransformation())
      .into(this.participantAvatarImageView);
  }

  private void setUnreadCountTextView(final @NonNull String unreadCount) {
    this.unreadCountTextView.setText(StringExt.wrapInParentheses(unreadCount));

    this.unreadCountTextView.setContentDescription(
      this.ksString.format(this.unreadCountUnreadString, "unread_count", unreadCount)
    );
  }
}
