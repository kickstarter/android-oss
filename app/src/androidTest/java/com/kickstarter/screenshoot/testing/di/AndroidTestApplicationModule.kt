package com.kickstarter.screenshoot.testing.di

import android.app.Application
import androidx.annotation.NonNull
import com.apollographql.apollo3.ApolloClient
import com.google.gson.Gson
import com.kickstarter.ApplicationModule
import com.kickstarter.libs.ApiEndpoint
import com.kickstarter.libs.InternalToolsType
import com.kickstarter.libs.NoopInternalTools
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.ApiServiceV2
import com.kickstarter.services.ApolloClientTypeV2
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [ApplicationModule::class])
class AndroidTestApplicationModule(private val application: Application) : ApplicationModule(application) {

    @Provides
    @Singleton
    internal fun provideApiEndpoint(): ApiEndpoint {
        return ApiEndpoint.STAGING
    }

    @Provides
    @Singleton
    @NonNull
    internal fun providesInternalToolsType(): InternalToolsType {
        return NoopInternalTools()
    }

    @Provides
    @Singleton
    internal fun provideApiClientType(apiService: ApiServiceV2, gson: Gson): ApiClientTypeV2 {
        return MockApiClientV2()
    }

    @Provides
    @Singleton
    internal fun provideApolloClientType(apolloClient: ApolloClient): ApolloClientTypeV2 {
        return MockApolloClientV2()
    }
}
