package com.kickstarter;

import com.kickstarter.libs.Config;
import com.kickstarter.libs.ConfigLoader;
import com.kickstarter.libs.Money;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtils {
  static ConfigLoader mockConfigLoader(final Config config) {
    ConfigLoader configLoader = mock(ConfigLoader.class);
    when(configLoader.current()).thenReturn(config);
    return configLoader;
  }

  static Config createConfig(final String countryCode) {
    final List<Config.LaunchedCountry> launchedCountries = new ArrayList<Config.LaunchedCountry>();
    launchedCountries.add(new Config.LaunchedCountry("US", "USD", "$", true));
    launchedCountries.add(new Config.LaunchedCountry("GB", "GBP", "£", false));
    launchedCountries.add(new Config.LaunchedCountry("CA", "CAD", "$", true));
    return new Config.Builder(countryCode, launchedCountries).build();

  }

  static Money createMoney(final String countryCode) {
    return new Money(mockConfigLoader(createConfig(countryCode)));
  }
}
