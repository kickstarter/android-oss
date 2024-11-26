package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kickstarter.R
import com.kickstarter.databinding.ActivityFeedLayoutBinding
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.CurrentUserTypeV2
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.recyclerviewpagination.RecyclerViewPaginatorV2
import com.kickstarter.libs.utils.ApplicationUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.models.Activity
import com.kickstarter.models.ErroredBacking
import com.kickstarter.models.Project
import com.kickstarter.models.SurveyResponse
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.ActivityFeedAdapter
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.ui.extensions.setUpConnectivityStatusCheck
import com.kickstarter.ui.extensions.startActivityWithTransition
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.ActivityFeedViewModel.ActivityFeedViewModel
import com.kickstarter.viewmodels.ActivityFeedViewModel.Factory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class ActivityFeedActivity : AppCompatActivity() {
    private var adapter: ActivityFeedAdapter? = null
    private var currentUser: CurrentUserTypeV2? = null
    private var recyclerViewPaginator: RecyclerViewPaginatorV2? = null
    private lateinit var binding: ActivityFeedLayoutBinding
    private lateinit var viewModelFactory: Factory
    private val viewModel: ActivityFeedViewModel by viewModels {
        viewModelFactory
    }

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedLayoutBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root,
        )
        setContentView(binding.root)

        setUpConnectivityStatusCheck(lifecycle)

        val environment = this.getEnvironment()?.let { env ->
            viewModelFactory = Factory(env)
            env
        }

        currentUser = environment?.currentUserV2()

        adapter = ActivityFeedAdapter(viewModel.inputs)

        binding.recyclerView.adapter = adapter

        binding.recyclerView.layoutManager = LinearLayoutManager(this@ActivityFeedActivity)

        recyclerViewPaginator = RecyclerViewPaginatorV2(
            binding.recyclerView,
            { viewModel.inputs.nextPage() },
            viewModel.outputs.isFetchingActivities()
        )

        binding.activityFeedSwipeRefreshLayout.setOnRefreshListener {
            viewModel.outputs.isFetchingActivities()
            viewModel.inputs.refresh()
        }

        viewModel.outputs.isFetchingActivities()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.activityFeedSwipeRefreshLayout.isRefreshing = it }
            .addToDisposable(disposables)

        // Only allow refreshing if there's a current user
        currentUser?.observable()
            ?.map { it.isPresent() }
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe { binding.activityFeedSwipeRefreshLayout.isEnabled = it }
            ?.addToDisposable(disposables)

        viewModel.outputs.activityList()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showActivities(it) }
            .addToDisposable(disposables)

        viewModel.outputs.erroredBackings()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showErroredBackings(it) }
            .addToDisposable(disposables)

        viewModel.outputs.goToDiscovery()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { resumeDiscoveryActivity() }
            .addToDisposable(disposables)

        viewModel.outputs.goToLogin()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startActivityFeedLogin() }
            .addToDisposable(disposables)

        viewModel.outputs.goToProject()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startProjectActivity(it) }
            .addToDisposable(disposables)

        viewModel.outputs.startFixPledge()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startFixPledge(it) }
            .addToDisposable(disposables)

        viewModel.outputs.startUpdateActivity()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { startUpdateActivity(it) }
            .addToDisposable(disposables)

        viewModel.outputs.loggedOutEmptyStateIsVisible()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter?.showLoggedOutEmptyState(it) }
            .addToDisposable(disposables)

        viewModel.outputs.loggedInEmptyStateIsVisible()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { adapter?.showLoggedInEmptyState(it) }
            .addToDisposable(disposables)

        viewModel.outputs.surveys()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showSurveys(it) }
            .addToDisposable(disposables)

        this.onBackPressedDispatcher.addCallback {
            finishWithAnimation()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.inputs.resume()
    }

    override fun onDestroy() {
        disposables.clear()
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
        val intent = Intent().getProjectIntent(this)
            .putExtra(IntentKey.PROJECT_PARAM, projectSlug)
            .putExtra(IntentKey.EXPAND_PLEDGE_SHEET, true)
            .putExtra(IntentKey.REF_TAG, RefTag.activity())
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startProjectActivity(project: Project) {
        val intent = Intent().getProjectIntent(this)
            .putExtra(IntentKey.PROJECT_PARAM, project.slug())
            .putExtra(IntentKey.REF_TAG, RefTag.activity())
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startUpdateActivity(activity: Activity) {
        val intent = Intent(this, UpdateActivity::class.java)
            .putExtra(IntentKey.PROJECT_PARAM, activity.project()?.slug())
            .putExtra(IntentKey.UPDATE, activity.update())
        startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }
}
