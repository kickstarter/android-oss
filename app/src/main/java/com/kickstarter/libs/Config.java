package com.kickstarter.libs;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class Config implements Parcelable {
  public abstract String countryCode();
  public abstract @Nullable Map<String, Boolean> features();
  public abstract List<LaunchedCountry> launchedCountries();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder countryCode(String __);
    public abstract Builder features(Map<String, Boolean> __);
    public abstract Builder launchedCountries(List<LaunchedCountry> __);
    public abstract Config build();
  }

  @AutoGson
  @AutoParcel
  public abstract static class LaunchedCountry implements Parcelable {
    public abstract String name();
    public abstract String currencyCode();
    public abstract String currencySymbol();
    public abstract Boolean trailingCode();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder name(String __);
      public abstract Builder currencyCode(String __);
      public abstract Builder currencySymbol(String __);
      public abstract Builder trailingCode(Boolean __);
      public abstract LaunchedCountry build();
    }

    public static Builder builder() {
      return new AutoParcel_Config_LaunchedCountry.Builder();
    }

    public abstract Builder toBuilder();
  }

  public static Builder builder() {
    return new AutoParcel_Config.Builder()
      .features(Collections.emptyMap());
  }

  public abstract Builder toBuilder();

  /**
   * A currency needs a code if its symbol is ambiguous, e.g. `$` is used for currencies such as USD, CAD, AUD.
   */
  public boolean currencyNeedsCode(final @NonNull String currencySymbol) {
    for (final LaunchedCountry country : launchedCountries()) {
      if (country.currencySymbol().equals(currencySymbol)) {
        return country.trailingCode();
      }
    }

    // Unlaunched country, default to showing the code.
    return true;
  }
}
