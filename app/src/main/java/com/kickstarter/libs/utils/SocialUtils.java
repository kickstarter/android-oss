package com.kickstarter.libs.utils;

import android.content.Context;
import android.text.TextUtils;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.models.User;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;

public final class SocialUtils {
  private SocialUtils() {}

  public static String friendBackingActivityTitle(final @NonNull Context context, final @NonNull String friendName,
    final long categoryId, final @NonNull KSString ksString) {
    final String str;

    switch ((int) categoryId) {
      case 1:
        str = ksString.format(
          context.getString(R.string.activity_friend_backed_art_project),
          "friend_name",
          friendName
        );
        break;
      case 3:
        str = ksString.format(
          context.getString(R.string.activity_friend_backed_comics_project),
          "friend_name",
          friendName
        );
        break;
      case 26:
        str = ksString.format(
          context.getString(R.string.activity_friend_backed_crafts_project),
          "friend_name",
          friendName
        );
        break;
      case 6:
        str = ksString.format(
          context.getString(R.string.activity_friend_backed_dance_project),
          "friend_name",
          friendName
        );
        break;
      case 7:
        str = ksString.format(
          context.getString(R.string.activity_friend_backed_design_project),
          "friend_name",
          friendName
        );
        break;
      case 9:
        str = ksString.format(
          context.getString(R.string.activity_friend_backed_fashion_project),
          "friend_name",
          friendName
        );
        break;
      case 11:
        str = ksString.format(
          context.getString(R.string.activity_friend_backed_film_project),
          "friend_name",
          friendName
        );
        break;
      case 10:
        str = ksString.format(
          context.getString(R.string.activity_friend_backed_food_project),
          "friend_name",
          friendName
        );
        break;
      case 12:
        str = ksString.format(
          context.getString(R.string.activity_friend_backed_games_project),
          "friend_name",
          friendName
        );
        break;
      case 13:
        str = ksString.format(
          context.getString(R.string.activity_friend_backed_journalism_project),
          "friend_name",
          friendName
        );
        break;
      case 14:
        str = ksString.format(
          context.getString(R.string.activity_friend_backed_music_project),
          "friend_name",
          friendName
        );
        break;
      case 15:
        str = ksString.format(
          context.getString(R.string.activity_friend_backed_photography_project),
          "friend_name",
          friendName
        );
        break;
      case 18:
        str = ksString.format(
          context.getString(R.string.activity_friend_backed_publishing_project),
          "friend_name",
          friendName
        );
        break;
      case 16:
        str = ksString.format(
          context.getString(R.string.activity_friend_backed_tech_project),
          "friend_name",
          friendName
        );
        break;
      case 17:
        str = ksString.format(
          context.getString(R.string.activity_friend_backed_theater_project),
          "friend_name",
          friendName
        );
        break;
      default: str = "";
    }

    return str;
  }

  /**
   * Returns a namepile for a list of friends.
   */
  public static @NonNull String projectCardFriendNamepile(final @NonNull Context context, final @NonNull List<User> friends, final @NonNull KSString ksString) {
    final String friendName;
    if (friends.size() < 3) {
      friendName = friends.size() >= 1 ? friends.get(0).name() : "";
    } else {
      //if there are 3 friends, we should combine the first 2 friend names using the delimiter
      //and if there are more than 3, we should combine all of them
      final String delimiter = context.getString(R.string.project_social_friends_separator).trim() + " ";
      final String joinedFriends = TextUtils.join(delimiter, Arrays.asList(friends.get(0).name(), friends.get(1).name()));
      friendName = friends.size() == 3 ? joinedFriends.concat(delimiter.trim()) : joinedFriends;
    }

    final String secondFriendName;
    if (friends.size() >= 2) {
      secondFriendName = friends.size() == 2 ? friends.get(1).name() : friends.get(2).name();
    } else {
      secondFriendName = "";
    }
    final String remainingCount = NumberUtils.format(Math.max(0, friends.size() - 3));

    //since we show 3 friends at max, the remaining number needs to be adjusted
    return ksString.format("discovery_baseball_card_social_friends_are_backers", friends.size() == 3 ? friends.size() - 1 : friends.size(),
      "friend_name", friendName,
      "second_friend_name", secondFriendName,
      "remaining_count", remainingCount
    );
  }
}
