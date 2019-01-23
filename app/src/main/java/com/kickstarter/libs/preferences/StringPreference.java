/*
 * Copyright 2014 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ***
 *
 * Original: https://github.com/JakeWharton/u2020/blob/7363d27ee0356e24dcbd00dc6926d993ee56d6e2/src/main/java/com/jakewharton/u2020/data/prefs/StringPreference.java
 * Modifications: Some modifiers and annotations have been added by Kickstarter.
 */

package com.kickstarter.libs.preferences;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class StringPreference implements StringPreferenceType {
  private final SharedPreferences sharedPreferences;
  private final String key;
  private final String defaultValue;

  public StringPreference(final @NonNull SharedPreferences sharedPreferences, final @NonNull String key) {
    this(sharedPreferences, key, null);
  }

  public StringPreference(final @NonNull SharedPreferences sharedPreferences, final @NonNull String key,
    final @Nullable String defaultValue) {
    this.sharedPreferences = sharedPreferences;
    this.key = key;
    this.defaultValue = defaultValue;
  }

  @Override
  public String get() {
    return this.sharedPreferences.getString(this.key, this.defaultValue);
  }

  @Override
  public boolean isSet() {
    return this.sharedPreferences.contains(this.key);
  }

  @Override
  public void set(final @NonNull String value) {
    this.sharedPreferences.edit().putString(this.key, value).apply();
  }

  @Override
  public void delete() {
    this.sharedPreferences.edit().remove(this.key).apply();
  }
}
