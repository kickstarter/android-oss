package com.kickstarter;

import org.joda.time.DateTimeZone;
import org.joda.time.tz.UTCProvider;

public class TestKSApplication extends KSApplication {

  @Override
  public void onCreate() {
    super.onCreate();
    DateTimeZone.setProvider(new UTCProvider());
  }

  @Override
  public boolean isInUnitTests() {
    return true;
  }
}
