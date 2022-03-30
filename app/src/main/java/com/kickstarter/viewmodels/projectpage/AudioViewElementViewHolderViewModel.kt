package com.kickstarter.viewmodels.projectpage

import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.htmlparser.AudioViewElement
import com.kickstarter.ui.fragments.projectpage.ProjectCampaignFragment
import rx.subjects.PublishSubject
import timber.log.Timber

interface AudioViewElementViewHolderViewModel {

    interface Inputs {
        fun configureWith(audioViewElement: AudioViewElement)
    }

    interface Outputs

    class ViewModel(@NonNull environment: Environment) : FragmentViewModel<ProjectCampaignFragment>(environment), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val audioElement = PublishSubject.create<AudioViewElement>()

        init {
            Timber.d("$this has been initialized")
        }

        // - Inputs
        override fun configureWith(audioViewElement: AudioViewElement) = this.audioElement.onNext(audioViewElement)
    }
}
