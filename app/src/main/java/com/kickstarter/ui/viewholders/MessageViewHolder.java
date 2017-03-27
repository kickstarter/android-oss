package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

public final class MessageViewHolder extends KSViewHolder {
  private final MessageHolderViewModel.ViewModel viewModel;

  protected @Bind(R.id.message_body_text_view) TextView messageBodyTextView;
  protected @Bind(R.id.sender_avatar_image_view) ImageView creatorAvatarImageView;

  public MessageViewHolder(final @NonNull View view) {
    super(view);

    this.viewModel = new MessageHolderViewModel.ViewModel(environment());

    ButterKnife.bind(this, view);

    this.viewModel.outputs.creatorAvatarImageHidden()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.creatorAvatarImageView));

    this.viewModel.outputs.creatorAvatarImageUrl()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setCreatorAvatarImageView);

    this.viewModel.outputs.messageBodyTextViewAlignParentEnd()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::alignMessageBodyTextView);

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
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Message message = ObjectUtils.requireNonNull((Message) data);
    this.viewModel.inputs.configureWith(message);
  }

  private void alignMessageBodyTextView(final boolean alignParentEnd) {
    final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.messageBodyTextView.getLayoutParams();
    layoutParams.addRule(alignParentEnd ? RelativeLayout.ALIGN_PARENT_END : RelativeLayout.ALIGN_PARENT_START);
    layoutParams.addRule(alignParentEnd ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_PARENT_LEFT);
    this.messageBodyTextView.setLayoutParams(layoutParams);
  }

  private void setCreatorAvatarImageView(final @NonNull String avatarUrl) {
    Picasso.with(this.context()).load(avatarUrl)
      .transform(new CircleTransformation())
      .into(this.creatorAvatarImageView);
  }
}
