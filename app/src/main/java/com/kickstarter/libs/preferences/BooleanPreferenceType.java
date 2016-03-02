package com.kickstarter.libs.preferences;

public interface BooleanPreferenceType {
  boolean get();
  boolean isSet();
  void set(boolean value);
  void delete();
}
