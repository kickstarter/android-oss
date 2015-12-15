package com.kickstarter.libs;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.kickstarter.libs.qualifiers.AutoGson;

import java.util.List;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class Config implements Parcelable {
  public abstract String countryCode();
  public abstract List<LaunchedCountry> launchedCountries();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder countryCode(String __);
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
    return new AutoParcel_Config.Builder();
  }

  public abstract Builder toBuilder();

  public boolean currencyIsDuplicatedWithSymbol(@NonNull final String symbol, @NonNull final String code) {
    // TODO: Cache the results
    int count = 0;
    for (final LaunchedCountry country : launchedCountries()) {
      if (country.currencySymbol().equals(symbol) && !country.currencyCode().equals(code)) {
        ++count;
      }
    }

    return count >= 1;
  }
}
