package com.kickstarter.libs;

import java.text.NumberFormat;
import java.util.Locale;

public class Money {
  final ConfigLoader configLoader;

  public Money(final ConfigLoader configLoader) {
    this.configLoader = configLoader;
  }

  public String formattedNumber(final Float number) {
    // TODO: Should use appropriate locale
    // TODO: Could take bool to abbreviate (e.g. 100k instead of 100,000)
    return NumberFormat.getInstance(Locale.getDefault()).format(number);
  }

  public String formattedCurrency(final Float amount, final String country, final String currencySymbol, final String currencyCode) {
    return formattedCurrency(amount, country, currencySymbol, currencyCode, false);
  }

  public String formattedCurrency(final Float amount,
    final String country,
    final String currencySymbol,
    final String currencyCode,
    final boolean excludeCurrencyCode) {

    boolean useCurrencyCode = false;
    StringBuilder builder = new StringBuilder();
    if (!excludeCurrencyCode) {
      final Config config = configLoader.current();
      final boolean currencyIsDupe = config.currencyIsDuplicatedWithSymbol(currencySymbol, currencyCode);
      final boolean userIsUS = config.countryCode().equals("US");
      final boolean countryIsUS = country.equals("US");

//      builder.append("currencyIsDupe: " + currencyIsDupe);
//      builder.append("userIsUS: " + userIsUS);
//      builder.append("countryIsUS: " + countryIsUS);
      if ((currencyIsDupe && !userIsUS) || (currencyIsDupe && !countryIsUS)) {
        useCurrencyCode = true;
      }
    }

    builder.append(currencySymbol.isEmpty() ? "$" : currencySymbol);
    builder.append(formattedNumber(amount));
    if (useCurrencyCode) {
      builder.append(" " + currencyCode);
    }
    return builder.toString();
  }
}
