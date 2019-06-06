package com.kickstarter.libs.utils;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kickstarter.R;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.models.Country;
import com.kickstarter.models.Project;
import com.kickstarter.ui.data.ScreenLocation;
import com.kickstarter.ui.views.AppRatingDialog;
import com.kickstarter.ui.views.CenterSpan;
import com.kickstarter.ui.views.ConfirmDialog;

import org.jetbrains.annotations.NotNull;

import java.math.RoundingMode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import rx.functions.Action1;

public final class ViewUtils {
  private ViewUtils() {}

  public static Bitmap getBitmap(final @NonNull View view, final int width, final int height) {
    final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    final Canvas canvas = new Canvas(bitmap);
    view.draw(canvas);
    return bitmap;
  }

  public static float getScreenDensity(final @NonNull Context context) {
    return context.getResources().getDisplayMetrics().density;
  }

  public static ScreenLocation getScreenLocation(final  @NonNull View view) {
    return new ScreenLocation(view.getLeft(), view.getTop(), view.getHeight(), view.getWidth());
  }

  public static int getScreenHeightDp(final @NonNull Context context) {
    return context.getResources().getConfiguration().screenHeightDp;
  }

  public static int getScreenWidthDp(final @NonNull Context context) {
    return context.getResources().getConfiguration().screenWidthDp;
  }

  /**
   * Returns a SpannableString that shrinks and centers currency code if it's necessary.
   * Special case: US people looking at US currency just get the currency symbol.
   *
   */
  public static @NotNull SpannableString styleCurrency(final double value, final Project project, final @NonNull KSCurrency ksCurrency) {
    final String formattedCurrency = ksCurrency.format(value, project, RoundingMode.HALF_UP);
    final SpannableString spannableString = new SpannableString(formattedCurrency);

    final Country country = Country.findByCurrencyCode(project.currency());
    if (country == null) {
      return spannableString;
    }

    final boolean currencyNeedsCode = ksCurrency.currencyNeedsCode(country, true);
    final String currencySymbolToDisplay = StringUtils.trim(ksCurrency.getCurrencySymbol(country, true));

    if (currencyNeedsCode) {
      final int startOfSymbol = formattedCurrency.indexOf(currencySymbolToDisplay);
      final int endOfSymbol = startOfSymbol + currencySymbolToDisplay.length();
      spannableString.setSpan(new RelativeSizeSpan(.5f), startOfSymbol, endOfSymbol, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
      spannableString.setSpan(new CenterSpan(), startOfSymbol, endOfSymbol, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }

    return spannableString;
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
   * Sets the visiblity of a view to {@link View#VISIBLE} or {@link View#INVISIBLE}. Setting
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
