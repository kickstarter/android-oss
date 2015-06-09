package com.kickstarter.libs;

import java.util.List;

public class Config {
  String country_code;
  List<LaunchedCountry> launched_countries;

  public static class Builder {
    private final String country_code;
    private final List<LaunchedCountry> launched_countries;

    public Builder(final String country_code, final List<LaunchedCountry> launched_countries) {
      this.country_code = country_code;
      this.launched_countries = launched_countries;
    }

    public Config build() {
      return new Config(this);
    }
  }

  private Config(final Builder builder) {
    this.country_code = builder.country_code;
    this.launched_countries = builder.launched_countries;

  }

  public String countryCode() {
    return country_code;
  }

  List<LaunchedCountry> launchedCountries() {
    return launched_countries;
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
    String currency_code;
    String currency_symbol;
    Boolean trailing_code;

    public LaunchedCountry(final String name,
      final String currency_code,
      final String currency_symbol,
      final boolean trailing_code) {
      this.name = name;
      this.currency_code = currency_code;
      this.currency_symbol = currency_symbol;
      this.trailing_code = trailing_code;
    }

    public String name() {
      return name;
    }

    public String currencyCode() {
      return currency_code;
    }

    public String currencySymbol() {
      return currency_symbol;
    }

    public Boolean trailingCode() {
      return trailing_code;
    }
  }
}
