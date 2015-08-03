package com.kickstarter.services;

import android.content.Context;
import android.net.Uri;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.kickstarter.KsrApplication;
import com.kickstarter.libs.Build;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.FormContents;
import com.kickstarter.libs.IOUtils;
import com.kickstarter.ui.activities.CheckoutActivity;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class KickstarterWebViewClient extends WebViewClient {
  private final Build build;
  private final CookieManager cookieManager;
  private final CurrentUser currentUser;
  private final String webEndpoint;
  private FormContents formContents = null;

  public KickstarterWebViewClient(final Build build,
    final CookieManager cookieManager,
    final CurrentUser currentUser,
    final String webEndpoint) {
    this.build = build;
    this.cookieManager = cookieManager;
    this.currentUser = currentUser;
    this.webEndpoint = webEndpoint;
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

      // TODO: Move into handler
      final Uri lastRequestUri = Uri.parse(response.request().urlString());
      if (isSignupUri(lastRequestUri)) {
        // TODO: Is this safe to call from here? (threading)
        Timber.d("Triggered isSignupUri");
        ((CheckoutActivity) view.getContext()).onSignupUriRequest();
        return noopWebResourceResponse();
      } else if (isCheckoutThanksUri(lastRequestUri)) {
        ((CheckoutActivity) view.getContext()).onCheckoutThanksUriRequest();
        return noopWebResourceResponse();
      }

      InputStream body = response.body().byteStream();

      if (mimeHeaders.type != null && mimeHeaders.type.equals("text/html")) {
        body = bodyWithWebViewJavascript(view.getContext(), body);
      }

      return new WebResourceResponse(mimeHeaders.type, mimeHeaders.encoding, body);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      formContents = null; // TODO: Should unset this much earlier?
    }
  }

  public void setFormContents(final FormContents formContents) {
    this.formContents = formContents;
  }

  protected Request buildRequest(final String url) {
    Request.Builder requestBuilder = new Request.Builder().url(url);

    RequestBody requestBody = null;
    if (httpMethod().equals("POST")) {
      requestBody = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"), formContents.serialized);
    }

    requestBuilder.addHeader("Kickstarter-Android-App", build.versionCode().toString());

    // Add authorization if it's a Hivequeen environment. TODO: Inject this
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

  protected InputStream bodyWithWebViewJavascript(final Context context, final InputStream originalBody) throws IOException {
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
    return isKickstarterUri(uri);
  }

  protected boolean isKickstarterUri(final Uri uri) {
    return uri.getHost().equals(Uri.parse(webEndpoint).getHost());
  }

  protected boolean isSignupUri(final Uri uri) {
    return isKickstarterUri(uri) && uri.getPath().equals("/signup");
  }

  protected boolean isCheckoutThanksUri(final Uri uri) {
    // e.g. /projects/slug-1/slug-2/checkouts/1/thanks
    return isKickstarterUri(uri) &&
      Pattern.compile("\\A\\/projects/[a-zA-Z0-9_-]+\\/[a-zA-Z0-9_-]+\\/checkouts\\/\\d+\\/thanks\\z")
        .matcher(uri.getPath()).matches();
  }

  protected boolean isProjectNewPledgeUri(final Uri uri) {
    // e.g. /projects/slug-1/slug-2/pledge/new
    return isKickstarterUri(uri) &&
      Pattern.compile("\\A\\/projects/[a-zA-Z0-9_-]+\\/[a-zA-Z0-9_-]+\\/pledge\\/new\\z")
        .matcher(uri.getPath()).matches();
  }

  protected WebResourceResponse noopWebResourceResponse() throws IOException {
    return new WebResourceResponse("application/JavaScript", null, new ByteArrayInputStream(new byte[0]));

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
