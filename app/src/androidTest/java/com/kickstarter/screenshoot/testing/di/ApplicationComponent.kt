package com.kickstarter.screenshoot.testing.di

import com.kickstarter.ApplicationGraph
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidTestApplicationModule::class])
interface ApplicationComponent : ApplicationGraph
