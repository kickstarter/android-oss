package com.kickstarter.ui.views
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.kickstarter.R
import com.kickstarter.databinding.CommentComposerViewBinding
import com.kickstarter.libs.transformations.CircleTransformation
import com.squareup.picasso.Picasso

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
             isDisabledViewVisible(it)
            }
        }
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

    fun isDisabledViewVisible(isVisible: Boolean) {
        binding.commentsDisableMsg.isVisible = isVisible
        binding.commentTextGroup.isVisible = !isVisible
        binding.commentActionButton.isVisible = !isVisible
    }

    fun setCommentComposerActionClickListener(onCommentComposerViewClickedListener: OnCommentComposerViewClickedListener?) {
       this.onCommentComposerViewClickedListener = onCommentComposerViewClickedListener
    }

    fun setAvatarUrl(url: String?) {
        url?.let {
            Picasso.get().load(it)
                    .transform(CircleTransformation())
                    .into(binding.avatar)
        }
    }
}

interface OnCommentComposerViewClickedListener {
    fun onClickActionListener(string: String)
}
