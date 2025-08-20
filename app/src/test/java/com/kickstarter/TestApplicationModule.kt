package com.kickstarter

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider

// TODO: Refactor the approach to testing with this class to not rely on extending `ApplicationModule`.
/* This class is no longer marked as `@Module`, and methods no longer marked as
 * `@Provides`. Dagger 2 recognizes this approach as an anti-pattern, and since Kotlin 1.9.+
 *  overriding `@Provides` methods becomes a compile-time error. */
class TestApplicationModule(private val application: Application) : ApplicationModule(application) {

    @Override
    override fun provideApplication(): Application {
        return application
    }

    @Override
    override fun provideApplicationContext(): Context {
        return application
    }

    @Override
    override fun provideAssetManager(): AssetManager {
        return application.assets
    }

    @Override
    override fun provideSharedPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
    }
}
