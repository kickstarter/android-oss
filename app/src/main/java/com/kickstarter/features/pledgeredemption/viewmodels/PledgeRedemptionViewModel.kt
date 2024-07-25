package com.kickstarter.features.pledgeredemption.viewmodels

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey

class PledgeRedemptionViewModel(private val environment: Environment, private val bundle: Bundle? = null) : ViewModel() {

    lateinit var backing: Backing
    lateinit var project: Project

    fun start() {
        project = (bundle?.getParcelable(IntentKey.PROJECT) as Project?)?.let {
            it
        } ?: Project.builder().build()

        backing = project.backing() ?: Backing.builder().build()
    }

    class Factory(private val environment: Environment, private val bundle: Bundle? = null) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PledgeRedemptionViewModel(environment, bundle = bundle) as T
        }
    }
}
