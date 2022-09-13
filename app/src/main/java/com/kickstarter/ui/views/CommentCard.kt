package com.kickstarter.ui.views

import android.content.Context
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.util.Linkify
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.kickstarter.R
import com.kickstarter.databinding.CommentCardBinding
import com.kickstarter.libs.utils.extensions.parseHtmlTag
import com.kickstarter.libs.utils.extensions.setAllOnClickListener
import com.kickstarter.ui.extensions.loadCircleImage
import com.kickstarter.ui.extensions.makeLinks
import com.kickstarter.ui.extensions.parseAndSpanHtmlTag
import com.kickstarter.ui.extensions.parseHtmlTag
import com.kickstarter.ui.extensions.urlSpanWithoutUnderlines

class CommentCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var binding: CommentCardBinding = CommentCardBinding.inflate(LayoutInflater.from(context), this, true)

    private var onCommentCardClickedListener: OnCommentCardClickedListener? = null

    private var isCommentEnabledThreads: Boolean = false

    init {
        obtainStyledAttributes(context, attrs, defStyleAttr)

        bindCommunityGuidelines(binding.removedMessage, onCommentCardClickedListener)
        bindFlaggedCommunityGuidelines(
            context.resources.getString(R.string.This_comment_is_under_review_for_potentially_violating_kickstarters_community_guidelines),
            binding.flaggedMessage,
            onCommentCardClickedListener
        )

        binding.retryButtonGroup.setAllOnClickListener {
            onCommentCardClickedListener?.onRetryViewClicked(it)
        }

        binding.replies.setOnClickListener {
            onCommentCardClickedListener?.onViewRepliesButtonClicked(it)
        }

        binding.removedMessage.setOnClickListener {
            onCommentCardClickedListener?.onCommentGuideLinesClicked(it)
        }

        binding.replyButton.setOnClickListener {
            onCommentCardClickedListener?.onReplyButtonClicked(it)
        }
    }

    private fun bindCommunityGuidelines(textView: AppCompatTextView, onCommentCardClickedListener: OnCommentCardClickedListener?) {
        textView.parseHtmlTag()
        textView.makeLinks(
            Pair(
                context.resources.getString(R.string.Learn_more_about_comment_guidelines).parseHtmlTag(),
                OnClickListener {
                    onCommentCardClickedListener?.onCommentGuideLinesClicked(it)
                },

            ),
            linkColor = R.color.kds_create_500,
            isUnderlineText = false
        )
    }

    private fun bindFlaggedCommunityGuidelines(message: String, textView: AppCompatTextView, onCommentCardClickedListener: OnCommentCardClickedListener?) {
        textView.parseAndSpanHtmlTag(
            message,
            clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onCommentCardClickedListener?.onCommentGuideLinesClicked(widget)
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.color = ContextCompat.getColor(binding.flaggedMessage.context, R.color.kds_create_500)
                }
            }
        )
    }

    private fun bindCancelPledgeMessage() {
        binding.canceledPledgeMessage.parseHtmlTag()
        binding.canceledPledgeMessage.makeLinks(
            Pair(
                context.resources.getString(R.string.Show_comment),
                OnClickListener {
                    onCommentCardClickedListener?.onShowCommentClicked(it)
                },

            ),
            linkColor = R.color.kds_create_500,
            isUnderlineText = false
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

            getBoolean(R.styleable.CommentCardView_is_comment_reply_button_visible, true).also {
                setReplyButtonVisibility(it)
            }

            getInt(R.styleable.CommentCardView_is_comment_you_badge_visible, 0).also {
                setCommentBadge(CommentCardBadge.NO_BADGE)
            }

            getInt(R.styleable.CommentCardView_is_comment_reply_button_visible, 0).also {
                setCommentBadge(CommentCardBadge.NO_BADGE)
            }

            getInt(R.styleable.CommentCardView_is_comment_reply_button_visible, 0).also {
                setCommentBadge(CommentCardBadge.NO_BADGE)
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

    fun setCommentEnabledThreads(isActiveFeatureFlag: Boolean) {
        this.isCommentEnabledThreads = isActiveFeatureFlag
    }

    fun setReplyButtonVisibility(isGroupVisible: Boolean) {
        binding.replyButton.isVisible = isGroupVisible && this.isCommentEnabledThreads
    }

    fun setCommentCardStatus(cardCommentStatus: CommentCardStatus) {

        binding.commentBody.isVisible = cardCommentStatus == CommentCardStatus.COMMENT_WITH_REPLIES ||
            cardCommentStatus == CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS ||
            cardCommentStatus == CommentCardStatus.FAILED_TO_SEND_COMMENT ||
            cardCommentStatus == CommentCardStatus.RE_TRYING_TO_POST ||
            cardCommentStatus == CommentCardStatus.POSTING_COMMENT_COMPLETED_SUCCESSFULLY ||
            cardCommentStatus == CommentCardStatus.TRYING_TO_POST ||
            cardCommentStatus == CommentCardStatus.CANCELED_PLEDGE_COMMENT

        binding.removedMessage.isVisible =
            cardCommentStatus == CommentCardStatus.DELETED_COMMENT

        binding.flaggedMessage.isVisible =
            cardCommentStatus == CommentCardStatus.FLAGGED_COMMENT

        binding.infoButton.isVisible =
            cardCommentStatus == CommentCardStatus.DELETED_COMMENT ||
            cardCommentStatus == CommentCardStatus.FLAGGED_COMMENT

        binding.canceledPledgeMessage.isVisible =
            cardCommentStatus == CommentCardStatus.CANCELED_PLEDGE_MESSAGE

        if (shouldShowReplyButton(cardCommentStatus)) {
            setReplyButtonVisibility(true)
        } else {
            hideReplyButton()
        }

        binding.retryButtonGroup.isVisible =
            cardCommentStatus == CommentCardStatus.FAILED_TO_SEND_COMMENT

        binding.postingButton.isVisible =
            cardCommentStatus == CommentCardStatus.RE_TRYING_TO_POST

        binding.postedButton.isVisible =
            cardCommentStatus == CommentCardStatus.POSTING_COMMENT_COMPLETED_SUCCESSFULLY

        val commentBodyTextColor = if (cardCommentStatus == CommentCardStatus.FAILED_TO_SEND_COMMENT ||
            cardCommentStatus == CommentCardStatus.RE_TRYING_TO_POST
        ) {
            R.color.soft_grey_disable
        } else {
            R.color.text_primary
        }

        binding.commentBody.setTextColor(ContextCompat.getColor(context, commentBodyTextColor))
    }

    private fun shouldShowReplyButton(cardCommentStatus: CommentCardStatus) =
        cardCommentStatus == CommentCardStatus.COMMENT_WITH_REPLIES ||
            cardCommentStatus == CommentCardStatus.COMMENT_FOR_LOGIN_BACKED_USERS

    /*
     * To display replies count
     * binding.replies.text = String.format("%s (%d)",resources.getString(R.string.view_replies), replies)
     */
    fun setCommentReplies(replies: Int) {
        setViewRepliesVisibility(replies > 0)
    }

    fun setSeparatorVisibility(visibility: Boolean) {
        binding.separtor.isVisible = visibility
    }

    fun setViewRepliesVisibility(isViewRepliesVisible: Boolean) {
        binding.replies.isVisible = isViewRepliesVisible && isCommentEnabledThreads
    }

    fun setCommentUserName(username: String) {
        binding.commentUserName.text = username
    }

    fun hideReplyButton() {
        binding.replyButton.isVisible = false
    }

    fun setCommentPostTime(time: String) {
        binding.commentPostTime.text = time
    }

    fun setCommentBody(comment: String) {
        binding.commentBody.text = comment
        Linkify.addLinks(binding.commentBody, Linkify.WEB_URLS)
        binding.commentBody.urlSpanWithoutUnderlines()
    }

    fun setRemovedMessage(message: String) {
        binding.removedMessage.text = message
        bindCommunityGuidelines(binding.removedMessage, onCommentCardClickedListener)
    }

    fun setFlaggedMessage(message: String) {
        bindFlaggedCommunityGuidelines(message, binding.flaggedMessage, onCommentCardClickedListener)
    }

    fun setCancelPledgeMessage(message: String) {
        binding.canceledPledgeMessage.text = message
        bindCancelPledgeMessage()
    }

    fun setCommentCardClickedListener(onCommentCardClickedListener: OnCommentCardClickedListener?) {
        this.onCommentCardClickedListener = onCommentCardClickedListener
    }

    fun setAvatarUrl(url: String?) {
        binding.avatar.loadCircleImage(url)
    }

    fun setCommentBadge(badge: CommentCardBadge?) {
        when (badge) {
            CommentCardBadge.NO_BADGE -> setBadgesVisibility(false, false, false)
            CommentCardBadge.YOU -> setBadgesVisibility(true, false, false)
            CommentCardBadge.SUPERBACKER -> setBadgesVisibility(false, true, false)
            CommentCardBadge.CREATOR -> setBadgesVisibility(false, false, true, context.getString(R.string.Creator))
            CommentCardBadge.COLLABORATOR -> setBadgesVisibility(false, false, true, context.getString(R.string.Collaborator))
            else -> {}
        }
    }

    private fun setBadgesVisibility(
        isYouBadgeVisible: Boolean,
        isSuperBackerBadgeVisible: Boolean,
        isCreatorBadgeVisible: Boolean,
        creatorBadgeText: String? = null
    ) {
        binding.youBadge.isVisible = isYouBadgeVisible
        binding.superbackerBadge.isVisible = isSuperBackerBadgeVisible
        binding.ownerBadge.isVisible = isCreatorBadgeVisible
        binding.ownerBadge.text = creatorBadgeText ?: ""
    }
}

interface OnCommentCardClickedListener {
    fun onRetryViewClicked(view: View)
    fun onReplyButtonClicked(view: View)
    fun onFlagButtonClicked(view: View)
    fun onViewRepliesButtonClicked(view: View)
    fun onCommentGuideLinesClicked(view: View)
    fun onShowCommentClicked(view: View)
}

enum class CommentCardBadge(val commentCardBadge: Int) {
    NO_BADGE(0),
    YOU(1),
    CREATOR(2),
    SUPERBACKER(3),
    COLLABORATOR(4),
}

enum class CommentCardStatus(val commentCardStatus: Int) {
    COMMENT_FOR_LOGIN_BACKED_USERS(0), // comments without reply view
    COMMENT_WITH_REPLIES(1), // comments with reply view
    FAILED_TO_SEND_COMMENT(2), // pending comment
    DELETED_COMMENT(3), // Deleted comment
    RE_TRYING_TO_POST(4), // trying to post comment
    POSTING_COMMENT_COMPLETED_SUCCESSFULLY(5), // trying to post comment,
    TRYING_TO_POST(6), // comments without reply view,
    CANCELED_PLEDGE_MESSAGE(7),
    CANCELED_PLEDGE_COMMENT(8),
    FLAGGED_COMMENT(9),
}
