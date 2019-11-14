package com.kickstarter

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [RegressionApplicationModule::class])
interface ApplicationComponent : ApplicationGraph
