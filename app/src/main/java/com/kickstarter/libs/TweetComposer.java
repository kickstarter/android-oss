/*
 * Copyright (C) 2015 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ***
 *
 * Original: https://github.com/twitter/twitter-kit-android/blob/a9ff5134a736d10f70331ddea4db35c13cb86c89/tweet-composer/src/main/java/com/twitter/sdk/android/tweetcomposer/TweetComposer.java
 * Modifications: Kickstarter have added some modifiers and annotations. References to `java.net.URL` have also been
 *   changed to `android.net.URI`.
 */

package com.kickstarter.libs.vendor;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;

import java.util.List;

/**
 * The TweetComposer Kit provides a lightweight mechanism for creating intents to interact with the installed Twitter app or a browser.
 */
public class TweetComposer {

  private static final String MIME_TYPE_PLAIN_TEXT = "text/plain";
  private static final String MIME_TYPE_JPEG = "image/jpeg";
  private static final String TWITTER_PACKAGE_NAME = "com.twitter.android";
  private static final String WEB_INTENT = "https://twitter.com/intent/tweet?text=%s&url=%s";

  /**
   * The TweetComposer Builder will use the installed Twitter instance and fall back to a browser
   */
  public static class Builder {
    private final Context context;
    private String text;
    private Uri uri;
    private Uri imageUri;

    /**
     * Initializes a new {@link TweetComposer.Builder}
     */
    public Builder(Context context) {
      if (context == null) {
        throw new IllegalArgumentException("Context must not be null.");
      }
      this.context = context;
    }

    /**
     * Sets Text for Tweet Intent, no length validation is performed
     */
    public Builder text(String text) {
      if (text == null) {
        throw new IllegalArgumentException("text must not be null.");
      }

      if (this.text != null) {
        throw new IllegalStateException("text already set.");
      }
      this.text = text;

      return this;
    }

    /**
     * Sets Uri for Tweet Intent, no length validation is performed
     */
    public Builder uri(Uri uri) {
      if (uri == null) {
        throw new IllegalArgumentException("uri must not be null.");
      }

      if (this.uri != null) {
        throw new IllegalStateException("url already set.");
      }
      this.uri = uri;

      return this;
    }
    /**
     * Sets Image {@link android.net.Uri} for the Tweet. Only valid if the Twitter App is
     * installed.
     * The Uri should be a file Uri to a local file (e.g. <pre><code>Uri.fromFile(someExternalStorageFile)</code></pre>))
     */
    public Builder image(Uri imageUri) {
      if (imageUri == null) {
        throw new IllegalArgumentException("imageUri must not be null.");
      }

      if (this.imageUri != null) {
        throw new IllegalStateException("imageUri already set.");
      }
      this.imageUri = imageUri;

      return this;
    }

    /**
     * Creates {@link android.content.Intent} based on data in {@link TweetComposer.Builder}
     * @return an Intent to the Twitter for Android or a web intent.
     */
    public Intent createIntent() {
      Intent intent = createTwitterIntent();

      if (intent == null) {
        intent = createWebIntent();
      }

      return intent;
    }

    Intent createTwitterIntent() {
      final Intent intent = new Intent(Intent.ACTION_SEND);

      final StringBuilder builder = new StringBuilder();

      if (!TextUtils.isEmpty(text)) {
        builder.append(text);
      }

      if (uri != null) {
        if (builder.length() > 0) {
          builder.append(' ');
        }
        builder.append(uri.toString());
      }

      intent.putExtra(Intent.EXTRA_TEXT, builder.toString());
      intent.setType(MIME_TYPE_PLAIN_TEXT);

      if (imageUri != null) {
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        intent.setType(MIME_TYPE_JPEG);
      }

      final PackageManager packManager = context.getPackageManager();
      final List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(intent,
        PackageManager.MATCH_DEFAULT_ONLY);

      for (ResolveInfo resolveInfo: resolvedInfoList){
        if (resolveInfo.activityInfo.packageName.startsWith(TWITTER_PACKAGE_NAME)){
          intent.setClassName(resolveInfo.activityInfo.packageName,
            resolveInfo.activityInfo.name);
          return intent;
        }
      }

      return null;
    }

    Intent createWebIntent() {
      final String uri = (this.uri == null ? "" : this.uri.toString());


      final String tweetUrl =
        String.format(WEB_INTENT, Uri.encode(text), uri);
      return new Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl));
    }

    /**
     * Starts the intent created in {@link TweetComposer.Builder#createIntent()}
     */
    public void show() {
      final Intent intent = createIntent();
      context.startActivity(intent);
    }
  }
}

