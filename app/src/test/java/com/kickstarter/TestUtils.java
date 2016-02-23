package com.kickstarter;

import com.kickstarter.libs.Config;
import com.kickstarter.libs.CurrentConfig;
import com.kickstarter.libs.KSCurrency;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TestUtils {
  private TestUtils() {}

  static CurrentConfig mockCurrentConfig(final Config config) {
    final CurrentConfig currentConfig = mock(CurrentConfig.class);
    when(currentConfig.getConfig()).thenReturn(config);
    return currentConfig;
  }

  static Config createConfig(final String countryCode) {

    final Config.LaunchedCountry US = Config.LaunchedCountry.builder()
      .name("US")
      .currencyCode("USD")
      .currencySymbol("$")
      .trailingCode(true)
      .build();
    final Config.LaunchedCountry GB = Config.LaunchedCountry.builder()
      .name("GB")
      .currencyCode("GBP")
      .currencySymbol("£")
      .trailingCode(false)
      .build();
    final Config.LaunchedCountry CA = Config.LaunchedCountry.builder()
      .name("CA")
      .currencyCode("CAD")
      .currencySymbol("$")
      .trailingCode(true)
      .build();

    final List<Config.LaunchedCountry> launchedCountries = new ArrayList<>();
    launchedCountries.add(US);
    launchedCountries.add(GB);
    launchedCountries.add(CA);

    return Config.builder()
      .countryCode(countryCode)
      .launchedCountries(launchedCountries)
      .build();

  }

  static KSCurrency createKSCurrency(final String countryCode) {
    return new KSCurrency(mockCurrentConfig(createConfig(countryCode)));
  }
}
