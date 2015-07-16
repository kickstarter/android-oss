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
    if (!isInterceptable(request)) {
      return null;
    }

    final HttpGet httpGet = new HttpGet(request.getUrl().toString());
    httpGet.setHeader("Kickstarter-Android-App", build.versionCode().toString());
    try {
      final HttpResponse response = new DefaultHttpClient().execute(httpGet);
      final MimeHeaders mimeHeaders = new MimeHeaders(response.getEntity().getContentType());

      return new WebResourceResponse(mimeHeaders.type,
        mimeHeaders.encoding,
        response.getStatusLine().getStatusCode(),
        response.getStatusLine().getReasonPhrase(),
        headers(response),
        response.getEntity().getContent());
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
    return false;
  }

  protected boolean isInterceptable(final WebResourceRequest request) {
    // TODO: Check injected host rather than 'www.kickstarter.com' - e.g. if we change endpoints
    // TODO: Fix up support for lower API versions
    if (!request.getUrl().getHost().equals("www.kickstarter.com")) {
      return false;
    }

    // TODO: Add more endpoints, make pattern matching more robust
    return Pattern.compile("/pledge/new").matcher(request.getUrl().toString()).find();
  }

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
      final Matcher matcher = Pattern.compile("([\\w\\/]+); charset=([\\w/-]+)")
        .matcher(header.getValue());
      if (matcher.matches()) {
        type = matcher.group(1);
        encoding = matcher.group(2).toUpperCase();
      }
    }
  }
}
