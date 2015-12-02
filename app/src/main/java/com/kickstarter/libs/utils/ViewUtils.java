package com.kickstarter.libs.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.kickstarter.R;
import com.kickstarter.ui.views.KSDialog;

import rx.functions.Action1;

public class ViewUtils {
  public ViewUtils() {}

  public static boolean isFontScaleLarge(final @NonNull Context context) {
    return context.getResources().getConfiguration().fontScale > 1.5f;
  }

  public static boolean isLandscape(@NonNull final Context context) {
    return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
  }

  public static boolean isPortrait(@NonNull final Context context) {
    return !isLandscape(context);
  }

  /**
   * Show a dialog box to the user.
   */
  public static void showDialog(@NonNull final Context context, @Nullable final String title, @NonNull final String message) {
    new KSDialog(context, title, message).show();
  }

  public static void showDialog(@NonNull final Context context, @Nullable final String title,
    @NonNull final String message, @NonNull final String buttonMessage) {
    new KSDialog(context, title, message, buttonMessage).show();
  }

  /**
   * Show a toast to the user.
   */
  public static void showToast(@NonNull final Context context, @NonNull final String message) {
    final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    final View view = inflater.inflate(R.layout.toast, null);
    final TextView text = (TextView) view.findViewById(R.id.toast_text_view);
    text.setText(message);

    final Toast toast = new Toast(context);
    toast.setView(view);
    toast.show();
  }

  /**
   * Curried form of showToast.
   */
  public static Action1<String> showToast(@NonNull final Context context) {
    return (message) -> showToast(context, message);
  }
}
