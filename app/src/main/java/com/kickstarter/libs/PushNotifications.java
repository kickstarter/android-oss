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

import com.kickstarter.R;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.transformations.CropSquareTransformation;
import com.kickstarter.libs.utils.PlayServicesUtils;
import com.kickstarter.models.pushdata.Activity;
import com.kickstarter.models.pushdata.GCM;
import com.kickstarter.services.apiresponses.PushNotificationEnvelope;
import com.kickstarter.services.gcm.RegisterService;
import com.kickstarter.services.gcm.UnregisterService;
import com.kickstarter.ui.activities.ProjectActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.IOException;

import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class PushNotifications {
  @ForApplication final Context context;
  PublishSubject<PushNotificationEnvelope> notifications = PublishSubject.create();
  CompositeSubscription subscriptions = new CompositeSubscription();

  public PushNotifications(@ForApplication final Context context) {
    this.context = context;
  }

  public void initialize() {
    subscriptions.add(notifications
      .filter(PushNotificationEnvelope::isFriendFollow)
      .observeOn(Schedulers.newThread())
      .subscribe(this::showFriendFollow));

    subscriptions.add(notifications
      .filter(PushNotificationEnvelope::isProjectActivity)
      .observeOn(Schedulers.newThread())
      .subscribe(this::showProjectActivity));

    subscriptions.add(notifications
      .filter(PushNotificationEnvelope::isProjectReminder)
      .observeOn(Schedulers.newThread())
      .subscribe(this::showProjectReminder));

    subscriptions.add(notifications
      .filter(PushNotificationEnvelope::isProjectUpdateActivity)
      .observeOn(Schedulers.newThread())
      .subscribe(this::showProjectUpdateActivity));

    registerDevice();
  }

  public void registerDevice() {
    if (!PlayServicesUtils.isAvailable(context)) {
      return;
    }

    final Intent intent = new Intent(context, RegisterService.class);
    context.startService(intent);
  }

  public void unregisterDevice() {
    if (!PlayServicesUtils.isAvailable(context)) {
      return;
    }

    final Intent intent = new Intent(context, UnregisterService.class);
    context.startService(intent);
  }

  public void add(@NonNull final PushNotificationEnvelope envelope) {
    notifications.onNext(envelope);
  }

  private NotificationCompat.Builder builder() {
    return new NotificationCompat.Builder(context)
      .setSmallIcon(R.drawable.ic_kickstarter_k)
      .setColor(context.getResources().getColor(R.color.green))
      .setAutoCancel(true);
  }

  private void showFriendFollow(@NonNull final PushNotificationEnvelope envelope) {
    final GCM gcm = envelope.gcm();

    // TODO: Friend icon, intent to load friends
    final Notification notification = builder()
      .setContentText(gcm.alert())
      .setContentTitle(gcm.title())
      .setStyle(new NotificationCompat.BigTextStyle().bigText(gcm.alert()))
      .build();

    notificationManager().notify(envelope.signature(), notification);
  }

  private void showProjectActivity(@NonNull final PushNotificationEnvelope envelope) {
    final Activity activity = envelope.activity();
    final GCM gcm = envelope.gcm();

    final Notification notification = builder()
      .setLargeIcon(fetchBitmap(activity.projectPhoto(), false))
      .setContentIntent(projectContentIntent(activity.projectId()))
      .setContentText(gcm.alert())
      .setContentTitle(gcm.title())
      .setStyle(new NotificationCompat.BigTextStyle().bigText(gcm.alert()))
      .build();

    notificationManager().notify(envelope.signature(), notification);
  }

  private void showProjectUpdateActivity(@NonNull final PushNotificationEnvelope envelope) {
    final Activity activity = envelope.activity();
    final GCM gcm = envelope.gcm();

    // TODO: Content intent

    final Notification notification = builder()
      .setLargeIcon(fetchBitmap(activity.projectPhoto(), false))
      .setContentText(gcm.alert())
      .setContentTitle(gcm.title())
      .setStyle(new NotificationCompat.BigTextStyle().bigText(gcm.alert()))
      .build();

    notificationManager().notify(envelope.signature(), notification);
  }

  private @NonNull PendingIntent projectContentIntent(@NonNull final Long id) {
    final Intent intent = new Intent(context, ProjectActivity.class)
      .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      .putExtra(context.getString(R.string.intent_project_param), id.toString());

    // TODO: Check the flags!
    return PendingIntent.getActivity(context, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);
  }

  private void showProjectReminder(@NonNull final PushNotificationEnvelope envelope) {
    final GCM gcm = envelope.gcm();

    final Notification notification = builder()
      .setLargeIcon(fetchBitmap(envelope.project().photoUrl(), false))
      .setContentIntent(projectContentIntent(envelope.project().id()))
      .setContentText(gcm.alert())
      .setContentTitle(gcm.title())
      .setStyle(new NotificationCompat.BigTextStyle().bigText(gcm.alert()))
      .build();

    notificationManager().notify(envelope.signature(), notification);
  }

  private @Nullable Bitmap fetchBitmap(@NonNull final String url, final boolean isCircle) {
    try {
      RequestCreator requestCreator = Picasso.with(context).load(url).transform(new CropSquareTransformation());
      if (isCircle) {
        requestCreator = requestCreator.transform(new CircleTransformation());
      }
      return requestCreator.get();
    } catch (IOException e) {
      Timber.e("Failed to load large icon: " + e);
      return null;
    }
  }

  private NotificationManager notificationManager() {
    return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
  }
}
