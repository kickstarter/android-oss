package com.kickstarter.ui.viewholders

import android.util.Pair
import android.view.View
import androidx.annotation.StringRes
import com.kickstarter.R
import com.kickstarter.databinding.EmptyCommentsLayoutBinding
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Project
import com.kickstarter.models.User

class EmptyCommentsViewHolder(private val binding: EmptyCommentsLayoutBinding, private val delegate: Delegate) : KSViewHolder(binding.root) {
    private var project: Project? = null
    private var user: User? = null

    interface Delegate {
        fun emptyCommentsLoginClicked(viewHolder: EmptyCommentsViewHolder?)
    }

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val projectAndUser = ObjectUtils.requireNonNull(data as? Pair<Project, User>?)
        project = ObjectUtils.requireNonNull(projectAndUser.first, Project::class.java)
        user = projectAndUser.second
    }

    override fun onBind() {
        when {
            user == null -> {
                bindView(View.VISIBLE, R.string.project_comments_empty_state_logged_out_message_log_in)
            }
            project?.isBacking == true -> {
                bindView(View.GONE, R.string.project_comments_empty_state_backer_message)
            }
            else -> {
                bindView(View.GONE, R.string.update_comments_empty_state_non_backer_message)
            }
        }
        binding.commentsLoginButton.setOnClickListener {
            emptyCommentsLogin()
        }
    }
    private fun bindView(hasVisibility: Int, @StringRes string: Int) {
        binding.commentsLoginButton.visibility = hasVisibility
        binding.noCommentsMessage.setText(string)
    }

    fun emptyCommentsLogin() {
        delegate.emptyCommentsLoginClicked(this)
    }
}
