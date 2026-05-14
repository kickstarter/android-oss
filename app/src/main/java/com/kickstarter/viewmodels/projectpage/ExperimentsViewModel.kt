package com.kickstarter.viewmodels.projectpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.libs.Environment
import com.kickstarter.libs.featureflag.StatsigExperiments
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import timber.log.Timber
import kotlin.coroutines.EmptyCoroutineContext

/** For Crashlytics logging only **/
private class ExperimentsException(cause: Exception) : Exception(cause)
@OptIn(FlowPreview::class)
class ExperimentsViewModel(
    environment: Environment,
    testDispatcher: CoroutineDispatcher? = null
) : ViewModel() {

    private val scope = viewModelScope + (testDispatcher ?: EmptyCoroutineContext)
    private val statsigClient = requireNotNull(environment.statsigClient())

    init {
        scope.launch {
            try {
                statsigClient.isReady.first { it }
                statsigClient.statsigUser
                    .onEach {
                        Timber.d(
                            """
                        statsigUser[stableID: ${statsigClient.getStableId()}]
                        - userID: ${it.userID}
                        - email: ${it.email}
                        - ip: ${it.ip}
                        - userAgent: ${it.userAgent}
                        - country: ${it.country}
                        - locale: ${it.locale}
                        - appVersion: ${it.appVersion}
                        - custom: ${it.custom}
                        - privateAttributes: ${it.privateAttributes}
                        - customIDs: ${it.customIDs}
                        """.trimIndent()
                        )
                    }
                    .collect {
                        val authenticatedExperiment = statsigClient.getExperiment(StatsigExperiments.NoOpAuthenticatedUsers.name)
                        val anonymousExperiment = statsigClient.getExperiment(StatsigExperiments.NoOpAnonymousUsers.name)
                        Timber.d(
                            """
                        authenticatedExperiment: ${authenticatedExperiment.getName()}
                        - getEvalDetails(): ${authenticatedExperiment.getEvalDetails()}
                        - getIsExperimentActive(): ${authenticatedExperiment.getIsExperimentActive()}
                        - getIsUserInExperiment(): ${authenticatedExperiment.getIsUserInExperiment()}
                        - getValue(): ${authenticatedExperiment.getValue()}
                        - getBooleanIfPresent(${StatsigExperiments.NoOpAuthenticatedUsers.parameters.TEST}): ${authenticatedExperiment.getBooleanIfPresent(StatsigExperiments.NoOpAuthenticatedUsers.parameters.TEST)}
                        - getRuleID(): ${authenticatedExperiment.getRuleID()}
                        - getRulePassed(): ${authenticatedExperiment.getRulePassed()}
                        - getGroupName(): ${authenticatedExperiment.getGroupName()}
                        """.trimIndent()
                        )
                        Timber.d(
                            """
                        anonymousExperiment: ${anonymousExperiment.getName()}
                        - getEvalDetails(): ${anonymousExperiment.getEvalDetails()}
                        - getIsExperimentActive(): ${anonymousExperiment.getIsExperimentActive()}
                        - getIsUserInExperiment(): ${anonymousExperiment.getIsUserInExperiment()}
                        - getValue(): ${anonymousExperiment.getValue()}
                        - getBooleanIfPresent(${StatsigExperiments.NoOpAnonymousUsers.parameters.TEST}): ${anonymousExperiment.getBooleanIfPresent(StatsigExperiments.NoOpAnonymousUsers.parameters.TEST)}
                        - getRuleID(): ${anonymousExperiment.getRuleID()}
                        - getRulePassed(): ${anonymousExperiment.getRulePassed()}
                        - getGroupName(): ${anonymousExperiment.getGroupName()}
                        """.trimIndent()
                        )
                    }
            } catch (exception: Exception) {
                FirebaseCrashlytics.getInstance().recordException(ExperimentsException(exception))
            }
        }
    }

    class Factory(
        private val environment: Environment,
        private val testDispatcher: CoroutineDispatcher? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExperimentsViewModel(environment, testDispatcher) as T
        }
    }
}
