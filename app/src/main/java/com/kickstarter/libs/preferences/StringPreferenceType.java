package com.kickstarter.libs.preferences;

public interface StringPreferenceType {
  String get();
  boolean isSet();
  void set(String value);
  void delete();
}
