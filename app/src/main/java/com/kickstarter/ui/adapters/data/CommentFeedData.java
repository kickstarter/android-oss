package com.kickstarter.ui.adapters.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;

import java.util.List;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class CommentFeedData {
  public abstract @NonNull Project project();
  public abstract @Nullable List<Comment> comments();
  public abstract @Nullable User user();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder project(Project __);
    public abstract Builder comments(List<Comment> __);
    public abstract Builder user(User __);
    public abstract CommentFeedData build();
  }

  public static Builder builder() {
    return new AutoParcel_CommentFeedData.Builder();
  }

  public abstract Builder toBuilder();
}
