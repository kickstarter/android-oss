package com.kickstarter

import androidx.annotation.NonNull
import com.kickstarter.libs.*
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [ApplicationModule::class])
class RegressionApplicationModule {

    @Provides
    @Singleton
    internal fun provideApiEndpoint(): ApiEndpoint {
        return ApiEndpoint.STAGING
    }

    @Provides
    internal fun provideBuildCheck(): BuildCheck {
        return NoopBuildCheck()
    }

    @Provides
    @Singleton
    @NonNull
    internal fun providesInternalToolsType(): InternalToolsType {
        return NoopInternalTools()
    }
}
