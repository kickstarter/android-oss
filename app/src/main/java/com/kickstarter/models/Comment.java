package com.kickstarter.models;

import android.os.Parcelable;
import androidx.annotation.Nullable;
import com.kickstarter.libs.qualifiers.AutoGson;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Objects;
import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class Comment implements Parcelable, Relay{
  public abstract User author();
  public abstract String body();
  public abstract DateTime createdAt();
  public abstract Boolean deleted();
  public abstract String cursor();
  public abstract Integer repliesCount();
  public abstract List<String> authorBadges();
  public abstract long id();
  public abstract @Nullable long parentId();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder author(User __);
    public abstract Builder cursor(String __);
    public abstract Builder authorBadges(List<String> __);
    public abstract Builder repliesCount(Integer __);
    public abstract Builder body(String __);
    public abstract Builder createdAt(DateTime __);
    public abstract Builder deleted(Boolean __);
    public abstract Builder id(long __);
    public abstract Builder parentId(long __);
    public abstract Comment build();
  }

  public static Builder builder() {
    return new AutoParcel_Comment.Builder();
  }

  public abstract Builder toBuilder();

  @Override
  public boolean equals(final @Nullable Object obj) {
    boolean equals = super.equals(obj);

    if (obj instanceof Comment) {
      final Comment other = (Comment) obj;
      equals = Objects.equals(this.id(), other.id()) &&
              Objects.equals(this.body(), other.body()) &&
              Objects.equals(this.author(), other.author()) &&
              Objects.equals(this.cursor(), other.cursor()) &&
              Objects.equals(this.deleted(), other.deleted()) &&
              Objects.equals(this.createdAt(), other.createdAt()) &&
              Objects.equals(this.parentId(), other.parentId());
    }

    return equals;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
