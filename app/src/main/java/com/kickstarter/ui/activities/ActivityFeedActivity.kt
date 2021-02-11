package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.ActivityFeedLayoutBinding
import com.kickstarter.libs.*
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.*
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.ActivityFeedAdapter
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.viewmodels.ActivityFeedViewModel

@RequiresActivityViewModel(ActivityFeedViewModel.ViewModel::class)
class ActivityFeedActivity : BaseActivity<ActivityFeedViewModel.ViewModel>() {
    private var adapter: ActivityFeedAdapter? = null
    private var currentUser: CurrentUserType? = null
    private var recyclerViewPaginator: RecyclerViewPaginator? = null
    private var swipeRefresher: SwipeRefresher? = null
    private lateinit var binding: ActivityFeedLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedLayoutBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        currentUser = environment().currentUser()
        adapter = ActivityFeedAdapter(viewModel.inputs)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this@ActivityFeedActivity)

        recyclerViewPaginator = RecyclerViewPaginator(binding.recyclerView, { viewModel.inputs.nextPage() }, viewModel.outputs.isFetchingActivities)
        swipeRefresher = SwipeRefresher(
            this, binding.activityFeedSwipeRefreshLayout, { viewModel.inputs.refresh() }
        ) { viewModel.outputs.isFetchingActivities }

        // Only allow refreshing if there's a current user
        currentUser?.observable()
            ?.map { `object`: User? -> ObjectUtils.isNotNull(`object`) }
            ?.compose(bindToLifecycle())
            ?.compose(Transformers.observeForUI())
            ?.subscribe { binding.activityFeedSwipeRefreshLayout.isEnabled = it }
        viewModel.outputs.activityList()
            .compose(bindToLifecycle())
            .compose<List<Activity?>>(Transformers.observeForUI())
            .subscribe { showActivities(it) }
        viewModel.outputs.erroredBackings()
            .compose(bindToLifecycle())
            .compose<List<ErroredBacking?>>(Transformers.observeForUI())
            .subscribe { showErroredBackings(it) }
        viewModel.outputs.goToDiscovery()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { resumeDiscoveryActivity() }
        viewModel.outputs.goToLogin()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startActivityFeedLogin() }
        viewModel.outputs.goToProject()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startProjectActivity(it) }
        viewModel.outputs.startFixPledge()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startFixPledge(it) }
        viewModel.outputs.startUpdateActivity()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startUpdateActivity(it) }
        viewModel.outputs.loggedOutEmptyStateIsVisible()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { adapter?.showLoggedOutEmptyState(it) }
        viewModel.outputs.loggedInEmptyStateIsVisible()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { adapter?.showLoggedInEmptyState(it) }
        viewModel.outputs.surveys()
            .compose(bindToLifecycle())
            .compose<List<SurveyResponse?>>(Transformers.observeForUI())
            .subscribe { showSurveys(it) }
    }

    override fun onResume() {
        super.onResume()
        viewModel.inputs.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        recyclerViewPaginator?.stop()
        binding.recyclerView.adapter = null
    }

    private fun showActivities(activities: List<Activity?>) {
        adapter?.takeActivities(activities)
    }

    private fun showErroredBackings(erroredBackings: List<ErroredBacking?>) {
        adapter?.takeErroredBackings(erroredBackings)
    }

    private fun showSurveys(surveyResponses: List<SurveyResponse?>) {
        adapter?.takeSurveys(surveyResponses)
    }

    private fun resumeDiscoveryActivity() {
        ApplicationUtils.resumeDiscoveryActivity(this)
    }

    private fun startActivityFeedLogin() {
        val intent = Intent(this, LoginToutActivity::class.java)
            .putExtra(IntentKey.LOGIN_REASON, LoginReason.ACTIVITY_FEED)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
    }

    private fun startFixPledge(projectSlug: String) {
        val intent = Intent(this, ProjectActivity::class.java)
            .putExtra(IntentKey.PROJECT_PARAM, projectSlug)
            .putExtra(IntentKey.EXPAND_PLEDGE_SHEET, true)
            .putExtra(IntentKey.REF_TAG, RefTag.activity())
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startProjectActivity(project: Project) {
        val intent = Intent(this, ProjectActivity::class.java)
            .putExtra(IntentKey.PROJECT, project)
            .putExtra(IntentKey.REF_TAG, RefTag.activity())
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startUpdateActivity(activity: Activity) {
        val intent = Intent(this, UpdateActivity::class.java)
            .putExtra(IntentKey.PROJECT, activity.project())
            .putExtra(IntentKey.UPDATE, activity.update())
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }
}
