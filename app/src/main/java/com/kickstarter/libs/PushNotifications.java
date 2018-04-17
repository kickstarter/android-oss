package com.kickstarter.libs;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Pair;

import com.kickstarter.R;
import com.kickstarter.libs.qualifiers.ApplicationContext;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.transformations.CropSquareTransformation;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.MessageThread;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.models.Update;
import com.kickstarter.models.pushdata.Activity;
import com.kickstarter.models.pushdata.GCM;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.MessageThreadEnvelope;
import com.kickstarter.services.apiresponses.PushNotificationEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.ActivityFeedActivity;
import com.kickstarter.ui.activities.MessagesActivity;
import com.kickstarter.ui.activities.ProjectActivity;
import com.kickstarter.ui.activities.SurveyResponseActivity;
import com.kickstarter.ui.activities.UpdateActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.IOException;

import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;

public final class PushNotifications {
  private final @ApplicationContext Context context;
  private final ApiClientType client;
  private final DeviceRegistrarType deviceRegistrar;

  private final PublishSubject<PushNotificationEnvelope> notifications = PublishSubject.create();
  private final CompositeSubscription subscriptions = new CompositeSubscription();

  public PushNotifications(final @ApplicationContext @NonNull Context context, final @NonNull ApiClientType client,
    final @NonNull DeviceRegistrarType deviceRegistrar) {

    this.context = context;
    this.client = client;
    this.deviceRegistrar = deviceRegistrar;
  }

  public void initialize() {
    this.subscriptions.add(
      this.notifications
        .onBackpressureBuffer()
        .filter(PushNotificationEnvelope::isFriendFollow)
        .observeOn(Schedulers.newThread())
        .subscribe(this::displayNotificationFromFriendFollowActivity)
    );

    this.subscriptions.add(
      this.notifications
        .onBackpressureBuffer()
        .filter(PushNotificationEnvelope::isMessage)
        .flatMap(this::fetchMessageThreadWithEnvelope)
        .filter(ObjectUtils::isNotNull)
        .observeOn(Schedulers.newThread())
        .subscribe(envelopeAndMessageThread ->
          this.displayNotificationFromMessageActivity(envelopeAndMessageThread.first, envelopeAndMessageThread.second)
        )
    );

    this.subscriptions.add(
      this.notifications
        .onBackpressureBuffer()
        .filter(PushNotificationEnvelope::isProjectActivity)
        .observeOn(Schedulers.newThread())
        .subscribe(this::displayNotificationFromProjectActivity)
    );

    this.subscriptions.add(
      this.notifications
        .onBackpressureBuffer()
        .filter(PushNotificationEnvelope::isProjectReminder)
        .observeOn(Schedulers.newThread())
        .subscribe(this::displayNotificationFromProjectReminder)
    );

    this.subscriptions.add(
      this.notifications
        .onBackpressureBuffer()
        .filter(PushNotificationEnvelope::isProjectUpdateActivity)
        .flatMap(this::fetchUpdateWithEnvelope)
        .filter(ObjectUtils::isNotNull)
        .observeOn(Schedulers.newThread())
        .subscribe(envelopeAndUpdate ->
          this.displayNotificationFromUpdateActivity(envelopeAndUpdate.first, envelopeAndUpdate.second)
        )
    );

    this.subscriptions.add(
      this.notifications
        .onBackpressureBuffer()
        .filter(PushNotificationEnvelope::isSurvey)
        .flatMap(this::fetchSurveyResponseWithEnvelope)
        .filter(ObjectUtils::isNotNull)
        .observeOn(Schedulers.newThread())
        .subscribe(envelopeAndSurveyResponse ->
          this.displayNotificationFromSurveyResponseActivity(
            envelopeAndSurveyResponse.first,
            envelopeAndSurveyResponse.second
          )
        )
    );

    this.deviceRegistrar.registerDevice();
  }

  public void add(final @NonNull PushNotificationEnvelope envelope) {
    this.notifications.onNext(envelope);
  }

  private void displayNotificationFromFriendFollowActivity(final @NonNull PushNotificationEnvelope envelope) {
    final GCM gcm = envelope.gcm();

    final Activity activity = envelope.activity();
    if (activity == null) {
      return;
    }

    final Notification notification = notificationBuilder(gcm.title(), gcm.alert())
      .setLargeIcon(fetchBitmap(activity.userPhoto(), true))
      .build();
    notificationManager().notify(envelope.signature(), notification);
  }

