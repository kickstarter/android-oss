package com.kickstarter.libs

import android.content.res.AssetManager
import com.google.gson.Gson
import com.kickstarter.libs.preferences.StringPreferenceType
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.isNotNull
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.io.IOException
import java.io.InputStream

interface CurrentConfigTypeV2 {
    /**
     * Returns the config as an observable.
     */
    fun observable(): Observable<Config>

    /**
     * Set a new config.
     */
    fun config(config: Config)
}

class CurrentConfigV2(
    assetManager: AssetManager,
    gson: Gson,
    configPreference: StringPreferenceType
) : CurrentConfigTypeV2 {
    private val config = BehaviorSubject.create<Config>()
    private val disposables = CompositeDisposable()
    init {

        // Loads config from disk
        val diskConfig = Observable.just(ASSET_PATH)
            .map { path: String -> configJSONString(path, assetManager) }
            .map { json: String? -> gson.fromJson(json, Config::class.java) }
            .filter { `object`: Config? -> `object`.isNotNull() }
            .compose(Transformers.neverErrorV2())
            .doOnNext {
                Timber.d("Config from Asset: ${it.countryCode()}")
            }
            .subscribeOn(Schedulers.io())

        // Loads config from string preference
        val prefConfig = Observable.just(configPreference)
            .map { obj: StringPreferenceType -> obj.get() }
            .map { json: String? -> gson.fromJson(json, Config::class.java) }
            .filter { `object`: Config? -> `object`.isNotNull() }
            .compose(Transformers.neverErrorV2())
            .doOnNext {
                Timber.d("Config from Prefs: ${it.countryCode()}")
            }
            .subscribeOn(Schedulers.io())

        // Seed config observable with what's cached
        disposables.add(
            Observable.concat(prefConfig, diskConfig)
                .take(1)
                .subscribe { v: Config ->
                    Timber.d("Seed from cache config.onNext(... ${v.countryCode()}...)")
                    config.onNext(v)
                }
        )

        // Cache any new values to preferences
        disposables.add(
            config
                .doOnNext {
                    Timber.d("config.doOnNext: ${it.countryCode()}")
                }
                .skip(1)
                .doOnNext {
                    Timber.d("config.skip(1).doOnNext: ${it.countryCode()}")
                }
                .filter { `object`: Config? -> `object`.isNotNull() }
                .subscribe { c: Config? ->
                    Timber.d("Cache new configPreference.set(... ${c?.countryCode()}...)")
                    configPreference.set(gson.toJson(c, Config::class.java))
                }
        )
    }

    /**
     * Get an observable representation of the current config. Emits immediately with the freshes copy of the config
     * and then emits again for any fresher values.
     */
    override fun observable(): Observable<Config> {
        return config
    }

    override fun config(config: Config) {
        Timber.d("fun config(Config): config.onNext(... ${config.countryCode()}...)")
        this.config.onNext(config)
    }

    /**
     * @param assetPath Path where `server-config.json` lives.
     * @param assetManager Asset manager to use to load `server-config.json`.
     * @return A string representation of the config JSON.
     */
    private fun configJSONString(assetPath: String, assetManager: AssetManager): String {
        try {
            val input: InputStream
            input = assetManager.open(assetPath)
            val buffer = ByteArray(input.available())
            input.read(buffer)
            input.close()
            return String(buffer)
        } catch (e: IOException) {
            Timber.e(e)
            // TODO: This should probably be fatal?
        }
        return "{}"
    }

    companion object {
        private const val ASSET_PATH = "json/server-config.json"
    }
}
