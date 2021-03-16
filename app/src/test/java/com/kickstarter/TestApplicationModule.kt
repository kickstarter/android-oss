package com.kickstarter

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import com.kickstarter.libs.qualifiers.*
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class TestApplicationModule(private val application: Application) : ApplicationModule(application) {

    @Provides
    @Singleton
    @Override
    override fun provideApplication(): Application {
        return application
    }

    @Provides
    @Singleton
    @ApplicationContext
    @Override
    override fun provideApplicationContext(): Context {
        return application
    }

    @Provides
    @Singleton
    @Override
    override fun provideAssetManager(): AssetManager {
        return application.assets
    }

    @Provides
    @Singleton
    @Override
    override fun provideSharedPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
    }
}