  private void displayNotificationFromMessageActivity(final @NonNull PushNotificationEnvelope envelope,
    final @NonNull MessageThread messageThread) {
    final GCM gcm = envelope.gcm();

    final PushNotificationEnvelope.Message message = envelope.message();
    if (message == null) {
      return;
    }

    final Notification notification = notificationBuilder(gcm.title(), gcm.alert())
      .setContentIntent(messageThreadIntent(envelope, messageThread))
      .build();

    notificationManager().notify(envelope.signature(), notification);
  }

  private void displayNotificationFromProjectActivity(final @NonNull PushNotificationEnvelope envelope) {
    final GCM gcm = envelope.gcm();

    final Activity activity = envelope.activity();
    if (activity == null) {
      return;
    }
    final Long projectId = activity.projectId();
    if (projectId == null) {
      return;
    }
    final String projectPhoto = activity.projectPhoto();

    final String projectParam = ObjectUtils.toString(projectId);

    NotificationCompat.Builder notificationBuilder = notificationBuilder(gcm.title(), gcm.alert())
      .setContentIntent(projectContentIntent(envelope, projectParam));
    if (projectPhoto != null) {
      notificationBuilder = notificationBuilder.setLargeIcon(fetchBitmap(projectPhoto, false));
    }
    final Notification notification = notificationBuilder.build();

    notificationManager().notify(envelope.signature(), notification);
  }

  private void displayNotificationFromProjectReminder(final @NonNull PushNotificationEnvelope envelope) {
    final GCM gcm = envelope.gcm();

    final PushNotificationEnvelope.Project project = envelope.project();
    if (project == null) {
      return;
    }

    final Notification notification = notificationBuilder(gcm.title(), gcm.alert())
      .setContentIntent(projectContentIntent(envelope, ObjectUtils.toString(project.id())))
      .setLargeIcon(fetchBitmap(project.photo(), false))
      .build();

    notificationManager().notify(envelope.signature(), notification);
  }

  private void displayNotificationFromSurveyResponseActivity(final @NonNull PushNotificationEnvelope envelope,
    final @NonNull SurveyResponse surveyResponse) {

    final GCM gcm = envelope.gcm();

    final PushNotificationEnvelope.Survey survey = envelope.survey();
    if (survey == null) {
      return;
    }

    final Notification notification = notificationBuilder(gcm.title(), gcm.alert())
      .setContentIntent(surveyResponseContentIntent(envelope, surveyResponse))
      .build();
    notificationManager().notify(envelope.signature(), notification);
  }

  private void displayNotificationFromUpdateActivity(final @NonNull PushNotificationEnvelope envelope,
    final @NonNull Update update) {

    final GCM gcm = envelope.gcm();

    final Activity activity = envelope.activity();
    if (activity == null) {
      return;
    }
    final Long updateId = activity.updateId();
    if (updateId == null) {
      return;
    }
    final Long projectId = activity.projectId();
    if (projectId == null) {
      return;
    }

    final String projectParam = ObjectUtils.toString(projectId);

    final Notification notification = notificationBuilder(gcm.title(), gcm.alert())
      .setContentIntent(projectUpdateContentIntent(envelope, update, projectParam))
      .setLargeIcon(fetchBitmap(activity.projectPhoto(), false))
      .build();
    notificationManager().notify(envelope.signature(), notification);
  }

  private @NonNull PendingIntent messageThreadIntent(final @NonNull PushNotificationEnvelope envelope,
    final @NonNull MessageThread messageThread) {

    final Intent messageThreadIntent = new Intent(this.context, MessagesActivity.class)
      .putExtra(IntentKey.MESSAGE_THREAD, messageThread)
      .putExtra(IntentKey.KOALA_CONTEXT, KoalaContext.Message.PUSH);

    final TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this.context)
      .addNextIntentWithParentStack(messageThreadIntent);

