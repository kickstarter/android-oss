package com.kickstarter.ui.views
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.kickstarter.R
import com.kickstarter.databinding.CommentComposerViewBinding
import com.kickstarter.ui.extensions.loadCircleImage

class CommentComposerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding: CommentComposerViewBinding = CommentComposerViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var onCommentComposerViewClickedListener: OnCommentComposerViewClickedListener? = null

    init {
        obtainStyledAttributes(context, attrs, defStyleAttr)
        binding.commentActionButton.isVisible = false

        binding.commentActionButton.setOnClickListener {
            onCommentComposerViewClickedListener?.onClickActionListener(binding.commentTextComposer.text.toString())
        }

        binding.commentTextComposer.doOnTextChanged { text, _, _, _ ->
            binding.commentActionButton.isEnabled = text?.trim()?.isNotEmpty() ?: false
        }

        binding.commentTextComposer.setOnFocusChangeListener { _, hasFocus ->
            binding.commentActionButton.isVisible = hasFocus
        }
    }

    private fun obtainStyledAttributes(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        context.withStyledAttributes(
            set = attrs,
            attrs = R.styleable.CommentComposerView,
            defStyleAttr = defStyleAttr
        ) {
            getString(R.styleable.CommentComposerView_composer_hint)?.also {
                setCommentComposerHint(it)
            }
            getString(R.styleable.CommentComposerView_composer_action_title)?.also {
                setActionButtonTitle(it)
            }
            getString(R.styleable.CommentComposerView_composer_avatar_url)?.also {
                setAvatarUrl(it)
            }
            getBoolean(R.styleable.CommentComposerView_composer_disabled, false).also {
                showCommentComposerDisabledView()
            }
        }
    }

    fun clearCommentComposer() {
        binding.commentTextComposer.text?.clear()
    }

    fun setActionButtonTitle(title: String) {
        binding.commentActionButton.text = title
    }

    fun setCommentComposerHint(hint: String) {
        binding.commentTextComposer.hint = hint
    }

    fun setActionButtonTitle(@StringRes title: Int) {
        binding.commentActionButton.text = context.getString(title)
    }

    fun setCommentComposerHint(@StringRes hint: Int) {
        binding.commentTextComposer.hint = context.getString(hint)
    }

    fun setCommentComposerStatus(commentComposerStatus: CommentComposerStatus) {
        when (commentComposerStatus) {
            CommentComposerStatus.ENABLED -> showCommentComposerEnabledView()
            CommentComposerStatus.DISABLED -> showCommentComposerDisabledView()
            CommentComposerStatus.GONE -> hideCommentComposer()
        }
    }

    private fun showCommentComposerDisabledView() {
        binding.separtor.isVisible = true
        binding.commentsDisableMsg.isVisible = true
        binding.commentTextGroup.isVisible = false
        binding.commentActionButton.isVisible = false
    }

    private fun showCommentComposerEnabledView() {
        binding.separtor.isVisible = true
        binding.commentsDisableMsg.isVisible = false
        binding.commentTextGroup.isVisible = true
        binding.commentActionButton.isVisible = true
    }

    private fun hideCommentComposer() {
        binding.commentsDisableMsg.isGone = true
        binding.commentTextGroup.isGone = true
        binding.commentActionButton.isGone = true
    }

    fun setCommentComposerActionClickListener(onCommentComposerViewClickedListener: OnCommentComposerViewClickedListener?) {
        this.onCommentComposerViewClickedListener = onCommentComposerViewClickedListener
    }

    fun setAvatarUrl(url: String?) {
        binding.avatar.loadCircleImage(url)
    }
}

interface OnCommentComposerViewClickedListener {
    fun onClickActionListener(string: String)
}

enum class CommentComposerStatus(val commentComposerStatus: Int) {
    ENABLED(0), // Visible and interactable
    DISABLED(1), // Visible and not interactable
    GONE(2) // Entire view is completely gone
}
