package com.kickstarter.models;

import android.net.Uri;
import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import org.joda.time.DateTime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.Nullable;
import androidx.annotation.StringDef;
import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class Activity implements Parcelable {
  public abstract @Category String category();
  public abstract DateTime createdAt();
  public abstract long id();
  public abstract @Nullable Project project();
  public abstract @Nullable Update update();
  public abstract DateTime updatedAt();
  public abstract @Nullable User user();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder category(@Category String __);
    public abstract Builder createdAt(DateTime __);
    public abstract Builder id(long __);
    public abstract Builder project(Project __);
    public abstract Builder update(Update __);
    public abstract Builder updatedAt(DateTime __);
    public abstract Builder user(User __);
    public abstract Activity build();
  }

  public static Builder builder() {
    return new AutoParcel_Activity.Builder();
  }

  public abstract Builder toBuilder();

  public static final String CATEGORY_WATCH               = "watch";
  public static final String CATEGORY_UPDATE              = "update";
  public static final String CATEGORY_COMMENT_PROJECT     = "comment-project";
  public static final String CATEGORY_BACKING             = "backing";
  public static final String CATEGORY_COMMENT_POST        = "comment-post";
  public static final String CATEGORY_CANCELLATION        = "cancellation";
  public static final String CATEGORY_SUCCESS             = "success";
  public static final String CATEGORY_SUSPENSION          = "suspension";
  public static final String CATEGORY_LAUNCH              = "launch";
  public static final String CATEGORY_FAILURE             = "failure";
  public static final String CATEGORY_FUNDING             = "funding";
  public static final String CATEGORY_BACKING_CANCELED    = "backing-canceled";
  public static final String CATEGORY_BACKING_DROPPED     = "backing-dropped";
  public static final String CATEGORY_BACKING_REWARD      = "backing-reward";
  public static final String CATEGORY_BACKING_AMOUNT      = "backing-amount";
  public static final String CATEGORY_COMMENT_PROPOSAL    = "comment-proposal";
  public static final String CATEGORY_FOLLOW              = "follow";

  @Retention(RetentionPolicy.SOURCE)
  @StringDef({CATEGORY_WATCH, CATEGORY_UPDATE, CATEGORY_COMMENT_PROJECT, CATEGORY_BACKING,
    CATEGORY_COMMENT_POST, CATEGORY_CANCELLATION, CATEGORY_SUCCESS, CATEGORY_SUSPENSION, CATEGORY_LAUNCH,
    CATEGORY_FAILURE, CATEGORY_FUNDING, CATEGORY_BACKING_CANCELED, CATEGORY_BACKING_DROPPED, CATEGORY_BACKING_REWARD,
    CATEGORY_BACKING_AMOUNT, CATEGORY_COMMENT_PROPOSAL, CATEGORY_FOLLOW})
  public @interface Category {}

  public String projectUpdateUrl() {
    return Uri.parse(project().webProjectUrl()).buildUpon()
      .appendEncodedPath("posts")
      .appendPath(Long.toString(update().id()))
      .toString();
  }
}
