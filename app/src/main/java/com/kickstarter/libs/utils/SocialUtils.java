package com.kickstarter.libs.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.models.User;

import java.util.List;

public final class SocialUtils {
  private SocialUtils() {}

  public static String friendBackingActivityTitle(final @NonNull Context context, final @NonNull String friendName,
    final long categoryId) {
    final String str;

    switch ((int) categoryId) {
      case 1: str = context.getString(R.string.___friend_name_backed_art_project, friendName); break;
      case 3: str = context.getString(R.string.___friend_name_backed_comics_project, friendName); break;
      case 26: str = context.getString(R.string.___friend_name_backed_crafts_project, friendName); break;
      case 6: str = context.getString(R.string.___friend_name_backed_dance_project, friendName); break;
      case 7: str = context.getString(R.string.___friend_name_backed_design_project, friendName); break;
      case 9: str = context.getString(R.string.___friend_name_backed_fashion_project, friendName); break;
      case 11: str = context.getString(R.string.___friend_name_backed_film_project, friendName); break;
      case 10: str = context.getString(R.string.___friend_name_backed_food_project, friendName); break;
      case 12: str = context.getString(R.string.___friend_name_backed_games_project, friendName); break;
      case 13: str = context.getString(R.string.___friend_name_backed_journalism_project, friendName); break;
      case 14: str = context.getString(R.string.___friend_name_backed_music_project, friendName); break;
      case 15: str = context.getString(R.string.___friend_name_backed_photography_project, friendName); break;
      case 18: str = context.getString(R.string.___friend_name_backed_publishing_project, friendName); break;
      case 16: str = context.getString(R.string.___friend_name_backed_technology_project, friendName); break;
      case 17: str = context.getString(R.string.___friend_name_backed_theater_project, friendName); break;
      default: str = "";
    }

    return str;
  }

  /**
   * Returns a namepile for a list of friends.
   */
  public static @NonNull String projectCardFriendNamepile(final @NonNull List<User> friends, final @NonNull KSString ksString) {
    final String friendName = (friends.size() >= 1 ? friends.get(0).name() : "");
    final String secondFriendName = (friends.size() >= 2 ? friends.get(1).name() : "");
    final String remainingCount = Integer.toString(Math.max(0, friends.size() - 2));

    return ksString.format("discovery_baseball_card_social_friends_are_backers", friends.size(),
      "friend_name", friendName,
      "second_friend_name", secondFriendName,
      "remaining_count", remainingCount
    );
  }
}
