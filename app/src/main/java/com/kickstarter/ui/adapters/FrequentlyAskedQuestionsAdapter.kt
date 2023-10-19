package com.kickstarter.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.kickstarter.R
import com.kickstarter.databinding.ItemFrequentlyAskedQuestionCardBinding
import com.kickstarter.models.ProjectFaq
import com.kickstarter.ui.viewholders.FrequentlyAskedQuestionsViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder

class FrequentlyAskedQuestionsAdapter : KSListAdapter() {

    init {
        insertSection(SECTION_QUESTIONS, emptyList<ProjectFaq>())
    }

    fun takeData(projectFaq: List<ProjectFaq>) {
        if (projectFaq.isNotEmpty()) {
            setSection(SECTION_QUESTIONS, projectFaq)
        }
        submitList(items())
    }

    override fun layout(sectionRow: SectionRow?): Int = R.layout.item_frequently_asked_question_card

    override fun viewHolder(@LayoutRes layout: Int, viewGroup: ViewGroup): KSViewHolder {
        return FrequentlyAskedQuestionsViewHolder(
            ItemFrequentlyAskedQuestionCardBinding.inflate(
                LayoutInflater.from(
                    viewGroup
                        .context
                ),
                viewGroup,
                false
            )
        )
    }

    companion object {
        private const val SECTION_QUESTIONS = 0
    }
}
