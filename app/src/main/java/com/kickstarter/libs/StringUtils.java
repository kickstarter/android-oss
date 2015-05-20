package com.kickstarter.libs;

import android.content.Context;
import android.util.Patterns;

import com.kickstarter.R;

public class StringUtils {
  private StringUtils() {}

  public static boolean isEmail(final CharSequence str) {
    return Patterns.EMAIL_ADDRESS.matcher(str).matches();
  }

  // TODO: Take category instead of id
  public static String friendBackingActivityTitle(final Context context, final String friend_name, final Integer category_id) {
    final String str;

    // TODO: Would be nice to switch on a category enum
    switch (category_id) {
      case 1: str = context.getResources().getString(R.string.friend_name_backed_art_project, friend_name); break;
      case 3: str = context.getResources().getString(R.string.friend_name_backed_comics_project, friend_name); break;
      case 26: str = context.getResources().getString(R.string.friend_name_backed_crafts_project, friend_name); break;
      case 6: str = context.getResources().getString(R.string.friend_name_backed_dance_project, friend_name); break;
      case 7: str = context.getResources().getString(R.string.friend_name_backed_design_project, friend_name); break;
      case 9: str = context.getResources().getString(R.string.friend_name_backed_fashion_project, friend_name); break;
      case 11: str = context.getResources().getString(R.string.friend_name_backed_film_project, friend_name); break;
      case 10: str = context.getResources().getString(R.string.friend_name_backed_food_project, friend_name); break;
      case 12: str = context.getResources().getString(R.string.friend_name_backed_games_project, friend_name); break;
      case 13: str = context.getResources().getString(R.string.friend_name_backed_journalism_project, friend_name); break;
      case 14: str = context.getResources().getString(R.string.friend_name_backed_music_project, friend_name); break;
      case 15: str = context.getResources().getString(R.string.friend_name_backed_photography_project, friend_name); break;
      case 18: str = context.getResources().getString(R.string.friend_name_backed_publishing_project, friend_name); break;
      case 16: str = context.getResources().getString(R.string.friend_name_backed_technology_project, friend_name); break;
      case 17: str = context.getResources().getString(R.string.friend_name_backed_theater_project, friend_name); break;
      default: str = "";
    }

    return str;
  }
}
