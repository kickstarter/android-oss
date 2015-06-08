package com.kickstarter;

import com.kickstarter.libs.Config;
import com.kickstarter.libs.ConfigLoader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtils {
  static ConfigLoader mockConfigLoader() {
    ConfigLoader configLoader = mock(ConfigLoader.class);
    Config config = mockConfig();
    when(configLoader.current()).thenReturn(config);
    return configLoader;
  }

  static Config mockConfig() {
    Config config = mock(Config.class);
    when(config.countryCode()).thenReturn("US");
    return config;
  }
}
