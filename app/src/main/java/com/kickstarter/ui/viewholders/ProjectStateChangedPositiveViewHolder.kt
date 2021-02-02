package com.kickstarter.ui.viewholders

import androidx.core.content.ContextCompat
import com.kickstarter.R
import com.kickstarter.databinding.ActivityProjectStateChangedPositiveViewBinding
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.KSString
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Activity
import com.squareup.picasso.Picasso
import org.joda.time.DateTime

class ProjectStateChangedPositiveViewHolder(
    private val binding: ActivityProjectStateChangedPositiveViewBinding,
    private val delegate: Delegate?
) :
    ActivityListViewHolder(binding.root) {

    private val ksCurrency: KSCurrency = environment().ksCurrency()
    private val ksString: KSString = environment().ksString()

    interface Delegate {
        fun projectStateChangedPositiveClicked(viewHolder: ProjectStateChangedPositiveViewHolder?, activity: Activity?)
    }

    override fun onBind() {
        val context = context()
        val project = activity().project()
        val user = activity().user()
        val photo = project?.photo()

        if (project != null && user != null && photo != null) {
            when (activity().category()) {
                Activity.CATEGORY_LAUNCH -> {

                    val launchedAt = ObjectUtils.coalesce(project.launchedAt(), DateTime())
                    bind(
                        ContextCompat.getColor(context, R.color.blue_darken_10),
                        ksCurrency.format(project.goal(), project),
                        context.getString(R.string.activity_project_state_change_goal),
                        context.getString(R.string.activity_project_state_change_launched),
                        DateTimeUtils.mediumDate(launchedAt),
                        ksString.format(
                            context.getString(R.string.activity_project_state_change_creator_launched_a_project),
                            "creator_name",
                            user.name(),
                            "project_name",
                            project.name()
                        )
                    )
                }
                Activity.CATEGORY_SUCCESS -> {

                    bind(
                        ContextCompat.getColor(context, R.color.green_darken_10),
                        ksCurrency.format(project.pledged(), project),
                        ksString.format(
                            context.getString(R.string.activity_project_state_change_pledged_of_goal),
                            "goal",
                            ksCurrency.format(project.goal(), project)
                        ),
                        context.getString(R.string.project_status_funded),
                        DateTimeUtils.mediumDate(activity().createdAt()),
                        ksString.format(
                            context.getString(R.string.activity_project_state_change_project_was_successfully_funded),
                            "project_name",
                            project.name()
                        )
                    )
                }
                else -> {
                    bind(ContextCompat.getColor(context, R.color.green_darken_10))
                }
            }
            // TODO: Switch to "You launched a project" if current user launched
            // return context.getString(R.string.creator_launched_a_project, activity.user().name(), activity.project().name());
            Picasso.with(context)
                .load(photo.full())
                .into(binding.projectPhoto)
        }
        binding.cardView.setOnClickListener {
            onClick()
        }
    }

    private fun bind(
        cardColor: Int,
        leftStatFirstText: String = "",
        leftStatSecondText: String = "",
        rightStatFirstText: String = "",
        rightStatSecondText: String = "",
        titleText: String = ""
    ) {
        binding.cardView.setCardBackgroundColor(cardColor)
        binding.leftStatFirst.text = leftStatFirstText
        binding.leftStatSecond.text = leftStatSecondText
        binding.rightStatFirst.text = rightStatFirstText
        binding.rightStatSecond.text = rightStatSecondText
        binding.title.text = titleText
    }

    fun onClick() {
        delegate?.projectStateChangedPositiveClicked(this, activity())
    }
}
