package com.kickstarter.libs;

import java.util.List;

public class Config {
  String countryCode;
  List<LaunchedCountry> launchedCountries;

  public static class Builder {
    private final String countryCode;
    private final List<LaunchedCountry> launchedCountries;

    public Builder(final String countryCode, final List<LaunchedCountry> launchedCountries) {
      this.countryCode = countryCode;
      this.launchedCountries = launchedCountries;
    }

    public Config build() {
      return new Config(this);
    }
  }

  private Config(final Builder builder) {
    this.countryCode = builder.countryCode;
    this.launchedCountries = builder.launchedCountries;

  }

  public String countryCode() {
    return countryCode;
  }

  List<LaunchedCountry> launchedCountries() {
    return launchedCountries;
  }

  public boolean currencyIsDuplicatedWithSymbol(final String symbol, final String code) {
    // TODO: Cache the results
    int count = 0;
    for (LaunchedCountry country : launchedCountries()) {
      if (country.currencySymbol().equals(symbol) && !country.currencyCode().equals(code)) {
        ++count;
      }
    }

    return count >= 1;
  }

  public static class LaunchedCountry {
    String name;
    String currencyCode;
    String currencySymbol;
    Boolean trailingCode;

    public LaunchedCountry(final String name,
      final String currencyCode,
      final String currencySymbol,
      final boolean trailingCode) {
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
