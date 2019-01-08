package com.kickstarter.ui.adapters.data;

import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import auto.parcel.AutoParcel;

@AutoParcel
public abstract class CommentsData {
  public abstract @NonNull Project project();
  public abstract @Nullable List<Comment> comments();
  public abstract @Nullable User user();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder project(Project __);
    public abstract Builder comments(List<Comment> __);
    public abstract Builder user(User __);
    public abstract CommentsData build();
  }

  public static Builder builder() {
    return new AutoParcel_CommentsData.Builder();
  }

  public abstract Builder toBuilder();

  public static @NonNull CommentsData deriveData(final @NonNull Project project,
    final @Nullable List<Comment> comments, final @Nullable User user) {

    return CommentsData.builder()
      .project(project)
      .comments(comments)
      .user(user)
      .build();
  }
}
