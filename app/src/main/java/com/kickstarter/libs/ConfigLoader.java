package com.kickstarter.libs;

import android.content.res.AssetManager;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

public class ConfigLoader {
  private final AssetManager assetManager;
  private Config config;
  private final Gson gson = new Gson();

  private final static String ASSET_PATH = "json/server-config.json";

  public ConfigLoader(final AssetManager assetManager) {
    this.assetManager = assetManager;

    final String json = loadJsonFromAssets();
    this.config = deserializeJson(json);
  }

  public Config current() {
    return this.config;
  }

  private Config deserializeJson(final String json) {
    return gson.fromJson(json, Config.class);
  }

  private String loadJsonFromAssets() {
    try {
      final InputStream input;
      input = assetManager.open(ASSET_PATH);
      final byte[] buffer = new byte[input.available()];
      input.read(buffer);
      input.close();

      return new String(buffer);
    } catch (final IOException e) {
      Timber.e(e.getMessage());
      // TODO: This should probably be fatal?
    }

    return "{}";
  }
}
