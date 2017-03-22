package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.models.MessageThread;
import com.kickstarter.viewmodels.MessageThreadViewModel;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class MessageThreadViewHolder extends KSViewHolder {
  private final MessageThreadViewModel.ViewModel viewModel;

  public @Bind(R.id.date_text_view) TextView dateTextView;
  public @Bind(R.id.message_body_text_view) TextView messageBodyTextView;
  public @Bind(R.id.participant_avatar_image_view) ImageView participantAvatarImageView;
  public @Bind(R.id.participant_name_text_view) TextView participantNameTextView;

  private KSString ksString;

  public MessageThreadViewHolder(final @NonNull View view) {
    super(view);

    this.ksString = environment().ksString();
    this.viewModel = new MessageThreadViewModel.ViewModel(environment());

    ButterKnife.bind(this, view);

    this.viewModel.outputs.dateDateTime()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setDateTextView);

    this.viewModel.outputs.messageBodyTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.messageBodyTextView::setText);

    this.viewModel.outputs.participantAvatarUrl()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setParticipantAvatarImageView);

    this.viewModel.outputs.participantNameTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.participantNameTextView::setText);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final MessageThread messageThread = requireNonNull((MessageThread) data);
    this.viewModel.inputs.configureWith(messageThread);

    this.participantNameTextView.setText(messageThread.participant().name());
  }

  private void setDateTextView(final @NonNull DateTime date) {
    this.dateTextView.setText(DateTimeUtils.relative(context(), ksString, date));
  }

  private void setParticipantAvatarImageView(final @NonNull String avatarUrl) {
    Picasso.with(context()).load(avatarUrl)
      .transform(new CircleTransformation())
      .into(this.participantAvatarImageView);
  }
}
