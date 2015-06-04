package com.kickstarter.libs;

import java.text.NumberFormat;
import java.util.Locale;

public class Money {
  final ConfigLoader configLoader;

  public Money(final ConfigLoader configLoader) {
    this.configLoader = configLoader;
  }

  public String formatted(final Float amount, final String country, final String currencySymbol, final String currencyCode) {
    return currencySymbol + NumberFormat.getInstance(Locale.getDefault()).format(amount) + " " + currencyCode;
  }
}
