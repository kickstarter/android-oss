package com.kickstarter;

import com.facebook.FacebookSdk;

import org.jetbrains.annotations.NotNull;

import coil.ComponentRegistry;
import coil.ImageLoader;
import coil.request.ErrorResult;
import coil.test.FakeImageLoaderEngine;

public class TestKSApplication extends KSApplication {

  @Override
  public ApplicationComponent getComponent() {
    final ApplicationComponent component = DaggerApplicationComponent.builder()
            .applicationModule(new TestApplicationModule(this))
            .build();

    return component;
  }

  @Override
  public void onCreate() {
    // - LoginToutViewModelTest needs the FacebookSDK initialized
    FacebookSdk.sdkInitialize(this);
    super.onCreate();
  }

  @Override
  public boolean isInUnitTests() {
    return true;
  }

  @Override
  public @NotNull ImageLoader newImageLoader() {
    final FakeImageLoaderEngine engine = new FakeImageLoaderEngine.Builder()
            .addInterceptor((chain, continuation) ->
                    new ErrorResult(null, chain.getRequest(), new Throwable()))
            .build();
    final ComponentRegistry componentRegistry = new ComponentRegistry.Builder()
            .add(engine)
            .build();
    return new ImageLoader.Builder(this)
            .components(componentRegistry)
            .build();
  }
}

