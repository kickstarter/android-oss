package com.kickstarter.libs;

import java.util.List;

public class Config {
  String country_code;
  List<LaunchedCountry> launched_countries;

  public String countryCode() {
    return country_code;
  }

  List<LaunchedCountry> launchedCountries() {
    return launched_countries;
  }

  public static class LaunchedCountry {
    String name;
    String currency_code;
    String currency_symbol;
    Boolean trailing_code;

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
