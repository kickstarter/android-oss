package com.kickstarter.libs.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;

import com.kickstarter.R;
import com.kickstarter.ui.activities.DiscoveryActivity;

import java.util.List;

import androidx.annotation.NonNull;
import rx.Observable;

public final class ApplicationUtils {
  private ApplicationUtils() {}

  public static void openUrlExternally(final @NonNull Context context, final @NonNull String url) {
    final Uri uri = Uri.parse(url);
    final List<Intent> targetIntents = targetIntents(context, uri);

    if (!targetIntents.isEmpty()) {
      /* We need to remove the first intent so it's not duplicated when we add the
      EXTRA_INITIAL_INTENTS intents. */
      final Intent chooserIntent = Intent.createChooser(targetIntents.remove(0), context.getString(R.string.View_project));
      chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
        targetIntents.toArray(new Parcelable[targetIntents.size()]));
      context.startActivity(chooserIntent);
    }
  }

  /**
   *
   * Starts the main activity at the top of a task stack, clearing all previous activities.
   *
   * `ACTION_MAIN` does not expect to receive any data in the intent, it should be the same intent as if a user had
   * just launched the app.
   */
  public static void startNewDiscoveryActivity(final @NonNull Context context) {
    final Intent intent = new Intent(context, DiscoveryActivity.class)
      .setAction(Intent.ACTION_MAIN)
      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

    context.startActivity(intent);
  }

  /**
   * Clears all activities from the task stack except discovery.
   */
  public static void resumeDiscoveryActivity(final @NonNull Context context) {
    final Intent intent = new Intent(context, DiscoveryActivity.class)
      .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

    context.startActivity(intent);
  }

  private static List<Intent> targetIntents(final @NonNull Context context, final @NonNull Uri uri) {
    final Uri fakeUri = Uri.parse("http://www.kickstarter.com");
    final Intent browserIntent = new Intent(Intent.ACTION_VIEW, fakeUri);

    return Observable.from(context.getPackageManager().queryIntentActivities(browserIntent, 0))
      .filter(resolveInfo -> !resolveInfo.activityInfo.packageName.contains("com.kickstarter"))
      .map(resolveInfo -> {
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage(resolveInfo.activityInfo.packageName);
        intent.setData(uri);
        return intent;
      })
      .toList()
      .toBlocking()
      .single();
  }
}
