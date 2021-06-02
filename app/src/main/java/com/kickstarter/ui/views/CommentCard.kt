package com.kickstarter.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.kickstarter.R
import com.kickstarter.databinding.CommentCardBinding
import com.kickstarter.ui.extensions.loadCircleImage
import com.kickstarter.ui.extensions.makeLinks
import com.kickstarter.ui.extensions.parseHtmlTag

class CommentCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var binding: CommentCardBinding = CommentCardBinding.inflate(LayoutInflater.from(context), this, true)

    private var onCommentCardClickedListener: OnCommentCardClickedListener? = null

    init {
        obtainStyledAttributes(context, attrs, defStyleAttr)

        bindFlaggedMessage()

        binding.retryButton.setOnClickListener {
            onCommentCardClickedListener?.onRetryViewClicked(it)
        }

        binding.flagButton.setOnClickListener {
            onCommentCardClickedListener?.onFlagButtonClicked(it)
        }

        binding.replies.setOnClickListener {
            onCommentCardClickedListener?.onViewRepliesButtonClicked(it)
        }

        binding.flaggedMessage.setOnClickListener {
            onCommentCardClickedListener?.onCommentGuideLinesClicked(it)
        }
        binding.replyButton.setOnClickListener {
            onCommentCardClickedListener?.onReplyButtonClicked(it)
        }
    }

    private fun bindFlaggedMessage() {
        binding.flaggedMessage.parseHtmlTag()
        binding.flaggedMessage.makeLinks(
            Pair(
                context.resources.getString(R.string.learn_more_about_comment_guidelines),
                OnClickListener {
                    onCommentCardClickedListener?.onCommentGuideLinesClicked(it)
                },

            ),
            linkColor = R.color.kds_create_500
        )
    }

    private fun obtainStyledAttributes(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        context.withStyledAttributes(
            set = attrs,
            attrs = R.styleable.CommentCardView,
            defStyleAttr = defStyleAttr
        ) {
            getString(R.styleable.CommentCardView_comment_card_message)?.also {
                setCommentBody(it)
            }
            getString(R.styleable.CommentCardView_comment_card_post_time)?.also {
                setCommentPostTime(it)
            }
            getString(R.styleable.CommentCardView_comment_card_avatar_url)?.also {
                setAvatarUrl(it)
            }

            getInt(R.styleable.CommentCardView_comment_card_replies, 0).also {
                setCommentReplies(it)
            }

            getString(R.styleable.CommentCardView_comment_card_user_name)?.also {
                setCommentUserName(it)
            }

            getBoolean(R.styleable.CommentCardView_is_comment_action_group_visible, true)?.also {
                setCommentActionGroupVisibility(it)
            }

            getInt(R.styleable.CommentCardView_comment_card_status, 0).also { attrValue ->
                CommentCardStatus.values().firstOrNull {
                    it.commentCardStatus == attrValue
                }?.let {
                    setCommentCardStatus(it)
                }
            }
        }
    }

    fun setCommentActionGroupVisibility(isGroupVisble: Boolean) {
        binding.commentActionGroup.isVisible = isGroupVisble
    }

    fun setCommentCardStatus(cardCommentStatus: CommentCardStatus) {
        binding.commentDeletedMessageGroup.isVisible =
            cardCommentStatus == CommentCardStatus.DELETED_COMMENT

        binding.commentBody.isVisible = cardCommentStatus == CommentCardStatus.COMMENT_WITH_REPLIES ||
            cardCommentStatus == CommentCardStatus.COMMENT_WITHOUT_REPLIES ||
            cardCommentStatus != CommentCardStatus.DELETED_COMMENT

        binding.commentActionGroup.isVisible = cardCommentStatus == CommentCardStatus.COMMENT_WITH_REPLIES ||
            cardCommentStatus == CommentCardStatus.COMMENT_WITHOUT_REPLIES

        binding.retryButton.isVisible =
            cardCommentStatus == CommentCardStatus.FAILED_TO_SEND_COMMENT

        val commentBodyTextColor = if (cardCommentStatus == CommentCardStatus.FAILED_TO_SEND_COMMENT) {
            R.color.soft_grey_disable
        } else {
            R.color.text_primary
        }

        binding.commentBody.setTextColor(ContextCompat.getColor(context, commentBodyTextColor))
    }

    /*
     * To display replies count
     * binding.replies.text = String.format("%s (%d)",resources.getString(R.string.view_replies), replies)
     */
    fun setCommentReplies(replies: Int) {
        binding.replies.isVisible = replies > 0
    }

    fun setCommentUserName(username: String) {
        binding.commentUserName.text = username
    }

    fun hideReplyViewGroup() {
        binding.commentActionGroup.isVisible = false
    }

    fun setCommentPostTime(time: String) {
        binding.commentPostTime.text = time
    }

    fun setCommentBody(comment: String) {
        binding.commentBody.text = comment
    }

    fun setCommentCardClickedListener(onCommentCardClickedListener: OnCommentCardClickedListener?) {
        this.onCommentCardClickedListener = onCommentCardClickedListener
    }

    fun setAvatarUrl(url: String?) {
        binding.avatar.loadCircleImage(url)
    }
}

interface OnCommentCardClickedListener {
    fun onRetryViewClicked(view: View)
    fun onReplyButtonClicked(view: View)
    fun onFlagButtonClicked(view: View)
    fun onViewRepliesButtonClicked(view: View)
    fun onCommentGuideLinesClicked(view: View)
}

enum class CommentCardStatus(val commentCardStatus: Int) {
    COMMENT_WITHOUT_REPLIES(0), // comments without reply view
    COMMENT_WITH_REPLIES(1), // comments with reply view
    FAILED_TO_SEND_COMMENT(2), // pending comment
    DELETED_COMMENT(3) // Deleted comment
}
