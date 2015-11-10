package com.kickstarter;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ExternalApplicationModule.class})
public interface ApplicationComponent extends ApplicationGraph {
}
