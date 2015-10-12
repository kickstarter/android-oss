package com.kickstarter.libs;

import android.support.annotation.NonNull;

import java.text.NumberFormat;
import java.util.Locale;

public class Money {
  final ConfigLoader configLoader;

  public Money(@NonNull final ConfigLoader configLoader) {
    this.configLoader = configLoader;
  }

  public String formattedNumber(final Float number) {
    // TODO: Should use appropriate locale
    // TODO: Could take bool to abbreviate (e.g. 100k instead of 100,000)
    return NumberFormat.getInstance(Locale.getDefault())
      .format(Math.floor(number));
  }

  public String formattedCurrency(final float amount, @NonNull final CurrencyOptions currencyOptions) {
    return formattedCurrency(amount, currencyOptions, false);
  }

  public String formattedCurrency(final float amount, @NonNull final CurrencyOptions currencyOptions,
    final boolean excludeCurrencyCode) {
    return formattedCurrency(amount,
      currencyOptions.country(),
      currencyOptions.currencySymbol(),
      currencyOptions.currencyCode(),
      excludeCurrencyCode);
  }

  private String formattedCurrency(final float amount,
    @NonNull final String country,
    @NonNull final String currencySymbol,
    @NonNull final String currencyCode,
    final boolean excludeCurrencyCode) {

    boolean useCurrencyCode = false;
    if (!excludeCurrencyCode) {
      final Config config = configLoader.current();
      final boolean currencyIsDupe = config.currencyIsDuplicatedWithSymbol(currencySymbol, currencyCode);
      final boolean userIsUS = config.countryCode().equals("US");
      final boolean countryIsUS = country.equals("US");

      if ((currencyIsDupe && !userIsUS) || (currencyIsDupe && !countryIsUS)) {
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
}
