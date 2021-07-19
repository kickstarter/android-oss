package com.kickstarter

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidTestApplicationModule::class])
interface ApplicationComponent : ApplicationGraph
