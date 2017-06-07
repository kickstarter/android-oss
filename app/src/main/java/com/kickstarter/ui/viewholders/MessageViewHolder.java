package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Message;
import com.kickstarter.viewmodels.MessageHolderViewModel;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

public final class MessageViewHolder extends KSViewHolder {
  private final MessageHolderViewModel.ViewModel viewModel;

  protected @Bind(R.id.message_body_card_view) CardView messageBodyCardView;
  protected @Bind(R.id.message_body_text_view) TextView messageBodyTextView;
  protected @Bind(R.id.sender_avatar_image_view) ImageView participantAvatarImageView;

  protected @BindDimen(R.dimen.grid_new_2) int gridNew2Dimen;
  protected @BindDimen(R.dimen.grid_new_3) int gridNew3Dimen;

  public MessageViewHolder(final @NonNull View view) {
    super(view);

    this.viewModel = new MessageHolderViewModel.ViewModel(environment());

    ButterKnife.bind(this, view);

    this.viewModel.outputs.participantAvatarImageHidden()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.participantAvatarImageView));

    this.viewModel.outputs.participantAvatarImageUrl()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setParticipantAvatarImageView);

    this.viewModel.outputs.messageBodyCardViewAlignParentEnd()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::alignMessageBodyCardView);

    this.viewModel.outputs.messageBodyTextViewBackgroundColorInt()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(colorInt ->
        this.messageBodyTextView.setBackgroundColor(ContextCompat.getColor(this.context(), colorInt))
      );

    this.viewModel.outputs.messageBodyTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.messageBodyTextView::setText);

    this.viewModel.outputs.messageBodyTextViewTextColorInt()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(colorInt ->
        this.messageBodyTextView.setTextColor(ContextCompat.getColor(this.context(), colorInt))
      );

    this.viewModel.outputs.shouldSetMessageBodyTextViewPaddingForSender()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setMessageBodyTextViewPadding);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Message message = ObjectUtils.requireNonNull((Message) data);
    this.viewModel.inputs.configureWith(message);
  }

  private void alignMessageBodyCardView(final boolean alignParentEnd) {
    final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.messageBodyCardView.getLayoutParams();
    layoutParams.addRule(alignParentEnd ? RelativeLayout.ALIGN_PARENT_END : RelativeLayout.ALIGN_PARENT_START);
    layoutParams.addRule(alignParentEnd ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_PARENT_LEFT);
    this.messageBodyCardView.setLayoutParams(layoutParams);
  }

  private void setMessageBodyTextViewPadding(final boolean isSender) {
    if (isSender) {
      this.messageBodyTextView.setPadding(gridNew2Dimen, gridNew2Dimen, gridNew2Dimen, gridNew2Dimen);
    } else {
      this.messageBodyTextView.setPadding(gridNew3Dimen, gridNew3Dimen, gridNew3Dimen, gridNew2Dimen);
    }
  }

  private void setParticipantAvatarImageView(final @NonNull String avatarUrl) {
    Picasso.with(this.context()).load(avatarUrl)
      .transform(new CircleTransformation())
      .into(this.participantAvatarImageView);
  }
}
