package com.kickstarter;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FileFsFile;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;

/* Custom runner because of https://github.com/robolectric/robolectric/issues/1430
 * Mostly based on https://github.com/robolectric/robolectric/blob/b4d324353c92496e8eb65792b17da695ff78b22f/robolectric/src/main/java/org/robolectric/RobolectricGradleTestRunner.java
 * Modifications from https://github.com/robolectric/robolectric/issues/1430#issuecomment-102546421
 */
public class KSRobolectricGradleTestRunner extends RobolectricTestRunner {
  public static final int DEFAULT_SDK = 21;
  private static final String BUILD_OUTPUT = "build/intermediates";

  public KSRobolectricGradleTestRunner(Class<?> testClass) throws InitializationError {
    super(testClass);
  }

  @Override
  protected AndroidManifest getAppManifest(Config config) {
    if (config.constants() == Void.class) {
      Logger.error("Field 'constants' not specified in @Config annotation");
      Logger.error("This is required when using RobolectricGradleTestRunner!");
      throw new RuntimeException("No 'constants' field in @Config annotation!");
    }

    final String type = getType(config);
    final String flavor = getFlavor(config);
    final String packageName = getPackageName(config);

    final FileFsFile res;
    final FileFsFile assets;
    final FileFsFile manifest;

    // res/merged added in Android Gradle plugin 1.3-beta1
    if (FileFsFile.from(BUILD_OUTPUT, "res", "merged").exists()) {
      res = FileFsFile.from(BUILD_OUTPUT, "res", "merged", flavor, type);
    } else if (FileFsFile.from(BUILD_OUTPUT, "res").exists()) {
      res = FileFsFile.from(BUILD_OUTPUT, "res", flavor, type);
    } else {
      res = FileFsFile.from(BUILD_OUTPUT, "bundles", flavor, type, "res");
    }

    if (FileFsFile.from(BUILD_OUTPUT, "assets").exists()) {
      assets = FileFsFile.from(BUILD_OUTPUT, "assets", flavor, type);
    } else {
      assets = FileFsFile.from(BUILD_OUTPUT, "bundles", flavor, type, "assets");
    }

    if (FileFsFile.from(BUILD_OUTPUT, "manifests").exists()) {
      manifest = FileFsFile.from(BUILD_OUTPUT, "manifests", "full", flavor, type, "AndroidManifest.xml");
    } else {
      manifest = FileFsFile.from(BUILD_OUTPUT, "bundles", flavor, type, "AndroidManifest.xml");
    }

    Logger.debug("Robolectric assets directory: " + assets.getPath());
    Logger.debug("   Robolectric res directory: " + res.getPath());
    Logger.debug("   Robolectric manifest path: " + manifest.getPath());
    Logger.debug("    Robolectric package name: " + packageName);

    return new AndroidManifest(manifest, res, assets) {
      @Override
      public String getRClassName() throws Exception {
        return com.kickstarter.R.class.getName();
      }
    };
  }

  private static String getType(Config config) {
    try {
      return ReflectionHelpers.getStaticField(config.constants(), "BUILD_TYPE");
    } catch (Throwable e) {
      return null;
    }
  }

  private static String getFlavor(Config config) {
    try {
      // HACK: Lowercase name due to issues with Roboletric and multidimensional flavors, see:
      // https://github.com/robolectric/robolectric/issues/1936
      final String ret = ReflectionHelpers.getStaticField(config.constants(), "FLAVOR");
      return ret.toLowerCase();
    } catch (Throwable e) {
      return null;
    }
  }

  private static String getPackageName(Config config) {
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
