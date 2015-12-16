package com.kickstarter.libs;

import android.support.annotation.NonNull;

import java.util.List;

public class Config {
  String countryCode;
  List<LaunchedCountry> launchedCountries;

  public static class Builder {
    private final String countryCode;
    private final List<LaunchedCountry> launchedCountries;

    public Builder(@NonNull final String countryCode, @NonNull final List<LaunchedCountry> launchedCountries) {
      this.countryCode = countryCode;
      this.launchedCountries = launchedCountries;
    }

    public Config build() {
      return new Config(this);
    }
  }

  private Config(@NonNull final Builder builder) {
    this.countryCode = builder.countryCode;
    this.launchedCountries = builder.launchedCountries;

  }

  public String countryCode() {
    return countryCode;
  }

  List<LaunchedCountry> launchedCountries() {
    return launchedCountries;
  }

  /**
   * A currency needs a code if its symbol is ambiguous, e.g. `$` is used for currencies such as USD, CAD, AUD.
   */
  public boolean currencyNeedsCode(final @NonNull String currencySymbol) {
    for (final LaunchedCountry country : launchedCountries()) {
      if (country.currencySymbol().equals(currencySymbol)) {
        return country.trailingCode();
      }
    }
    return true;
  }

  public static class LaunchedCountry {
    String name;
    String currencyCode;
    String currencySymbol;
    Boolean trailingCode;

    public LaunchedCountry(@NonNull final String name, @NonNull final String currencyCode,
      @NonNull final String currencySymbol, final boolean trailingCode) {
      this.name = name;
      this.currencyCode = currencyCode;
      this.currencySymbol = currencySymbol;
      this.trailingCode = trailingCode;
    }

    public String name() {
      return name;
    }

    public String currencyCode() {
      return currencyCode;
    }

    public String currencySymbol() {
      return currencySymbol;
    }

    public Boolean trailingCode() {
      return trailingCode;
    }
  }
}
