package com.kickstarter.ui.viewholders

import com.kickstarter.databinding.ProjectSocialViewBinding
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.User
import com.kickstarter.ui.extensions.loadCircleImage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

class ProjectSocialViewHolder(private val binding: ProjectSocialViewBinding) : KSViewHolder(binding.root) {
    private var friendObserver = BehaviorSubject.create<User>()
    private val disposables = CompositeDisposable()
    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val user = requireNotNull(data as User?) { User::class.java.toString() + " required to be non-null." }
        if (user.isNotNull()) friendObserver.onNext(user)
    }

    init {
        friendObserver
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.friendImage.loadCircleImage(it.avatar().medium())
                binding.friendName.text = it?.name()
            }
            .addToDisposable(disposables)
    }

    override fun destroy() {
        disposables.clear()
        super.destroy()
    }
}
