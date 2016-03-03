package com.kickstarter.libs;

import android.content.res.AssetManager;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.kickstarter.libs.preferences.StringPreferenceType;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ObjectUtils;

import java.io.IOException;
import java.io.InputStream;

import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

public class CurrentConfig {
  private final static String ASSET_PATH = "json/server-config.json";

  private final BehaviorSubject<Config> config = BehaviorSubject.create();

  public CurrentConfig(final @NonNull AssetManager assetManager,
    final @NonNull Gson gson,
    final @NonNull StringPreferenceType configPreference) {

    // Loads config from disk
    final Observable<Config> diskConfig = Observable.just(ASSET_PATH)
      .map(path -> configJSONString(path, assetManager))
      .map(json -> gson.fromJson(json, Config.class))
      .filter(ObjectUtils::isNotNull)
      .compose(Transformers.neverError())
      .subscribeOn(Schedulers.io());

    // Loads config from string preference
    final Observable<Config> prefConfig = Observable.just(configPreference)
      .map(StringPreferenceType::get)
      .map(json -> gson.fromJson(json, Config.class))
      .filter(ObjectUtils::isNotNull)
      .compose(Transformers.neverError())
      .subscribeOn(Schedulers.io());

    // Seed config observable with what's cached
    Observable.concat(prefConfig, diskConfig)
      .take(1)
      .subscribe(this.config::onNext);

    // Cache any new values to preferences
    this.config.skip(1)
      .filter(ObjectUtils::isNotNull)
      .subscribe(config -> configPreference.set(gson.toJson(config, Config.class)));
  }

  /**
   * Get an observable representation of the current config. Emits immediately with the freshes copy of the config
   * and then emits again for any fresher values.
   */
  public @NonNull Observable<Config> observable() {
    return this.config;
  }

  /**
   * @return The most recent config.
   */
  public @NonNull Config getConfig() {
    return this.config.getValue();
  }

  /**
   * Call when a fresh config has been loaded from the API.
   * @param freshConfig The fresh config object.
   */
  public void refresh(final @NonNull Config freshConfig) {
    this.config.onNext(freshConfig);
  }

  /**
   * @param assetPath Path where `server-config.json` lives.
   * @param assetManager Asset manager to use to load `server-config.json`.
   * @return A string representation of the config JSON.
   */
  private @NonNull String configJSONString(final @NonNull String assetPath, final @NonNull AssetManager assetManager) {
    try {
      final InputStream input;
      input = assetManager.open(assetPath);
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