    return taskStackBuilder.getPendingIntent(envelope.signature(), PendingIntent.FLAG_UPDATE_CURRENT);
  }

  private @NonNull NotificationCompat.Builder notificationBuilder(final @NonNull String title,
    final @NonNull String text) {

    return new NotificationCompat.Builder(this.context)
      .setSmallIcon(R.drawable.ic_kickstarter_micro_k)
      .setColor(ContextCompat.getColor(this.context, R.color.ksr_green_800))
      .setContentText(text)
      .setContentTitle(title)
      .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
      .setAutoCancel(true);
  }

  private @NonNull PendingIntent projectContentIntent(final @NonNull PushNotificationEnvelope envelope,
    final @NonNull String projectParam) {

    final Intent projectIntent = new Intent(this.context, ProjectActivity.class)
      .putExtra(IntentKey.PROJECT_PARAM, projectParam)
      .putExtra(IntentKey.PUSH_NOTIFICATION_ENVELOPE, envelope)
      .putExtra(IntentKey.REF_TAG, RefTag.push());

    final TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this.context)
      .addNextIntentWithParentStack(projectIntent);

    return taskStackBuilder.getPendingIntent(envelope.signature(), PendingIntent.FLAG_UPDATE_CURRENT);
  }

  private @NonNull PendingIntent projectUpdateContentIntent(final @NonNull PushNotificationEnvelope envelope,
    final @NonNull Update update, final @NonNull String projectParam) {

    final Intent projectIntent = new Intent(this.context, ProjectActivity.class)
      .putExtra(IntentKey.PROJECT_PARAM, projectParam)
      .putExtra(IntentKey.REF_TAG, RefTag.push());

    final Intent updateIntent = new Intent(this.context, UpdateActivity.class)
      .putExtra(IntentKey.PROJECT_PARAM, projectParam)
      .putExtra(IntentKey.UPDATE, update)
      .putExtra(IntentKey.PUSH_NOTIFICATION_ENVELOPE, envelope);

    final TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this.context)
      .addNextIntentWithParentStack(projectIntent)
      .addNextIntent(updateIntent);

    return taskStackBuilder.getPendingIntent(envelope.signature(), PendingIntent.FLAG_UPDATE_CURRENT);
  }

  private @NonNull PendingIntent surveyResponseContentIntent(final @NonNull PushNotificationEnvelope envelope,
    final @NonNull SurveyResponse surveyResponse) {

    final Intent activityFeedIntent = new Intent(this.context, ActivityFeedActivity.class);

    final Intent surveyResponseIntent = new Intent(this.context, SurveyResponseActivity.class)
      .putExtra(IntentKey.SURVEY_RESPONSE, surveyResponse);

    final TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this.context)
      .addNextIntentWithParentStack(activityFeedIntent)
      .addNextIntent(surveyResponseIntent);

    return taskStackBuilder.getPendingIntent(envelope.signature(), PendingIntent.FLAG_UPDATE_CURRENT);
  }

  private @Nullable Bitmap fetchBitmap(final @Nullable String url, final boolean transformIntoCircle) {
    if (url == null) {
      return null;
    }

    try {
      RequestCreator requestCreator = Picasso.with(this.context).load(url).transform(new CropSquareTransformation());
      if (transformIntoCircle) {
        requestCreator = requestCreator.transform(new CircleTransformation());
      }
      return requestCreator.get();
    } catch (IOException e) {
      Timber.e("Failed to load large icon: %s",  e);
      return null;
    }
  }

  private @NonNull NotificationManager notificationManager() {
    return (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
  }

  private @Nullable Observable<Pair<PushNotificationEnvelope, MessageThread>> fetchMessageThreadWithEnvelope(
    final @NonNull PushNotificationEnvelope envelope) {

    final PushNotificationEnvelope.Message message = envelope.message();
    if (message == null) {
      return null;
    }

    final Observable<MessageThread> messageThread = this.client.fetchMessagesForThread(message.messageThreadId())
      .compose(neverError())
      .map(MessageThreadEnvelope::messageThread);

    return Observable.just(envelope)
      .compose(combineLatestPair(messageThread));
  }

  private @Nullable Observable<Pair<PushNotificationEnvelope, SurveyResponse>> fetchSurveyResponseWithEnvelope(
    final @NonNull PushNotificationEnvelope envelope) {

    final PushNotificationEnvelope.Survey survey = envelope.survey();
    if (survey == null) {
      return null;
    }

    final Observable<SurveyResponse> surveyResponse = this.client.fetchSurveyResponse(survey.id())
      .compose(neverError());

    return Observable.just(envelope)
      .compose(combineLatestPair(surveyResponse));
  }

  private @Nullable Observable<Pair<PushNotificationEnvelope, Update>> fetchUpdateWithEnvelope(
    final @NonNull PushNotificationEnvelope envelope) {

    final Activity activity = envelope.activity();
    if (activity == null) {
      return null;
    }

    final Long updateId = activity.updateId();
    if (updateId == null) {
      return null;
    }

    final Long projectId = activity.projectId();
    if (projectId == null) {
      return null;
    }

    final String projectParam = ObjectUtils.toString(projectId);
    final String updateParam = ObjectUtils.toString(updateId);

    final Observable<Update> update = this.client.fetchUpdate(projectParam, updateParam)
      .compose(neverError());

    return Observable.just(envelope)
      .compose(combineLatestPair(update));
  }
}
