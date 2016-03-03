package com.kickstarter.libs.preferences;

public interface BooleanPreferenceType {
  /**
   * Get the current value of the preference.
   */
  boolean get();

  /**
   * Returns whether a value has been explicitly set for the preference.
   */
  boolean isSet();

  /**
   * Set the preference to a value.
   */
  void set(boolean value);

  /**
   * Delete the currently stored preference.
   */
  void delete();
}
