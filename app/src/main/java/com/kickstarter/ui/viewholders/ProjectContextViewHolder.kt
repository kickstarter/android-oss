package com.kickstarter.ui.viewholders

import android.view.View
import com.kickstarter.R
import com.kickstarter.databinding.ProjectContextViewBinding
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Project
import com.squareup.picasso.Picasso

class ProjectContextViewHolder(
    private val binding: ProjectContextViewBinding,
    private val delegate: Delegate
) :
    KSViewHolder(binding.root) {

    private val ksString: KSString = environment().ksString()
    private var project: Project? = null

    interface Delegate {
        fun projectContextClicked(viewHolder: ProjectContextViewHolder?)
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        project = ObjectUtils.requireNonNull(data as Project?, Project::class.java)
    }

    override fun onBind() {
        val photo = project?.photo()
        if (photo != null) {
            binding.projectContextImageView.visibility = View.VISIBLE
            Picasso.with(context()).load(photo.full()).into(binding.projectContextImageView)
        } else {
            binding.projectContextImageView.visibility = View.INVISIBLE
        }
        binding.projectContextProjectName.text = project?.name()
        binding.projectContextCreatorName.text = ksString.format(
            context().getString(R.string.project_creator_by_creator),
            "creator_name",
            project?.creator()?.name()
        )
    }

    override fun onClick(view: View) {
        delegate.projectContextClicked(this)
    }
}
