package com.kickstarter.services;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.kickstarter.libs.Build;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KickstarterWebViewClient extends WebViewClient {
  private final Build build;

  public KickstarterWebViewClient(final Build build) {
    this.build = build;
  }

  @Override
  public WebResourceResponse shouldInterceptRequest(final WebView view, final WebResourceRequest request) {
    // TODO: Check injected host rather than 'www.kickstarter.com' - e.g. if we change endpoints
    // TODO: Fix up support for lower API versions
    if (!request.getUrl().getHost().equals("www.kickstarter.com")) {
      return null;
    }

    // TODO: Add more endpoints, make pattern matching more robust
    if (!Pattern.compile("/pledge/new").matcher(request.getUrl().toString()).find()) {
      return null;
    }

    final HttpGet httpGet = new HttpGet(request.getUrl().toString());
    httpGet.setHeader("Kickstarter-Android-App", build.versionCode().toString());
    try {
      final HttpResponse response = new DefaultHttpClient().execute(httpGet);
      Header contentType = response.getEntity().getContentType();
      String mimeType = null;
      String encoding = null;
      if (contentType != null) {
        // Extract mime and encoding from string, e.g. "text/html; charset=utf-8"
        final Pattern pattern = Pattern.compile("([\\w\\/]+); charset=([\\w/-]+)");
        final Matcher matcher = pattern.matcher(contentType.getValue());
        if (matcher.matches()) {
          mimeType = matcher.group(1);
          encoding = matcher.group(2).toUpperCase();
        }
      }

      final Map<String,String> responseHeaders = new HashMap<String,String>();
      final HeaderIterator iterator = response.headerIterator();
      while(iterator.hasNext()) {
        Header header = iterator.nextHeader();
        responseHeaders.put(header.getName(), header.getValue());
      }

      InputStream data = response.getEntity().getContent();
      return new WebResourceResponse(mimeType,
        encoding,
        response.getStatusLine().getStatusCode(),
        response.getStatusLine().getReasonPhrase(),
        responseHeaders,
        data);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
    return false;
  }
}
