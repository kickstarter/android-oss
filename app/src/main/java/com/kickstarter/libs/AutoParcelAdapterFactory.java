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
 * Original: https://gist.github.com/jakewharton/0d67d01badcee0ae7bc9
 * Modifications: Some modifiers and annotations have been added by Kickstarter.
 */

package com.kickstarter.libs;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.kickstarter.libs.qualifiers.AutoGson;

import androidx.annotation.NonNull;

public final class AutoParcelAdapterFactory implements TypeAdapterFactory {
  @SuppressWarnings("unchecked")
  @Override
  public <T> TypeAdapter<T> create(final @NonNull Gson gson, final @NonNull TypeToken<T> type) {
    final Class<? super T> rawType = type.getRawType();
    if (!rawType.isAnnotationPresent(AutoGson.class)) {
      return null;
    }

    final String packageName = rawType.getPackage().getName();
    final String className = rawType.getName().substring(packageName.length() + 1).replace('$', '_');
    final String autoParcelName = packageName + ".AutoParcel_" + className;

    try {
      final Class<?> autoParcelType = Class.forName(autoParcelName);
      return (TypeAdapter<T>) gson.getAdapter(autoParcelType);
    } catch (final ClassNotFoundException e) {
      throw new RuntimeException("Could not load AutoParcel type " + autoParcelName, e);
    }
  }
}
