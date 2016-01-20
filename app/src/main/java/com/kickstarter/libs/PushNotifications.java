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

import com.kickstarter.R;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.transformations.CropSquareTransformation;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.PlayServicesUtils;
import com.kickstarter.models.Update;
import com.kickstarter.models.pushdata.Activity;
import com.kickstarter.models.pushdata.GCM;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.PushNotificationEnvelope;
import com.kickstarter.services.gcm.RegisterService;
import com.kickstarter.services.gcm.UnregisterService;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.ProjectActivity;
import com.kickstarter.ui.activities.WebViewActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.IOException;

import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class PushNotifications {
  protected final @ForApplication Context context;
  protected final ApiClientType client;
  protected PublishSubject<PushNotificationEnvelope> notifications = PublishSubject.create();
  protected CompositeSubscription subscriptions = new CompositeSubscription();

  public PushNotifications(final @ForApplication Context context, final @NonNull ApiClientType client) {
    this.context = context;
    this.client = client;
  }

  public void initialize() {
    subscriptions.add(notifications
      .filter(PushNotificationEnvelope::isFriendFollow)
      .observeOn(Schedulers.newThread())
      .subscribe(this::displayNotificationFromFriendFollowActivity));

    subscriptions.add(notifications
      .filter(PushNotificationEnvelope::isProjectActivity)
      .observeOn(Schedulers.newThread())
      .subscribe(this::displayNotificationFromProjectActivity));

    subscriptions.add(notifications
      .filter(PushNotificationEnvelope::isProjectReminder)
      .observeOn(Schedulers.newThread())
      .subscribe(this::displayNotificationFromProjectReminder));

    subscriptions.add(notifications
      .filter(PushNotificationEnvelope::isProjectUpdateActivity)
      .observeOn(Schedulers.newThread())
      .subscribe(this::displayNotificationFromUpdateActivity));

    registerDevice();
  }

  public void registerDevice() {
    if (!PlayServicesUtils.isAvailable(context)) {
      return;
    }

    context.startService(new Intent(context, RegisterService.class));
  }

  public void unregisterDevice() {
    if (!PlayServicesUtils.isAvailable(context)) {
      return;
    }

    context.startService(new Intent(context, UnregisterService.class));
  }

  public void add(@NonNull final PushNotificationEnvelope envelope) {
    notifications.onNext(envelope);
  }

  private void displayNotificationFromFriendFollowActivity(final @NonNull PushNotificationEnvelope envelope) {
    final Activity activity = envelope.activity();
    final GCM gcm = envelope.gcm();

    final Notification notification = notificationBuilder(gcm.title(), gcm.alert())
      .setLargeIcon(fetchBitmap(activity.userPhoto(), true))
      .build();
    notificationManager().notify(envelope.signature(), notification);
  }

  private void displayNotificationFromProjectActivity(final @NonNull PushNotificationEnvelope envelope) {
    final GCM gcm = envelope.gcm();

    final Activity activity = envelope.activity();
    if (activity == null) { return; }
    final String projectPhoto = activity.projectPhoto();
    if (projectPhoto == null) { return; }
    final Long projectId = activity.projectId();
    if (projectId == null) { return; }

    final Notification notification = notificationBuilder(gcm.title(), gcm.alert())
      .setContentIntent(projectContentIntent(projectId, envelope.signature()))
      .setLargeIcon(fetchBitmap(projectPhoto, false))
      .build();
    notificationManager().notify(envelope.signature(), notification);
  }

  private void displayNotificationFromProjectReminder(final @NonNull PushNotificationEnvelope envelope) {
    final PushNotificationEnvelope.Project project = envelope.project();
    final GCM gcm = envelope.gcm();

    final Notification notification = notificationBuilder(gcm.title(), gcm.alert())
      .setContentIntent(projectContentIntent(project.id(), envelope.signature()))
      .setLargeIcon(fetchBitmap(project.photo(), false))
      .build();

    notificationManager().notify(envelope.signature(), notification);
  }

  private void displayNotificationFromUpdateActivity(final @NonNull PushNotificationEnvelope envelope) {
    final Activity activity = envelope.activity();
    final GCM gcm = envelope.gcm();

    final String updateId = ObjectUtils.toString(activity.updateId());
    final String projectId = ObjectUtils.toString(activity.projectId());

    final Update update = client
      .fetchUpdate(projectId, updateId)
      .toBlocking().single();

    final Notification notification = notificationBuilder(gcm.title(), gcm.alert())
      .setContentIntent(projectUpdateContentIntent(update, projectId, envelope.signature()))
      .setLargeIcon(fetchBitmap(activity.projectPhoto(), false))
      .build();
    notificationManager().notify(envelope.signature(), notification);
  }

  private @NonNull NotificationCompat.Builder notificationBuilder(final @NonNull String title, final @NonNull String text) {
    return new NotificationCompat.Builder(context)
      .setSmallIcon(R.drawable.ic_kickstarter_k)
      .setColor(ContextCompat.getColor(context, R.color.green))
      .setContentText(text)
      .setContentTitle(title)
      .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
      .setAutoCancel(true);
  }

  private @NonNull PendingIntent projectContentIntent(final @NonNull Long projectId, final int uniqueNotificationId) {
    final Intent resultIntent = new Intent(context, ProjectActivity.class)
      .putExtra(IntentKey.PROJECT_PARAM, projectId.toString());

    final TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context)
      .addParentStack(ProjectActivity.class)
      .addNextIntent(resultIntent);

    return taskStackBuilder.getPendingIntent(uniqueNotificationId, PendingIntent.FLAG_UPDATE_CURRENT);
  }

  private @NonNull PendingIntent projectUpdateContentIntent(final @NonNull Update update, final @NonNull String projectId,
    final int uniqueNotificationId) {

    final Intent projectIntent = new Intent(context, ProjectActivity.class)
      .putExtra(IntentKey.PROJECT_PARAM, projectId.toString());

    final Intent updateIntent = new Intent(context, WebViewActivity.class)
      .putExtra(IntentKey.URL, update.urls().web().update());

    final TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context)
      .addParentStack(ProjectActivity.class)
      .addNextIntent(projectIntent)
      .addNextIntent(updateIntent);

    return taskStackBuilder.getPendingIntent(uniqueNotificationId, PendingIntent.FLAG_UPDATE_CURRENT);
  }

  private @Nullable Bitmap fetchBitmap(final @NonNull String url, final boolean transformIntoCircle) {
    try {
      RequestCreator requestCreator = Picasso.with(context).load(url).transform(new CropSquareTransformation());
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
    return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
  }
}
