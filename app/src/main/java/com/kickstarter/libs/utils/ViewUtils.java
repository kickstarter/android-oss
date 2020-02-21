package com.kickstarter.libs.utils;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kickstarter.R;
import com.kickstarter.libs.ApiCapabilities;
import com.kickstarter.ui.views.AppRatingDialog;
import com.kickstarter.ui.views.ConfirmDialog;

import rx.functions.Action1;

public final class ViewUtils {
  private ViewUtils() {}

  public static void addBoldSpan(final @NonNull SpannableString spannableString, final @NonNull String substring) {
    if (spannableString.toString().contains(substring)) {
      final int index = spannableString.toString().indexOf(substring);
      final StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
      spannableString.setSpan(boldSpan, index, index + substring.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    }
  }

  public static Bitmap getBitmap(final @NonNull View view, final int width, final int height) {
    final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    final Canvas canvas = new Canvas(bitmap);
    view.draw(canvas);
    return bitmap;
  }

  public static float getScreenDensity(final @NonNull Context context) {
    return context.getResources().getDisplayMetrics().density;
  }

  public static int getScreenHeightDp(final @NonNull Context context) {
    return context.getResources().getConfiguration().screenHeightDp;
  }

  public static int getScreenWidthDp(final @NonNull Context context) {
    return context.getResources().getConfiguration().screenWidthDp;
  }

  /**
   * Returns a Spanned representation of the passed in HTML string using the
   * proper API method.
   *
   */
  public static Spanned html(final @NonNull String htmlString) {
    if (ApiCapabilities.needsLegacyHtml()) {
      return Html.fromHtml(htmlString);
    } else {
      return Html.fromHtml(htmlString, Html.FROM_HTML_MODE_COMPACT);
    }
  }

  public static boolean isFontScaleLarge(final @NonNull Context context) {
    return context.getResources().getConfiguration().fontScale > 1.5f;
  }

  public static boolean isLandscape(final @NonNull Context context) {
    return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
  }

  public static boolean isPortrait(final @NonNull Context context) {
    return !isLandscape(context);
  }

  /**
   * Set layout margins for a ViewGroup with LinearLayout parent.
   */
  public static void setLinearViewGroupMargins(final @NonNull ViewGroup viewGroup, final int leftMargin, final int topMargin,
    final int rightMargin, final int bottomMargin) {
    final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(viewGroup.getLayoutParams());
    layoutParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
    viewGroup.setLayoutParams(layoutParams);
  }

  /**
   * Set layout margins for a ViewGroup with RelativeLayout parent.
   */
  public static void setRelativeViewGroupMargins(final @NonNull ViewGroup viewGroup, final int leftMargin, final int topMargin,
    final int rightMargin, final int bottomMargin) {
    final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(viewGroup.getLayoutParams());
    layoutParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
    viewGroup.setLayoutParams(layoutParams);
  }

  /**
   * Show a dialog box to the user.
   */
  public static void showDialog(final @NonNull Context context, final @Nullable String title, final @NonNull String message) {
    new ConfirmDialog(context, title, message).show();
  }

  public static void showDialog(final @NonNull Context context, final @Nullable String title,
    final @NonNull String message, final @NonNull String buttonMessage) {
    new ConfirmDialog(context, title, message, buttonMessage).show();
  }

  public static void showRatingDialog(final @NonNull Context context) {
    new AppRatingDialog(context).show();
  }

  /**
   * Opens the play store native app or the play store web site.
   */
  public static void openStoreRating(final @NonNull Context context, final @NonNull String packageName) {
    final Intent intent = new Intent(Intent.ACTION_VIEW);

    try {
      // First try to load the play store native application
      final Uri marketUri = Uri.parse("market://details?id=" + packageName);
      intent.setData(marketUri);
      context.startActivity(intent);
    } catch (ActivityNotFoundException __) {
      // Fallback to the play store web site
      final Uri httpUri = Uri.parse("http://play.google.com/store/apps/details?id=" + packageName);
      intent.setData(httpUri);
      context.startActivity(intent);
    }
  }

  /**
   * Show a toast with default bottom gravity to the user.
   */
  @SuppressLint("InflateParams")
  public static void showToast(final @NonNull Context context, final @NonNull String message) {
    final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    final View view = inflater.inflate(R.layout.toast, null);
    final TextView text = (TextView) view.findViewById(R.id.toast_text_view);
    text.setText(message);

    final Toast toast = new Toast(context);
    toast.setView(view);
    toast.show();
  }

  @SuppressLint("InflateParams")
  public static void showToastFromTop(final @NonNull Context context, final @NonNull String message, final int xOffset,
    final int yOffset) {
    final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    final View view = inflater.inflate(R.layout.toast, null);
    final TextView text = (TextView) view.findViewById(R.id.toast_text_view);
    text.setText(message);

    final Toast toast = new Toast(context);
    toast.setView(view);
    toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, xOffset, yOffset);
    toast.show();
  }

  public static Action1<String> showToast(final @NonNull Context context) {
    return (message) -> showToast(context, message);
  }

  /**
   * Sets the visiblity of a view to {@link View#VISIBLE} or {@link View#GONE}. Setting
   * the view to GONE removes it from the layout so that it no longer takes up any space.
   */
  public static void setGone(final @NonNull View view, final boolean gone) {
    if (gone) {
      view.setVisibility(View.GONE);
    } else {
      view.setVisibility(View.VISIBLE);
    }
  }

  public static Action1<Boolean> setGone(final @NonNull View view) {
    return (gone) -> setGone(view, gone);
  }

  /**
   * Sets the visibility of a view to {@link View#VISIBLE} or {@link View#INVISIBLE}. Setting
   * the view to INVISIBLE makes it hidden, but it still takes up space.
   */
  public static void setInvisible(final @NonNull View view, final boolean hidden) {
    if (hidden) {
      view.setVisibility(View.INVISIBLE);
    } else {
      view.setVisibility(View.VISIBLE);
    }
  }

  public static Action1<Boolean> setInvisible(final @NonNull View view) {
    return (invisible) -> setInvisible(view, invisible);
  }
}
