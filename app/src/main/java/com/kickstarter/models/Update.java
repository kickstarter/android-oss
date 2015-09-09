package com.kickstarter.models;

import android.text.Html;

import org.joda.time.DateTime;

public class Update {
  String body = null;
  Integer commentsCount = null;
  Boolean hasLiked = null;
  Integer id = null;
  Integer likesCount = null;
  Integer projectId = null;
  DateTime publishedAt = null;
  Integer sequence = null;
  String title = null;
  User user = null;
  Integer updateId = null;
  DateTime updatedAt = null;
  Boolean visible = null;

  private static final int TRUNCATED_BODY_LENGTH = 400;

  public String body() {
    return body;
  }

  public Integer commentsCount() {
    return commentsCount;
  }

  public Boolean hasLiked() {
    return hasLiked;
  }

  public Integer id() {
    return id;
  }

  public Integer likesCount() {
    return likesCount;
  }

  public Integer projectId() {
    return projectId;
  }

  public DateTime publishedAt() {
    return publishedAt;
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
    return updateId;
  }

  public DateTime updatedAt() {
    return updatedAt;
  }

  public Boolean visible() {
    return visible;
  }
}
