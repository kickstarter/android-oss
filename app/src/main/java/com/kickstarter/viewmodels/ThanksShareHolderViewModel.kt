package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag.Companion.thanksFacebookShare
import com.kickstarter.libs.RefTag.Companion.thanksShare
import com.kickstarter.libs.RefTag.Companion.thanksTwitterShare
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.UrlUtils.appendRefTag
import com.kickstarter.models.Project
import com.kickstarter.ui.viewholders.ThanksShareViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface ThanksShareHolderViewModel {
    interface Inputs {
        /** Call to configure the view model with a project.  */
        fun configureWith(project: Project)

        /** Call when the share button is clicked.  */
        fun shareClick()

        /** Call when the share on Facebook button is clicked.  */
        fun shareOnFacebookClick()

        /** Call when the share on Twitter button is clicked.  */
        fun shareOnTwitterClick()
    }

    interface Outputs {
        /** Emits the backing's project name.  */
        fun projectName(): Observable<String>

        /** Emits the project name and url to share using Android's default share behavior.  */
        fun startShare(): Observable<Pair<String, String>>

        /** Emits the project and url to share using Facebook.  */
        fun startShareOnFacebook(): Observable<Pair<Project, String>>

        /** Emits the project name and url to share using Twitter.  */
        fun startShareOnTwitter(): Observable<Pair<String, String>>
    }

    class ViewModel(environment: Environment) :
        ActivityViewModel<ThanksShareViewHolder>(environment), Inputs, Outputs {

        private val project = PublishSubject.create<Project>()
        private val shareClick = PublishSubject.create<Void>()
        private val shareOnFacebookClick = PublishSubject.create<Void>()
        private val shareOnTwitterClick = PublishSubject.create<Void>()
        private val projectName = BehaviorSubject.create<String>()
        private val startShare = PublishSubject.create<Pair<String, String>>()
        private val startShareOnFacebook = PublishSubject.create<Pair<Project, String>>()
        private val startShareOnTwitter = PublishSubject.create<Pair<String, String>>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            project
                .map { it.name() }
                .compose(bindToLifecycle())
                .subscribe { projectName.onNext(it) }

            project
                .map {
                    Pair.create(
                        it.name(),
                        appendRefTag(it.webProjectUrl(), thanksShare().tag())
                    )
                }
                .compose(Transformers.takeWhen(shareClick))
                .compose(bindToLifecycle())
                .subscribe { startShare.onNext(it) }

            project
                .map {
                    Pair.create(
                        it,
                        appendRefTag(it.webProjectUrl(), thanksFacebookShare().tag())
                    )
                }
                .compose(Transformers.takeWhen(shareOnFacebookClick))
                .compose(bindToLifecycle())
                .subscribe { startShareOnFacebook.onNext(it) }

            project
                .map {
                    Pair.create(
                        it.name(),
                        appendRefTag(it.webProjectUrl(), thanksTwitterShare().tag())
                    )
                }
                .compose(Transformers.takeWhen(shareOnTwitterClick))
                .compose(bindToLifecycle())
                .subscribe { startShareOnTwitter.onNext(it) }
        }

        override fun configureWith(project: Project) {
            this.project.onNext(project)
        }

        override fun shareClick() {
            shareClick.onNext(null)
        }

        override fun shareOnFacebookClick() {
            shareOnFacebookClick.onNext(null)
        }

        override fun shareOnTwitterClick() {
            shareOnTwitterClick.onNext(null)
        }

        override fun startShare(): Observable<Pair<String, String>> = startShare
        override fun startShareOnFacebook(): Observable<Pair<Project, String>> = startShareOnFacebook
        override fun startShareOnTwitter(): Observable<Pair<String, String>> = startShareOnTwitter
        override fun projectName(): Observable<String> = projectName
    }
}
