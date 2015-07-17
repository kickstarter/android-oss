package com.kickstarter.services;

import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.kickstarter.libs.Build;
import com.kickstarter.libs.CurrentUser;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class KickstarterWebViewClient extends WebViewClient {
  private final Build build;
  private final CurrentUser currentUser;
  private final String webEndpoint;

  public KickstarterWebViewClient(final Build build, final CurrentUser currentUser, final String webEndpoint) {
    this.build = build;
    this.currentUser = currentUser;
    this.webEndpoint = webEndpoint;
  }

  @Override
  public WebResourceResponse shouldInterceptRequest(final WebView view, final String url) {
    final Uri baseUri = Uri.parse(url);
    if (!isInterceptable(baseUri)) {
      return null;
    }

    final HttpGet httpGet = new HttpGet(buildUri(baseUri).toString());
    httpGet.setHeader("Kickstarter-Android-App", build.versionCode().toString());
    Timber.d("Intercepting request: %s", httpGet.getURI().toString());
    try {
      final HttpResponse response = new DefaultHttpClient().execute(httpGet);
      final MimeHeaders mimeHeaders = new MimeHeaders(response.getEntity().getContentType());

      return new WebResourceResponse(mimeHeaders.type, mimeHeaders.encoding, response.getEntity().getContent());
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  protected Uri buildUri(final Uri uri) {
    final Uri.Builder uriBuilder = uri.buildUpon();
    if (currentUser.exists()) {
      uriBuilder.appendQueryParameter("oauth_token", currentUser.getAccessToken());
    }
    return uriBuilder.build();
  }

  protected boolean isInterceptable(final Uri uri) {
    return uri.getHost().equals(Uri.parse(webEndpoint).getHost());
  }

  protected boolean isProjectNewPledgeUrl(final Uri uri) {
    return Pattern.compile("\\A\\/projects/[a-zA-Z0-9_-]+\\/[a-zA-Z0-9_-]+\\/pledge\\/new\\z")
      .matcher(uri.getPath()).matches();
  }

  // Unused, useful if we use new WebResourceResponse constructor added in API 21
  protected Map<String,String> headers(final HttpResponse response) {
    final Map<String,String> headers = new HashMap<String,String>();
    final HeaderIterator iterator = response.headerIterator();
    while(iterator.hasNext()) {
      Header header = iterator.nextHeader();
      headers.put(header.getName(), header.getValue());
    }
    return headers;
  }


  public class MimeHeaders {
    public String type = null;
    public String encoding = null;

    public MimeHeaders(final Header header) {
      if (header == null) {
        return;
      }

      // Extract mime and encoding from string, e.g. "text/html; charset=utf-8"
      final Matcher matcher = Pattern.compile("(\\A[\\w\\/]+); charset=([\\w/-]+)\\z")
        .matcher(header.getValue());
      if (matcher.matches()) {
        type = matcher.group(1);
        encoding = matcher.group(2).toUpperCase();
      }
    }
  }
}
