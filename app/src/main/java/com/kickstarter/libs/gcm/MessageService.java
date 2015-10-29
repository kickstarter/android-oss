package com.kickstarter.libs.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;
import com.kickstarter.R;
import com.kickstarter.ui.activities.DiscoveryActivity;

import timber.log.Timber;

/**
 * Enables various aspects of handling messages such as detecting different downstream message types,
 * determining upstream send status, and automatically displaying simple notifications on the app’s behalf.
 */
public class MessageService extends GcmListenerService {

  /**
   * Called when message is received.
   *
   * @param from SenderID of the sender.
   * @param data Data bundle containing message data as key/value pairs.
   *             For Set of keys use data.keySet().
   */
  @Override
  public void onMessageReceived(@NonNull final String from, @NonNull final Bundle data) {
    final String senderId = getString(R.string.gcm_defaultSenderId);
    if (!from.equals(senderId)) {
      Timber.e("Received a message from " + from + ", expecting " + senderId);
      return;
    }

    final String message = data.getString("message");
    if (message == null) {
      Timber.e("Received empty message");
      return;
    }

    Timber.d("Message: " + message);

    if (from.startsWith("/topics/")) {
      // message received from some topic.
    } else {
      // normal downstream message.
    }

    showNotification(message);
  }

  /**
   * Create and show a simple notification containing the received GCM message.
   *
   * @param message GCM message received.
   */
  private void showNotification(@NonNull final String message) {
    Timber.d("Sending new notification: " + message);
    // TODO: Switch intent appropriately
    final Intent intent = new Intent(this, DiscoveryActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
      PendingIntent.FLAG_ONE_SHOT);

    final Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
      .setSmallIcon(android.R.drawable.btn_star)
      .setContentTitle("Notification")
      .setContentText(message)
      .setAutoCancel(true)
      .setSound(defaultSoundUri)
      .setContentIntent(pendingIntent);

    final NotificationManager notificationManager =
      (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
  }
}
