package com.kickstarter.libs;

public class CurrencyOptions {
  final String country;
  final String currencySymbol;
  final String currencyCode;

  public CurrencyOptions(final String country, final String currencySymbol, final String currencyCode) {
    this.country = country;
    this.currencySymbol = currencySymbol;
    this.currencyCode = currencyCode;
  }

  public String country() {
    return country;
  }

  public String currencySymbol() {
    return currencySymbol;
  }

  public String currencyCode() {
    return currencyCode;
  }
}
