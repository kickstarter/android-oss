package com.kickstarter.ui.viewholders

import android.view.View
import com.kickstarter.R
import com.kickstarter.databinding.ProjectContextViewBinding
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Project
import com.kickstarter.ui.extensions.loadImage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

class ProjectContextViewHolder(
    private val binding: ProjectContextViewBinding
) : KSViewHolder(binding.root) {

    private val ksString = requireNotNull(environment().ksString())
    private var projectObserver = BehaviorSubject.create<Project>()
    private val disposables = CompositeDisposable()

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val project = requireNotNull(data as Project?) { Project::class.java.toString() + " required to be non-null." }
        if (project.isNotNull()) projectObserver.onNext(project)
    }

    init {
        projectObserver
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                val photo = it?.photo()
                if (photo != null) {
                    binding.projectContextImageView.visibility = View.VISIBLE
                    binding.projectContextImageView.loadImage(photo.full())
                } else {
                    binding.projectContextImageView.visibility = View.INVISIBLE
                }
                binding.projectContextProjectName.text = it?.name()
                binding.projectContextCreatorName.text = ksString.format(
                    context().getString(R.string.project_creator_by_creator),
                    "creator_name",
                    it?.creator()?.name()
                )
            }
            .addToDisposable(disposables)
    }

    override fun destroy() {
        disposables.clear()
        super.destroy()
    }
}
