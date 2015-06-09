package com.kickstarter;

import com.kickstarter.libs.Config;
import com.kickstarter.libs.ConfigLoader;

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

  static Config config(final String country_code) {
    final List<Config.LaunchedCountry> launched_countries = new ArrayList<Config.LaunchedCountry>();
    launched_countries.add(new Config.LaunchedCountry("US", "USD", "$", true));
    launched_countries.add(new Config.LaunchedCountry("GB", "GBP", "£", false));
    launched_countries.add(new Config.LaunchedCountry("CA", "CAD", "$", true));
    return new Config.Builder(country_code, launched_countries).build();

  }
}
