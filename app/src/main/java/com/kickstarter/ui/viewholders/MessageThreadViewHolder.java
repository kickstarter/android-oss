package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.MessageThread;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.MessagesActivity;
import com.kickstarter.viewmodels.MessageThreadHolderViewModel;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class MessageThreadViewHolder extends KSViewHolder {
  private final MessageThreadHolderViewModel.ViewModel viewModel;

  protected @Bind(R.id.date_text_view) TextView dateTextView;
  protected @Bind(R.id.message_body_text_view) TextView messageBodyTextView;
  protected @Bind(R.id.message_thread_card_view) CardView messageThreadCardView;
  protected @Bind(R.id.message_thread_unread_count_text_view) TextView unreadCountTextView;
  protected @Bind(R.id.participant_avatar_image_view) ImageView participantAvatarImageView;
  protected @Bind(R.id.participant_name_text_view) TextView participantNameTextView;
  protected @Bind(R.id.unread_indicator_image_view) ImageView unreadIndicatorImageView;

  @BindDimen(R.dimen.card_elevation) int cardElevationDimen;
  @BindDimen(R.dimen.card_no_elevation) int cardNoElevationDimen;

  @BindDrawable(R.drawable.click_indicator_light) Drawable clickIndicatorLightDrawable;
  @BindDrawable(R.drawable.message_thread_click_indicator) Drawable messageThreadClickIndicator;

  @BindString(R.string.font_family_sans_serif_medium) String fontFamilyMediumString;

  private KSString ksString;

  public MessageThreadViewHolder(final @NonNull View view) {
    super(view);

    this.ksString = environment().ksString();
    this.viewModel = new MessageThreadHolderViewModel.ViewModel(environment());

    ButterKnife.bind(this, view);

    RxView.clicks(this.messageThreadCardView)
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.viewModel.inputs.messageThreadCardViewClicked());

    this.viewModel.outputs.cardViewIsElevated()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setCardViewElevation);

    this.viewModel.outputs.dateDateTime()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setDateTextView);

    this.viewModel.outputs.dateTextViewIsMediumWeight()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setDateTextViewFontFamily);

    this.viewModel.outputs.messageBodyTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.messageBodyTextView::setText);

    this.viewModel.outputs.participantAvatarUrl()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setParticipantAvatarImageView);

    this.viewModel.outputs.participantNameTextViewIsMediumWeight()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setParticipantNameTextViewFontFamily);

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

    this.viewModel.outputs.unreadIndicatorViewHidden()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.unreadIndicatorImageView));
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final MessageThread messageThread = requireNonNull((MessageThread) data);
    this.viewModel.inputs.configureWith(messageThread);
  }

  private void setCardViewElevation(final boolean isElevated) {
    if (isElevated) {
      this.messageThreadCardView.setCardElevation(this.cardElevationDimen);
      this.messageThreadCardView.setForeground(this.clickIndicatorLightDrawable);
    } else {
      this.messageThreadCardView.setCardElevation(this.cardNoElevationDimen);
      this.messageThreadCardView.setForeground(this.messageThreadClickIndicator);
    }
  }

  private void setDateTextView(final @NonNull DateTime date) {
    this.dateTextView.setText(DateTimeUtils.relative(context(), ksString, date));
  }

  private void setDateTextViewFontFamily(final boolean isMediumWeight) {
    if (isMediumWeight) {
      this.dateTextView.setTypeface(Typeface.create(this.fontFamilyMediumString, Typeface.NORMAL));
    } else {
      this.dateTextView.setTypeface(Typeface.DEFAULT);
    }
  }

  private void startMessagesActivity(final @NonNull MessageThread messageThread) {
    final Context context = context();
    final Intent intent = new Intent(context, MessagesActivity.class)
      .putExtra(IntentKey.MESSAGE_THREAD, messageThread);

    context.startActivity(intent);
  }

  private void setParticipantAvatarImageView(final @NonNull String avatarUrl) {
    Picasso.with(context()).load(avatarUrl)
      .transform(new CircleTransformation())
      .into(this.participantAvatarImageView);
  }

  private void setParticipantNameTextViewFontFamily(final boolean isMediumWeight) {
    if (isMediumWeight) {
      this.participantNameTextView.setTypeface(Typeface.create(this.fontFamilyMediumString, Typeface.NORMAL));
    } else {
      this.participantNameTextView.setTypeface(Typeface.DEFAULT);
    }
  }

  private void setUnreadCountTextView(final @NonNull String unreadCount) {
    this.unreadCountTextView.setText("(" + unreadCount + ")");
  }
}
