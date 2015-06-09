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

  static Config createConfig(final String country_code) {
    final List<Config.LaunchedCountry> launched_countries = new ArrayList<Config.LaunchedCountry>();
    launched_countries.add(new Config.LaunchedCountry("US", "USD", "$", true));
    launched_countries.add(new Config.LaunchedCountry("GB", "GBP", "£", false));
    launched_countries.add(new Config.LaunchedCountry("CA", "CAD", "$", true));
    return new Config.Builder(country_code, launched_countries).build();

  }

  static Money createMoney(final String country_code) {
    return new Money(mockConfigLoader(createConfig(country_code)));
  }
}
