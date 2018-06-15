/*
 * The MIT License
 *
 * Copyright (c) 2010 Xtreme Labs and Pivotal Labs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * ***
 *
 * Original: https://github.com/robolectric/robolectric/blob/b4d324353c92496e8eb65792b17da695ff78b22f/robolectric/src/main/java/org/robolectric/RobolectricGradleTestRunner.java
 * Added suggested fix by Gabriel Moreira: https://github.com/robolectric/robolectric/issues/1430#issuecomment-102546421
 */

package com.kickstarter;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

public class KSRobolectricGradleTestRunner extends RobolectricTestRunner {
  public static final int DEFAULT_SDK = 21;
  private static final String BUILD_OUTPUT = "build/intermediates";

  public KSRobolectricGradleTestRunner(final Class<?> testClass) throws InitializationError {
    super(testClass);
  }

//  @Override
//  protected AndroidManifest getAppManifest(final Config config) {
//    if (config.constants() == Void.class) {
//      Logger.error("Field 'constants' not specified in @Config annotation");
//      Logger.error("This is required when using RobolectricGradleTestRunner!");
//      throw new RuntimeException("No 'constants' field in @Config annotation!");
//    }
//
//    final String type = getType(config);
//    final String flavor = getFlavor(config);
//    final String packageName = getPackageName(config);
//
//    final FileFsFile res;
//    final FileFsFile assets;
//    final FileFsFile manifest;
//
//    // res/merged added in Android Gradle plugin 1.3-beta1
//    if (FileFsFile.from(BUILD_OUTPUT, "res", "merged").exists()) {
//      res = FileFsFile.from(BUILD_OUTPUT, "res", "merged", flavor, type);
//    } else if (FileFsFile.from(BUILD_OUTPUT, "res").exists()) {
//      res = FileFsFile.from(BUILD_OUTPUT, "res", flavor, type);
//    } else {
//      res = FileFsFile.from(BUILD_OUTPUT, "bundles", flavor, type, "res");
//    }
//
//    if (FileFsFile.from(BUILD_OUTPUT, "assets").exists()) {
//      assets = FileFsFile.from(BUILD_OUTPUT, "assets", flavor, type);
//    } else {
//      assets = FileFsFile.from(BUILD_OUTPUT, "bundles", flavor, type, "assets");
//    }
//
//    if (FileFsFile.from(BUILD_OUTPUT, "manifests").exists()) {
//      manifest = FileFsFile.from(BUILD_OUTPUT, "manifests", "full", flavor, type, "AndroidManifest.xml");
//    } else {
//      manifest = FileFsFile.from(BUILD_OUTPUT, "bundles", flavor, type, "AndroidManifest.xml");
//    }
//
//    Logger.debug("Robolectric assets directory: " + assets.getPath());
//    Logger.debug("   Robolectric res directory: " + res.getPath());
//    Logger.debug("   Robolectric manifest path: " + manifest.getPath());
//    Logger.debug("    Robolectric package name: " + packageName);
//
//    return new AndroidManifest(manifest, res, assets) {
//      @Override
//      public String getRClassName() throws Exception {
//        return com.kickstarter.R.class.getName();
//      }
//    };
//  }

  private static String getType(final Config config) {
    try {
      return ReflectionHelpers.getStaticField(config.constants(), "BUILD_TYPE");
    } catch (Throwable e) {
      return null;
    }
  }

  private static String getFlavor(final Config config) {
    try {
      return ReflectionHelpers.getStaticField(config.constants(), "FLAVOR");
    } catch (Throwable e) {
      return null;
    }
  }

  private static String getPackageName(final Config config) {
    try {
      final String packageName = config.packageName();
      if (packageName != null && !packageName.isEmpty()) {
        return packageName;
      } else {
        return ReflectionHelpers.getStaticField(config.constants(), "APPLICATION_ID");
      }
    } catch (Throwable e) {
      return null;
    }
  }
}
