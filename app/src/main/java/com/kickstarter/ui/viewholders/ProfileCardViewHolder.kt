package com.kickstarter.ui.viewholders

import android.view.View
import com.kickstarter.R
import com.kickstarter.databinding.ProfileCardViewBinding
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.ProgressBarUtils
import com.kickstarter.models.Project
import com.squareup.picasso.Picasso

class ProfileCardViewHolder(
    private val binding: ProfileCardViewBinding,
    private val delegate: Delegate
) : KSViewHolder(binding.root) {
    private var project: Project? = null

    interface Delegate {
        fun profileCardViewHolderClicked(viewHolder: ProfileCardViewHolder, project: Project)
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        project = ObjectUtils.requireNonNull(data as Project?, Project::class.java)
    }

    override fun onBind() {
        val photo = project?.photo()

        binding.profileCardImage.visibility = View.INVISIBLE

        if (photo != null) {
            binding.profileCardImage.apply {
                visibility = View.VISIBLE
                Picasso.with(context()).load(photo.med())
                    .placeholder(context().getDrawable(R.drawable.gray_gradient))
                    .into(this)
            }
        }
        binding.profileCardName.text = project?.name()
        project?.percentageFunded()?.let {
            binding.percentageFunded.progress = ProgressBarUtils.progress(it)
        }
        setProjectStateView()
    }

    override fun onClick(view: View) {
        project?.let { delegate.profileCardViewHolderClicked(this, it) }
    }

    fun setProjectStateView() {
        when (project?.state()) {
            Project.STATE_SUCCESSFUL -> {
                binding.percentageFunded.visibility = View.GONE
                binding.projectStateViewGroup.visibility = View.VISIBLE
                binding.fundingUnsuccessfulTextView.visibility = View.GONE
                binding.successfullyFundedTextView.visibility = View.VISIBLE
                binding.successfullyFundedTextView.text = context().getString(R.string.profile_projects_status_successful)
            }
            Project.STATE_CANCELED -> {
                binding.percentageFunded.visibility = View.GONE
                binding.projectStateViewGroup.visibility = View.VISIBLE
                binding.successfullyFundedTextView.visibility = View.GONE
                binding.fundingUnsuccessfulTextView.visibility = View.VISIBLE
                binding.fundingUnsuccessfulTextView.text = context().getString(R.string.profile_projects_status_canceled)
            }
            Project.STATE_FAILED -> {
                binding.percentageFunded.visibility = View.GONE
                binding.projectStateViewGroup.visibility = View.VISIBLE
                binding.successfullyFundedTextView.visibility = View.GONE
                binding.fundingUnsuccessfulTextView.visibility = View.VISIBLE
                binding.fundingUnsuccessfulTextView.text = context().getString(R.string.profile_projects_status_unsuccessful)
            }
            Project.STATE_SUSPENDED -> {
                binding.percentageFunded.visibility = View.GONE
                binding.projectStateViewGroup.visibility = View.VISIBLE
                binding.successfullyFundedTextView.visibility = View.GONE
                binding.fundingUnsuccessfulTextView.visibility = View.VISIBLE
                binding.fundingUnsuccessfulTextView.text = context().getString(R.string.profile_projects_status_suspended)
            }
            else -> {
                binding.percentageFunded.visibility = View.VISIBLE
                binding.projectStateViewGroup.visibility = View.GONE
            }
        }
    }
}
