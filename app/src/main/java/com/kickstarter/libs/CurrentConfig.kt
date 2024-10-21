package com.kickstarter.libs

import android.content.res.AssetManager
import com.google.gson.Gson
import com.kickstarter.libs.preferences.StringPreferenceType
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.io.IOException

class CurrentConfig(
    assetManager: AssetManager,
    gson: Gson,
    configPreference: StringPreferenceType
) : CurrentConfigType {
    private val config = BehaviorSubject.create<Config>()
    private val disposables = CompositeDisposable()

    init {
        // Loads config from disk

        val diskConfig = Observable.just(ASSET_PATH)
            .map { configJSONString(it, assetManager) }
            .filter { gson.fromJson(it, Config::class.java).isNotNull() }
            .map { gson.fromJson(it, Config::class.java) }
            .map { it }
            .compose(Transformers.neverErrorV2())
            .subscribeOn(AndroidSchedulers.mainThread())

        // Loads config from string preference
        val prefConfig = Observable.just(configPreference)
            .filter { it.get().isNotNull() }
            .map { it.get() }
            .map { it }
            .filter { gson.fromJson(it, Config::class.java).isNotNull() }
            .map { gson.fromJson(it, Config::class.java) }
            .map { it }
            .compose(Transformers.neverErrorV2())
            .subscribeOn(AndroidSchedulers.mainThread())

        // Seed config observable with what's cached
        Observable.concat(prefConfig, diskConfig)
            .take(1)
            .subscribe { t: Config -> config.onNext(t) }
            .addToDisposable(disposables)

        // Cache any new values to preferences
        config.skip(1)
            .filter { it.isNotNull() }
            .map { it }
            .subscribe { configPreference.set(gson.toJson(it, Config::class.java)) }
            .addToDisposable(disposables)
    }

    /**
     * Get an observable representation of the current config. Emits immediately with the freshes copy of the config
     * and then emits again for any fresher values.
     */
    override fun observable(): Observable<Config> {
        return this.config
    }

    override fun config(config: Config) {
        this.config.onNext(config)
    }

    /**
     * @param assetPath Path where `server-config.json` lives.
     * @param assetManager Asset manager to use to load `server-config.json`.
     * @return A string representation of the config JSON.
     */
    private fun configJSONString(assetPath: String, assetManager: AssetManager): String {
        try {
            val input = assetManager.open(assetPath)
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
