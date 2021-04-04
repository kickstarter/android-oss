package com.kickstarter.ui.activities

import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.jakewharton.rxbinding.support.v4.widget.RxDrawerLayout
import com.kickstarter.BuildConfig
import com.kickstarter.R
import com.kickstarter.databinding.DiscoveryLayoutBinding
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.InternalToolsType
import com.kickstarter.libs.KoalaContext
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.models.QualtricsIntercept
import com.kickstarter.models.QualtricsResult
import com.kickstarter.services.apiresponses.InternalBuildEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.DiscoveryDrawerAdapter
import com.kickstarter.ui.adapters.DiscoveryPagerAdapter
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.showErrorSnackBar
import com.kickstarter.ui.extensions.showSuccessSnackBar
import com.kickstarter.ui.fragments.DiscoveryFragment
import com.kickstarter.ui.fragments.DiscoveryFragment.Companion.newInstance
import com.kickstarter.viewmodels.DiscoveryViewModel
import com.qualtrics.digital.InitializationResult
import com.qualtrics.digital.Qualtrics
import com.qualtrics.digital.QualtricsSurveyActivity
import rx.android.schedulers.AndroidSchedulers
import java.util.Arrays
import java.util.Locale

@RequiresActivityViewModel(DiscoveryViewModel.ViewModel::class)
class DiscoveryActivity : BaseActivity<DiscoveryViewModel.ViewModel>() {
    private lateinit var drawerAdapter: DiscoveryDrawerAdapter
    private lateinit var drawerLayoutManager: LinearLayoutManager
    private lateinit var pagerAdapter: DiscoveryPagerAdapter
    private lateinit var internalTools: InternalToolsType
    private lateinit var binding: DiscoveryLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DiscoveryLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        internalTools = environment().internalTools()

        drawerLayoutManager = LinearLayoutManager(this)

        binding.discoveryDrawerRecyclerView.layoutManager = drawerLayoutManager
        drawerAdapter = DiscoveryDrawerAdapter(viewModel.inputs)
        binding.discoveryDrawerRecyclerView.adapter = drawerAdapter

        val viewPagerTitles = Arrays.asList(
            getString(R.string.discovery_sort_types_magic),
            getString(R.string.Popular),
            getString(R.string.discovery_sort_types_newest),
            getString(R.string.Ending_soon)
        )

        pagerAdapter = DiscoveryPagerAdapter(
            supportFragmentManager, createFragments(viewPagerTitles.size), viewPagerTitles, viewModel.inputs
        )

        binding.discoveryViewPager.adapter = pagerAdapter
        binding.discoveryTabLayout.setupWithViewPager(binding.discoveryViewPager)

        addTabSelectedListenerToTabLayout()

