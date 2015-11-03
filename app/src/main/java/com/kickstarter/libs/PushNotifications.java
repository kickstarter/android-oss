package com.kickstarter.libs;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.kickstarter.R;
import com.kickstarter.libs.transformations.CropSquareTransformation;
import com.kickstarter.libs.utils.PlayServicesUtils;
import com.kickstarter.models.pushdata.Activity;
import com.kickstarter.models.pushdata.GCM;
import com.kickstarter.services.apiresponses.PushNotificationEnvelope;
import com.kickstarter.services.gcm.RegisterService;
import com.kickstarter.services.gcm.UnregisterService;
import com.kickstarter.ui.activities.ProjectActivity;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class PushNotifications {
  private static final int NOTIFICATION_ID = 10000; // TODO: This is temporary

  @ForApplication final Context context;
  PublishSubject<PushNotificationEnvelope> envelopeSubject = PublishSubject.create();
  Subscription envelopeSubscription;

  public PushNotifications(@ForApplication final Context context) {
    this.context = context;
  }

  public void initialize() {
    envelopeSubscription = envelopeSubject
      .observeOn(Schedulers.newThread())
      .subscribe(this::showProjectNotification);
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

  public void show(@NonNull final PushNotificationEnvelope envelope) {
    envelopeSubject.onNext(envelope);
  }

  private void showProjectNotification(@NonNull final PushNotificationEnvelope envelope) {
    final Activity activity = envelope.activity();
    final GCM gcm = envelope.gcm();

    final Intent intent = new Intent(context, ProjectActivity.class)
      .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      .putExtra(context.getString(R.string.intent_project_param), activity.projectId());

    final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

    Bitmap largeIcon;
    try {
      largeIcon = Picasso.with(context)
        .load(activity.projectPhoto())
        .transform(new CropSquareTransformation())
        .get();
    } catch (IOException e) {
      largeIcon = null;
      Timber.e("Failed to load activity photo: " + e);
    }

    NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
      .setSmallIcon(R.drawable.ic_kickstarter_k)
      .setContentTitle(gcm.title())
      .setContentText(gcm.alert())
      .setColor(context.getResources().getColor(R.color.green))
      .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(gcm.alert()))
      .setAutoCancel(true)
      .setLargeIcon(largeIcon)
      .setContentIntent(pendingIntent);

    final Notification notification = builder.build();

    final NotificationManager notificationManager =
      (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    notificationManager.notify(NOTIFICATION_ID, notification);
  }
}
