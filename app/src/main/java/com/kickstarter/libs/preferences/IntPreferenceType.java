package com.kickstarter.libs.preferences;

public interface IntPreferenceType {
  int get();
  boolean isSet();
  void set(int value);
  void delete();
}
