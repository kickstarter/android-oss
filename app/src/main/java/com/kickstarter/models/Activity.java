package com.kickstarter.models;

import android.text.style.UpdateAppearance;

import com.google.common.base.Enums;

public class Activity {
  String category = null;
  Integer id = null;
  Project project = null;
  Update update = null;
  User user = null;

  public enum Category {
    UNKNOWN,
    WATCH,
    RESUME,
    UPDATE,
    COMMENT_PROJECT,
    BACKING,
    COMMENT_POST,
    CANCELLATION,
    SUCCESS,
    SUSPENSION,
    LAUNCH,
    FAILURE,
    FUNDING,
    BACKING_CANCELED,
    BACKING_DROPPED,
    BACKING_REWARD,
    BACKING_AMOUNT,
    COMMENT_PROPOSAL,
    FOLLOW;
  }

  public Category category() {
    return Enums.getIfPresent(Category.class, category.toUpperCase()).or(Category.UNKNOWN);
  }

  public Integer id() {
    return this.id;
  }

  public Project project() {
    return project;
  }

  public Update update() {
    return update;
  }

  public User user() {
    return user;
  }
}
