package com.kickstarter;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ExternalDebugApplicationModule.class})
public interface ApplicationComponent extends ApplicationGraph {
}
