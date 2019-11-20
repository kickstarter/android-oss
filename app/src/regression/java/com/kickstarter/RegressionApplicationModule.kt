package com.kickstarter

import androidx.annotation.NonNull
import com.apollographql.apollo.ApolloClient
import com.google.gson.Gson
import com.kickstarter.libs.*
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.services.ApiClientType
import com.kickstarter.services.ApiService
import com.kickstarter.services.ApolloClientType
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

    @Provides
    @Singleton
    internal fun provideApiClientType(apiService: ApiService, gson: Gson): ApiClientType {
        return MockApiClient()
    }

    @Provides
    @Singleton
    internal fun provideApolloClientType(apolloClient: ApolloClient): ApolloClientType {
        return MockApolloClient()
    }
}
