package com.kickstarter.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.VisibleForTesting
import androidx.cardview.widget.CardView
import androidx.core.content.withStyledAttributes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.kickstarter.R
import com.kickstarter.databinding.FrequentlyAskedQuestionCardBinding

class FrequentlyAskedQuestionCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {
    private var binding: FrequentlyAskedQuestionCardBinding =
        FrequentlyAskedQuestionCardBinding.inflate(
            LayoutInflater.from(context),
            this, true
        )

    private var isExpanded: Boolean = false

    init {
        obtainStyledAttributes(context, attrs, defStyleAttr)
        binding.dropdownButton.setOnClickListener {
            toggleAnswerLayout()
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun toggleAnswerLayout() {
        if (isExpanded) collapse() else expand()
    }

    private fun obtainStyledAttributes(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        context.withStyledAttributes(
            set = attrs,
            attrs = R.styleable.FrequentlyAskedQuestionCard,
            defStyleAttr = defStyleAttr
        ) {
            getString(R.styleable.FrequentlyAskedQuestionCard_frequently_asked_question_text)?.also {
                setQuestion(it)
            }
            getString(R.styleable.FrequentlyAskedQuestionCard_frequently_asked_answer_text)?.also {
                setAnswer(it)
            }
            getString(R.styleable.FrequentlyAskedQuestionCard_frequently_asked_question_update_date_text)?.also {
                setLastedUpdateDate(it)
            }
        }
    }

    fun setQuestion(question: String) {
        binding.questionTv.text = question
    }

    fun setAnswer(answer: String) {
        binding.answerTv.text = answer
    }

    fun setLastedUpdateDate(date: String) {
        binding.updatedDateTv.text = date
    }

    private fun expand() {
        expandAnimation()
        isExpanded = true
    }

    private fun collapse() {
        collapseAnimation()
        isExpanded = false
    }

    private fun expandAnimation() {
        binding.dropdownButton.animate()?.rotation(180f)?.start()

        val transition = ChangeBounds()
        transition.duration = 100
        binding.answerLayout.isVisible = true
        binding.answerLayout.let {
            TransitionManager.beginDelayedTransition(it, transition)
        }
    }

    private fun collapseAnimation() {
        binding.dropdownButton.animate()?.rotation(0f)?.start()

        val transition = ChangeBounds()
        transition.duration = 100

        binding.answerLayout.let {
            TransitionManager.beginDelayedTransition(it, transition)
            binding.answerLayout.isGone = true
        }
    }
}
