package com.kickstarter.libs;

public class CurrencyOptions {
  final String country;
  final String currency_symbol;
  final String currency_code;

  public CurrencyOptions(final String country, final String currency_symbol, final String currency_code) {
    this.country = country;
    this.currency_symbol = currency_symbol;
    this.currency_code = currency_code;
  }

  public String country() {
    return country;
  }

  public String currencySymbol() {
    return currency_symbol;
  }

  public String currencyCode() {
    return currency_code;
  }
}
