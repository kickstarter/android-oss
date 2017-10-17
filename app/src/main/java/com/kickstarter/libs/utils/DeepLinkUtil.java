package com.kickstarter.libs.utils;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;

import com.kickstarter.libs.RefTag;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.DeepLinkActivity;
import com.kickstarter.ui.activities.ProjectActivity;

import java.util.ArrayList;
import java.util.List;

public class DeepLinkUtil {

  public static void startDiscoveryActivity(DeepLinkActivity deepLinkActivity) {
    ApplicationUtils.startNewDiscoveryActivity(deepLinkActivity);
    deepLinkActivity.finish();
  }

  public static void startProjectActivity(DeepLinkActivity deepLinkActivity, String url) {
    Uri uri = Uri.parse(url);
    final Intent projectIntent = new Intent(deepLinkActivity, ProjectActivity.class)
      .setData(uri);
    String ref = uri.getQueryParameter("ref");
    if(ref != null) {
      projectIntent.putExtra(IntentKey.REF_TAG, RefTag.from(ref));
    }
    deepLinkActivity.startActivity(projectIntent);
    deepLinkActivity.finish();
  }


  public static void startBrowser(DeepLinkActivity deepLinkActivity, String url) {
    Uri uri = Uri.parse(url);

    // We'll ask the system to open a generic URL, rather than the deep-link
    // capable one we actually want.
    Uri fakeUri = Uri.parse("http://www.kickstarter.com");

    Intent browserIntent = new Intent(Intent.ACTION_VIEW, fakeUri);
    PackageManager pm = deepLinkActivity.getPackageManager();
    List<ResolveInfo> activities = pm.queryIntentActivities(browserIntent, 0);

    // Loop through everything the system gives us, and remove the current
    // app (the whole point here is to open the link in something else).
    final List<Intent> targetIntents = new ArrayList<>(activities.size());
    for (ResolveInfo currentInfo : activities) {
      String packageName = currentInfo.activityInfo.packageName;
      if (!packageName.contains("com.kickstarter")) {
        // Build an intent pointing to the found package, but
        // this intent will contain the _real_ url.
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage(packageName);
        intent.setData(uri);
        targetIntents.add(intent);
      }
    }

    // Now present the user with the list of apps we have found (this chooser
    // is smart enough to just open a single option directly, so we don't need
    // to handle that case).
    Intent chooserIntent = Intent.createChooser(targetIntents.remove(0), "");
    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
      targetIntents.toArray(new Parcelable[targetIntents.size()]));
    deepLinkActivity.startActivity(chooserIntent);

    deepLinkActivity.finish();
  }
}
