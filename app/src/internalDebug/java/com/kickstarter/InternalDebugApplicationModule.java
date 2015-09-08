package com.kickstarter;

import android.content.SharedPreferences;

import com.kickstarter.libs.ApiEndpoint;
import com.kickstarter.libs.BuildCheck;
import com.kickstarter.libs.preferences.StringPreference;
import com.kickstarter.libs.qualifiers.ApiEndpointPreference;
import com.kickstarter.ui.containers.ApplicationContainer;
import com.kickstarter.ui.containers.InternalDebugApplicationContainer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = ApplicationModule.class)
public class InternalDebugApplicationModule {
  @Provides
  @Singleton
  ApplicationContainer provideApplicationContainer() {
    return new InternalDebugApplicationContainer();
  }

  @Provides
  @Singleton
  ApiEndpoint provideApiEndpoint(@ApiEndpointPreference final StringPreference apiEndpointPreference) {
    return ApiEndpoint.from(apiEndpointPreference.get());
  }

  @Provides
  @Singleton
  @ApiEndpointPreference
  StringPreference provideApiEndpointPreference(final SharedPreferences sharedPreferences) {
    return new StringPreference(sharedPreferences, "debug_api_endpoint", ApiEndpoint.PRODUCTION.url);
  }

  @Provides
  BuildCheck provideBuildCheck() {
    return BuildCheck.DEFAULT;
  }
}
