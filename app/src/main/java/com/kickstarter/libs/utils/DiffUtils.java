package com.kickstarter.libs.utils;

import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import auto.parcel.AutoParcel;
import rx.functions.Func2;

public final class DiffUtils {
  private DiffUtils() {}

  @AutoParcel
  public abstract static class Diff implements Parcelable {
    public abstract @NonNull List<Integer> insertions();
    public abstract @NonNull List<Integer> deletions();
    public abstract @NonNull List<Integer> updates();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder insertions(List<Integer> __);
      public abstract Builder deletions(List<Integer> __);
      public abstract Builder updates(List<Integer> __);
      public abstract Diff build();
    }

    public static Builder builder() {
      return new AutoParcel_DiffUtils_Diff.Builder()
        .insertions(new ArrayList<>())
        .deletions(new ArrayList<>())
        .updates(new ArrayList<>());
    }

    public abstract Builder toBuilder();
  }

  public static @NonNull <T> Diff diff(final @NonNull List<T> oldItems, final @NonNull List<T> newItems) {
    return DiffUtils.diff(oldItems, newItems, Object::equals);
  }

  public static @NonNull <T> Diff diff(final @NonNull List<T> oldItems, final @NonNull List<T> newItems,
    final @NonNull Func2<T, T, Boolean> matches) {

    final List<Integer> insertions = new ArrayList<>();
    final List<Integer> deletions = new ArrayList<>();
    final List<Integer> updates = new ArrayList<>();

    final List<T> missingItems = ListUtils.difference(oldItems, newItems, matches);
    for (final T item : missingItems) {
      deletions.add(oldItems.indexOf(item));
    }

    final List<T> addedItems = ListUtils.difference(newItems, oldItems, matches);
    for (final T item : addedItems) {
      insertions.add(newItems.indexOf(item));
    }

    final List<T> maybeUpdatedItems = ListUtils.intersection(oldItems, newItems, matches);
    for (final T maybeUpdatedItem : maybeUpdatedItems) {
      final T oldItem = ListUtils.find(oldItems, maybeUpdatedItem, matches);
      final T newItem = ListUtils.find(newItems, maybeUpdatedItem, matches);

      if (!oldItem.equals(newItem)) {
        updates.add(ListUtils.indexOf(oldItems, oldItem));
      }
    }

    return Diff.builder()
      .insertions(insertions)
      .deletions(deletions)
      .updates(updates)
      .build();
  }
}
