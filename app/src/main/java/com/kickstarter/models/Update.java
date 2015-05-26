package com.kickstarter.models;

import android.text.Html;

import org.joda.time.DateTime;

public class Update {
  String body = null;
  Integer comments_count = null;
  Boolean has_liked = null;
  Integer id = null;
  Integer likes_count = null;
  Integer project_id = null;
  DateTime published_at = null;
  Integer sequence = null;
  String title = null;
  User user = null;
  Integer update_id = null;
  DateTime updated_at = null;
  Boolean visible = null;

  private static final int TRUNCATED_BODY_LENGTH = 400;

  public String body() {
    return body;
  }

  public Integer commentsCount() {
    return comments_count;
  }

  public Boolean hasLiked() {
    return has_liked;
  }

  public Integer id() {
    return id;
  }

  public Integer likesCount() {
    return likes_count;
  }

  public Integer projectId() {
    return project_id;
  }

  public DateTime publishedAt() {
    return published_at;
  }

  public Integer sequence() {
    return sequence;
  }

  public String title() {
    return title;
  }

  public String truncatedBody() {
    String str = Html.fromHtml(body()).toString();
    if (str.length() > TRUNCATED_BODY_LENGTH) {
      str = str.substring(0, TRUNCATED_BODY_LENGTH - 1) + "\u2026";
    }

    return str;
  }

  public User user() {
    return user;
  }

  public Integer updateId() {
    return update_id;
  }

  public DateTime updatedAt() {
    return updated_at;
  }

  public Boolean visible() {
    return visible;
  }
}
