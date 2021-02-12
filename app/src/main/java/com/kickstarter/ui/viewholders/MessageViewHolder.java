package com.kickstarter.ui.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.kickstarter.R;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Message;
import com.kickstarter.viewmodels.MessageHolderViewModel;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

public final class MessageViewHolder extends KSViewHolder {
  private final MessageHolderViewModel.ViewModel viewModel;

  protected @Bind(R.id.message_delivery_status_text_view) TextView deliveryStatusTextView;
  protected @Bind(R.id.message_body_recipient_card_view) CardView messageBodyRecipientCardView;
  protected @Bind(R.id.message_body_recipient_text_view) TextView messageBodyRecipientTextView;
  protected @Bind(R.id.message_body_sender_card_view) CardView messageBodySenderCardView;
  protected @Bind(R.id.message_body_sender_text_view) TextView messageBodySenderTextView;
  protected @Bind(R.id.message_sender_avatar_image_view) ImageView participantAvatarImageView;

  public MessageViewHolder(final @NonNull View view) {
    super(view);
    this.viewModel = new MessageHolderViewModel.ViewModel(environment());
    ButterKnife.bind(this, view);

    this.viewModel.outputs.deliveryStatusTextViewIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.deliveryStatusTextView));

    this.viewModel.outputs.messageBodyRecipientCardViewIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.messageBodyRecipientCardView));

    this.viewModel.outputs.messageBodyRecipientTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.messageBodyRecipientTextView::setText);

    this.viewModel.outputs.messageBodySenderCardViewIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.messageBodySenderCardView));

    this.viewModel.outputs.messageBodySenderTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.messageBodySenderTextView::setText);

    this.viewModel.outputs.participantAvatarImageHidden()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.participantAvatarImageView));

    this.viewModel.outputs.participantAvatarImageUrl()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setParticipantAvatarImageView);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Message message = ObjectUtils.requireNonNull((Message) data);
    this.viewModel.inputs.configureWith(message);
  }

  public void isLastPosition(final boolean isLastPosition) {
    this.viewModel.inputs.isLastPosition(isLastPosition);
  }

  private void setParticipantAvatarImageView(final @NonNull String avatarUrl) {
    Picasso.get().load(avatarUrl)
      .transform(new CircleTransformation())
      .into(this.participantAvatarImageView);
  }
}
