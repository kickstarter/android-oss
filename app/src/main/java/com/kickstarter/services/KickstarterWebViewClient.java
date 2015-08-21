package com.kickstarter.services;

import android.content.Context;
import android.net.Uri;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.kickstarter.libs.Build;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.FormContents;
import com.kickstarter.libs.IOUtils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class KickstarterWebViewClient extends WebViewClient {
  private final Build build;
  private final CookieManager cookieManager;
  private final CurrentUser currentUser;
  private final String webEndpoint;
  private final List<ResponseHandler> responseHandlers = new ArrayList<>();
  private FormContents formContents = null;

  public KickstarterWebViewClient(final Build build,
    final CookieManager cookieManager,
    final CurrentUser currentUser,
    final String webEndpoint) {
    this.build = build;
    this.cookieManager = cookieManager;
    this.currentUser = currentUser;
    this.webEndpoint = webEndpoint;

    initializeResponseHandlers();
  }

  @Override
  public WebResourceResponse shouldInterceptRequest(final WebView view, final String url) {
    if (!isInterceptable(Uri.parse(url))) {
      return null;
    }

    final OkHttpClient client = new OkHttpClient();
    client.setCookieHandler(cookieManager);

    try {
      final Request request = buildRequest(url);
      final Response response = client.newCall(request).execute();
      final MimeHeaders mimeHeaders = new MimeHeaders(response.body().contentType().toString());

      if (handleResponse(response, view)) {
        return noopWebResourceResponse();
      }

      final InputStream body = constructBody(view.getContext(), response, mimeHeaders);

      return new WebResourceResponse(mimeHeaders.type, mimeHeaders.encoding, body);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      formContents = null; // TODO: Should unset this much earlier?
    }
  }

  protected InputStream constructBody(final Context context, final Response response, final MimeHeaders mimeHeaders) throws IOException {
    InputStream body = response.body().byteStream();

    if (mimeHeaders.type != null && mimeHeaders.type.equals("text/html")) {
      body = insertWebViewJavascript(context, body);
    }

    return body;
  }

  public void setFormContents(final FormContents formContents) {
    this.formContents = formContents;
  }

  protected Request buildRequest(final String url) {
    Request.Builder requestBuilder = new Request.Builder().url(url);

    RequestBody requestBody = null;
    if (httpMethod().equals("POST")) {
      requestBody = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"),
        formContents.serialized);
    }

    // TODO: All this header code is duplicated, refactor
    requestBuilder.addHeader("Kickstarter-Android-App", build.versionCode().toString());

    final Matcher matcher = Pattern.compile("\\Ahttps:\\/\\/([a-z]+)\\.***REMOVED***\\z")
      .matcher(webEndpoint);
    if (matcher.matches() && !matcher.group(1).equals("www")) {
      Timber.d("Hivequeen environment, adding authorization header");
      requestBuilder.addHeader("Authorization", "Basic ZnV6enk6d3V6enk=");
    }

    final StringBuilder userAgent = new StringBuilder()
      .append("Kickstarter Android Mobile Variant/")
      .append(build.variant())
      .append(" Code/")
      .append(build.versionCode())
      .append(" Version/")
      .append(build.versionName());
    requestBuilder.addHeader("User-Agent", userAgent.toString());

    if (currentUser.exists()) {
      requestBuilder.addHeader("Authorization", "token " + currentUser.getAccessToken());
    }

    requestBuilder.method(httpMethod(), requestBody);

    return requestBuilder.build();
  }

  protected InputStream insertWebViewJavascript(final Context context, final InputStream originalBody) throws IOException {
    final Document document = Jsoup.parse(new String(IOUtils.readFully(originalBody)));
    document.outputSettings().prettyPrint(true);

    final Elements elements = document.getElementsByTag("head");
    if (elements.size() > 0) {
      elements.get(0).prepend(new String(webViewJavascript(context)));
    }

    return new ByteArrayInputStream(document.toString().getBytes("UTF-8"));
  }

  protected byte[] webViewJavascript(final Context context) throws IOException {
    return IOUtils.readFully(context.getAssets().open("www/WebViewJavascript.html"));
  }

  protected String httpMethod() {
    String httpMethod = "GET";
    if (formContents != null && formContents.method != null) {
      httpMethod = formContents.method.toUpperCase();
    }
    return httpMethod;
  }

  protected boolean isInterceptable(final Uri uri) {
    return KickstarterUri.isKickstarterUri(uri, webEndpoint);
  }

  protected WebResourceResponse noopWebResourceResponse() throws IOException {
    return new WebResourceResponse("application/JavaScript", null, new ByteArrayInputStream(new byte[0]));
  }

  private void initializeResponseHandlers() {
    Collections.addAll(responseHandlers,
      new ResponseHandler(KickstarterUri::isProjectUri, this::startProjectDetailActivity)
    );
  }

  // The order of response handlers is important - we iterate through the response handlers
  // sequentially until a match is found.
  public void registerResponseHandlers(final List<ResponseHandler> responseHandlers) {
    this.responseHandlers.addAll(0, responseHandlers);
  }

  private boolean startProjectDetailActivity(final Response response, final WebView webView) {
//    final Context context = webView.getContext();
//    final Intent intent = new Intent(context, ProjectDetailActivity.class);
//    // TODO: Pass project intent
//    context.startActivity(intent);
//    return true;
    return false;
  }


  private boolean handleResponse(final Response response, final WebView webView) {
    final Uri uri = Uri.parse(response.request().urlString());
    for (final ResponseHandler responseHandler : responseHandlers) {
      if (responseHandler.matches(uri, webEndpoint) && responseHandler.action(response, webView)) {
        return true;
      }
    }

    return false;
  }

  public class MimeHeaders {
    public String type = null;
    public String encoding = null;

    public MimeHeaders(final String contentType) {
      // Extract mime and encoding from string, e.g. "text/html; charset=utf-8"
      final Matcher matcher = Pattern.compile("(\\A[\\w\\/]+); charset=([\\w/-]+)\\z")
        .matcher(contentType);
      if (matcher.matches()) {
        type = matcher.group(1);
        encoding = matcher.group(2).toUpperCase();
      }
    }
  }
}
