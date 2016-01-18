package com.kickstarter.libs.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kickstarter.R;
import com.kickstarter.ui.views.KSDialog;

import rx.functions.Action1;

public class ViewUtils {
  public ViewUtils() {}

  public static int getScreenHeightDp(final @NonNull Context context) {
    return context.getResources().getConfiguration().screenHeightDp;
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
    new KSDialog(context, title, message).show();
  }

  public static void showDialog(final @NonNull Context context, final @Nullable String title,
    final @NonNull String message, final @NonNull String buttonMessage) {
    new KSDialog(context, title, message, buttonMessage).show();
  }

  /**
   * Show a toast with default bottom gravity to the user.
   */
  public static void showToast(final @NonNull Context context, final @NonNull String message) {
    final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    final View view = inflater.inflate(R.layout.toast, null);
    final TextView text = (TextView) view.findViewById(R.id.toast_text_view);
    text.setText(message);

    final Toast toast = new Toast(context);
    toast.setView(view);
    toast.show();
  }

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

  /**
   * Curried form of showToast.
   */
  public static Action1<String> showToast(final @NonNull Context context) {
    return (message) -> showToast(context, message);
  }
}