        viewModel.outputs.expandSortTabLayout()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.discoverySortAppBarLayout.setExpanded(it) }

        viewModel.outputs.updateToolbarWithParams()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { binding.discoveryToolbar.discoveryToolbar.loadParams(it) }

        viewModel.outputs.updateParamsForPage()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { pagerAdapter.takeParams(it) }

        viewModel.outputs.clearPages()
            .compose(bindToLifecycle())
            .compose<List<Int?>>(Transformers.observeForUI())
            .subscribe { pagerAdapter.clearPages(it) }

        viewModel.outputs.rootCategoriesAndPosition()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { pagerAdapter.takeCategoriesForPosition(it.first, it.second) }

        viewModel.outputs.showBuildCheckAlert()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { showBuildAlert(it) }

        viewModel.outputs.showActivityFeed()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startActivityFeedActivity() }

        viewModel.outputs.showCreatorDashboard()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startCreatorDashboardActivity() }

        viewModel.outputs.showHelp()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startHelpSettingsActivity() }

        viewModel.outputs.showInternalTools()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { internalTools.maybeStartInternalToolsActivity(this) }

        viewModel.outputs.showLoginTout()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startLoginToutActivity() }

        viewModel.outputs.showMessages()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startMessageThreadsActivity() }

        viewModel.outputs.showProfile()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startProfileActivity() }

        viewModel.outputs.showSettings()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { startSettingsActivity() }

        viewModel.outputs.navigationDrawerData()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { drawerAdapter.takeData(it) }

        viewModel.outputs.drawerIsOpen()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(RxDrawerLayout.open(binding.discoveryDrawerLayout, GravityCompat.START))

        viewModel.outputs.drawerMenuIcon()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { drawerMenuIcon: Int -> updateDrawerMenuIcon(drawerMenuIcon) }

        //region Qualtrics
        viewModel.outputs.qualtricsPromptIsGone()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe(ViewUtils.setInvisible(binding.qualtricsPromptLayout.qualtricsPrompt))

        viewModel.outputs.setUpQualtrics()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { setUpQualtrics(it) }

        viewModel.outputs.showQualtricsSurvey()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { showQualtricsSurvey(it) }

        viewModel.outputs.updateImpressionCount()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { updateImpressionCount(it) }

        //endregion
        RxDrawerLayout.drawerOpen(binding.discoveryDrawerLayout, GravityCompat.START)
            .skip(1)
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { viewModel.inputs.openDrawer(it) }

        viewModel.outputs.showSuccessMessage()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { this@DiscoveryActivity.showSuccessSnackBar(binding.discoveryAnchorView, it) }

        viewModel.outputs.showErrorMessage()
            .compose(bindToLifecycle())
            .compose(Transformers.observeForUI())
            .subscribe { this@DiscoveryActivity.showErrorSnackBar(binding.discoveryAnchorView, it) }

        binding.qualtricsPromptLayout.qualtricsConfirm.setOnClickListener {
            qualtricsConfirmClicked()
        }

        binding.qualtricsPromptLayout.qualtricsDismiss.setOnClickListener {
            qualtricsDismissClicked()
        }
    }

    private fun qualtricsConfirmClicked() {
        viewModel.inputs.qualtricsConfirmClicked()
    }

    private fun qualtricsDismissClicked() {
        viewModel.inputs.qualtricsDismissClicked()
    }

    fun discoveryLayout(): DrawerLayout {
        return binding.discoveryDrawerLayout
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (binding.qualtricsPromptLayout.qualtricsPrompt.visibility != View.VISIBLE) {
            super.onNetworkConnectionChanged(isConnected)
        }
    }

    private fun addTabSelectedListenerToTabLayout() {
        binding.discoveryTabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.sortClicked(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {
                pagerAdapter.scrollToTop(tab.position)
                viewModel.sortClicked(tab.position)
            }
        })
    }

    private fun setUpQualtrics(firstAppSession: Boolean) {
        Qualtrics.instance().initialize(
            Secrets.Qualtrics.BRAND_ID,
            Secrets.Qualtrics.ZONE_ID,
            QualtricsIntercept.NATIVE_APP_FEEDBACK.id(BuildConfig.APPLICATION_ID),
            this
        ) { initializationResult: InitializationResult ->
            if (initializationResult.passed()) {
                updateQualtricsProperties(firstAppSession)
                Qualtrics.instance().evaluateTargetingLogic { viewModel.inputs.qualtricsResult(QualtricsResult(it)) }
            }
        }
    }

    private fun updateDrawerMenuIcon(drawerMenuIcon: Int) {
        binding.discoveryToolbar.menuButton.setImageResource(drawerMenuIcon)
        if (binding.discoveryToolbar.menuButton.drawable is Animatable) {
            val menuDrawable = binding.discoveryToolbar.menuButton.drawable as Animatable
            menuDrawable.start()
        }
    }

    protected fun startActivityFeedActivity() {
        startActivity(Intent(this, ActivityFeedActivity::class.java))
    }

    protected fun startCreatorDashboardActivity() {
        startActivity(Intent(this, CreatorDashboardActivity::class.java))
    }

    protected fun startHelpSettingsActivity() {
        startActivity(Intent(this, HelpSettingsActivity::class.java))
    }

    private fun startLoginToutActivity() {
        val intent = Intent(this, LoginToutActivity::class.java)
            .putExtra(IntentKey.LOGIN_REASON, LoginReason.DEFAULT)
        startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW)
        TransitionUtils.transition(this, TransitionUtils.slideInFromRight())
    }

    private fun startMessageThreadsActivity() {
        val intent = Intent(this, MessageThreadsActivity::class.java)
            .putExtra(IntentKey.KOALA_CONTEXT, KoalaContext.Mailbox.DRAWER)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun startProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }

    private fun showQualtricsSurvey(surveyUrl: String) {
        val surveyIntent = Intent(this, QualtricsSurveyActivity::class.java)
            .putExtra("targetURL", surveyUrl)
        startActivity(surveyIntent)
    }

    private fun startSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private fun showBuildAlert(envelope: InternalBuildEnvelope) {
        AlertDialog.Builder(this)
            .setTitle(R.string.Upgrade_app)
            .setMessage(getString(R.string.A_newer_build_is_available))
            .setPositiveButton(android.R.string.yes) { dialog: DialogInterface?, which: Int ->
                val intent = Intent(this, DownloadBetaActivity::class.java)
                    .putExtra(IntentKey.INTERNAL_BUILD_ENVELOPE, envelope)
                startActivity(intent)
            }
            .setNegativeButton(android.R.string.cancel) { dialog: DialogInterface?, which: Int -> }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun updateImpressionCount(qualtricsIntercept: QualtricsIntercept) {
        val key = qualtricsIntercept.impressionCountKey(BuildConfig.APPLICATION_ID)
        val initialCount = ObjectUtils.coalesce(Qualtrics.instance().properties.getNumber(key), 0.0)
        Qualtrics.instance().properties.setNumber(key, initialCount + 1.0)
    }

    private fun updateQualtricsProperties(firstAppSession: Boolean) {
        Qualtrics.instance().properties.setString("first_app_session", java.lang.Boolean.toString(firstAppSession))
        Qualtrics.instance().properties.setString("language", Locale.getDefault().language)
        Qualtrics.instance().properties.setString("package_name", BuildConfig.APPLICATION_ID)
    }

    override fun onDestroy() {
        viewModel = null
        super.onDestroy()
    }

    companion object {
        private fun createFragments(pages: Int): List<DiscoveryFragment> {
            val fragments: MutableList<DiscoveryFragment> = ArrayList(pages)
            for (position in 0..pages) {
                fragments.add(newInstance(position))
            }
            return fragments
        }
    }
}
