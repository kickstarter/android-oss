package com.kickstarter.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.kickstarter.R
import com.kickstarter.databinding.CommentReplyCardBinding
import com.kickstarter.ui.extensions.loadCircleImage

class CommentReplyCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding: CommentReplyCardBinding = CommentReplyCardBinding.inflate(LayoutInflater.from(context), this, true)
    private var onCommentCardClickedListener: OnCommentCardClickedListener? = null

    init {

        obtainStyledAttributes(context, attrs, defStyleAttr)

        binding.retryButton.setOnClickListener {
            onCommentCardClickedListener?.onRetryViewClicked(it)
        }

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

            getString(R.styleable.CommentCardView_comment_card_user_name)?.also {
                setCommentUserName(it)
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

    fun setCommentCardStatus(cardCommentStatus: CommentCardStatus) {

        binding.commentBody.isVisible = cardCommentStatus == CommentCardStatus.COMMENT_WITH_REPLAY
                || cardCommentStatus == CommentCardStatus.COMMENT_WITHOUT_REPLAY
                || cardCommentStatus != CommentCardStatus.DELETED_COMMENT

        binding.retryButton.isVisible =
            cardCommentStatus == CommentCardStatus.FAILED_TO_SEND_COMMENT

        val commentBodyTextColor = if (cardCommentStatus == CommentCardStatus.FAILED_TO_SEND_COMMENT) {
            R.color.soft_grey_disable
        } else {
            R.color.text_primary
        }

        binding.commentBody.setTextColor(ContextCompat.getColor(context, commentBodyTextColor))
    }

    fun setCommentUserName(username: String) {
        binding.commentUserName.text = username
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