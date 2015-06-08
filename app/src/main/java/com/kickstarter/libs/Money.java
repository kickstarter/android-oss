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
    if (!excludeCurrencyCode) {
      boolean currencyIsDupe = currencyIsDuplicatedWithSymbol(currencySymbol, currencyCode);
      boolean userIsUS = configLoader.current().countryCode().equals("US");
      boolean projectIsUS = country.equals("US");

      if ((currencyIsDupe && !userIsUS) || (currencyIsDupe && !projectIsUS)) {
        useCurrencyCode = true;
      }
    }

    StringBuilder builder = new StringBuilder();
    builder.append(currencySymbol.isEmpty() ? "$" : currencySymbol);
    builder.append(formattedNumber(amount));
    if (useCurrencyCode) {
      builder.append(" " + currencyCode);
    }
    return builder.toString();
  }

  protected boolean currencyIsDuplicatedWithSymbol(final String symbol, final String code) {
    final Config config = configLoader.current();

    // TODO: Cache the results?
    int dupes = 0;
    for (Config.LaunchedCountry country : config.launchedCountries()) {
      if (country.currencySymbol().equals(symbol) && !country.currencyCode().equals(code)) {
        ++dupes;
      }
    }

    return dupes > 1; // TODO: Why is this > 1 and not >= 1? Should explain what the method is doing a little more.
  }
}
