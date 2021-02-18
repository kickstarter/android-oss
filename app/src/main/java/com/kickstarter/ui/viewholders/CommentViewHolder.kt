package com.kickstarter.ui.viewholders

import android.graphics.Typeface
import android.util.Pair
import android.view.View
import com.kickstarter.R
import com.kickstarter.databinding.CommentCardViewBinding
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.CommentUtils
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Comment
import com.kickstarter.models.Project
import com.squareup.picasso.Picasso

class CommentViewHolder(private val binding: CommentCardViewBinding) : KSViewHolder(binding.root) {
    private var comment: Comment? = null
    private val ksString = environment().ksString()
    private var project: Project? = null

    @Throws(Exception::class)
    override fun bindData(data: Any?) {
        val projectAndComment = ObjectUtils.requireNonNull(data as? Pair<*, *>?)
        project = ObjectUtils.requireNonNull(projectAndComment.first as? Project, Project::class.java)
        comment = ObjectUtils.requireNonNull(projectAndComment.second as? Comment, Comment::class.java)
    }

    override fun onBind() {
        val context = context()
        val currentUser = environment().currentUser().observable()

        binding.creatorLabel.visibility = View.GONE
        binding.userLabel.visibility = View.GONE
        comment?.let { comment ->
            currentUser
                .compose(bindToLifecycle())
                .subscribe { user ->
                    if (CommentUtils.isUserAuthor(comment, project?.creator())) {
                        binding.creatorLabel.visibility = View.VISIBLE
                    } else if (CommentUtils.isUserAuthor(comment, user)) {
                        binding.userLabel.visibility = View.VISIBLE
                    }
                }

            comment.author()?.avatar()?.small()?.let {
                Picasso.get().load(it)
                    .transform(CircleTransformation())
                    .into(binding.avatar)
            }

            binding.name.text = comment.author()?.name()

            binding.postDate.text = DateTimeUtils.relative(context, ksString, comment.createdAt())

            if (CommentUtils.isDeleted(comment)) {
                binding.commentBody.setTextColor(context.getColor(R.color.text_secondary))
                binding.commentBody.setTypeface(binding.commentBody.typeface, Typeface.ITALIC)
            } else {
                binding.commentBody.setTextColor(context.getColor(R.color.text_primary))
                binding.commentBody.setTypeface(binding.commentBody.typeface, Typeface.NORMAL)
            }
            binding.commentBody.text = comment.body()
        }
    }
}
