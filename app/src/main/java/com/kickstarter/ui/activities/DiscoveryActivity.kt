package com.kickstarter.ui.activities

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Animatable
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.kickstarter.R
import com.kickstarter.databinding.DiscoveryLayoutBinding
import com.kickstarter.features.pledgedprojectsoverview.ui.PledgedProjectsOverviewActivity
import com.kickstarter.libs.Environment
import com.kickstarter.libs.InternalToolsType
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.checkPermissions
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.libs.utils.extensions.positionFromSort
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.adapters.DiscoveryDrawerAdapter
import com.kickstarter.ui.adapters.DiscoveryPagerAdapter
import com.kickstarter.ui.data.LoginReason
import com.kickstarter.ui.extensions.showErrorSnackBar
import com.kickstarter.ui.extensions.showSuccessSnackBar
import com.kickstarter.ui.extensions.startActivityWithTransition
import com.kickstarter.ui.fragments.ConsentManagementDialogFragment
import com.kickstarter.ui.fragments.DiscoveryFragment
import com.kickstarter.ui.fragments.DiscoveryFragment.Companion.newInstance
import com.kickstarter.utils.WindowInsetsUtil
import com.kickstarter.viewmodels.DiscoveryViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class DiscoveryActivity : AppCompatActivity() {
    private lateinit var drawerAdapter: DiscoveryDrawerAdapter
    private lateinit var drawerLayoutManager: LinearLayoutManager
    private lateinit var pagerAdapter: DiscoveryPagerAdapter
    private lateinit var consentManagementDialogFragment: ConsentManagementDialogFragment
    private var internalTools: InternalToolsType? = null
    private lateinit var binding: DiscoveryLayoutBinding
    private lateinit var viewModelFactory: DiscoveryViewModel.Factory
    private val viewModel: DiscoveryViewModel.DiscoveryViewModel by viewModels { viewModelFactory }
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        binding = DiscoveryLayoutBinding.inflate(layoutInflater)
        WindowInsetsUtil.manageEdgeToEdge(
            window,
            binding.root
        )
        setContentView(binding.root)
        getEnvironment()?.let { env ->
            viewModelFactory = DiscoveryViewModel.Factory(env)

            if (savedInstanceState == null) {
                activateFeatureFlags(env)
            }

            internalTools = env.internalTools()
        }

        viewModel.provideIntent(intent)

        // TODO: Replace with compose implementation
        val nightModeFlags =
            this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        viewModel.setDarkTheme(
            when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    true
                }

                else -> false
            }
        )
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

        drawerLayoutManager = LinearLayoutManager(this)

        binding.discoveryDrawerRecyclerView.layoutManager = drawerLayoutManager
        drawerAdapter = DiscoveryDrawerAdapter(
            viewModel.inputs,
        )
        binding.discoveryDrawerRecyclerView.adapter = drawerAdapter

        val viewPagerTitles = listOf(
            getString(R.string.discovery_sort_types_magic),
            getString(R.string.Popular),
            getString(R.string.discovery_sort_types_newest),
            getString(R.string.Ending_soon)
        )

        pagerAdapter = DiscoveryPagerAdapter(
            supportFragmentManager,
            createFragments(viewPagerTitles.size).toMutableList(),
            viewPagerTitles,
            viewModel.inputs
        )

        binding.discoveryViewPager.adapter = pagerAdapter
        binding.discoveryTabLayout.setupWithViewPager(binding.discoveryViewPager)

        addTabSelectedListenerToTabLayout()

        viewModel.outputs.expandSortTabLayout()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.discoverySortAppBarLayout.setExpanded(it) }
            .addToDisposable(disposables)

        viewModel.outputs.updateToolbarWithParams()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.discoveryToolbar.discoveryToolbar.loadParams(it) }
            .addToDisposable(disposables)

        viewModel.outputs.updateParamsForPage()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.discoveryViewPager.currentItem = it.sort().positionFromSort()
                pagerAdapter.takeParams(it)
            }
            .addToDisposable(disposables)

        viewModel.outputs.showNotifPermissionsRequest()
            .distinctUntilChanged()
            .filter {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    this.checkPermissions(Manifest.permission.POST_NOTIFICATIONS)
            }
            .delay(2000, TimeUnit.MILLISECONDS)
            .subscribe {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                viewModel.inputs.hasSeenNotificationsPermission(true)
            }
            .addToDisposable(disposables)

        viewModel.outputs.showConsentManagementDialog()
            .distinctUntilChanged()
            .subscribe {
                consentManagementDialogFragment = ConsentManagementDialogFragment()
                consentManagementDialogFragment.isCancelable = false
                consentManagementDialogFragment.show(
                    supportFragmentManager,
                    "consentManagementDialogFragment"
                )
            }
            .addToDisposable(disposables)

        viewModel.outputs.clearPages()
            .compose<List<Int?>>(Transformers.observeForUIV2())
            .subscribe { pagerAdapter.clearPages(it) }
            .addToDisposable(disposables)

        viewModel.outputs.rootCategoriesAndPosition()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { pagerAdapter.takeCategoriesForPosition(it.first, it.second) }
            .addToDisposable(disposables)

        viewModel.outputs.showActivityFeed()
            .compose(Transformers.observeForUIV2())
            .subscribe { startActivityFeedActivity() }
            .addToDisposable(disposables)

        viewModel.outputs.showHelp()
            .compose(Transformers.observeForUIV2())
            .subscribe { startHelpSettingsActivity() }
            .addToDisposable(disposables)

        viewModel.outputs.showInternalTools()
            .compose(Transformers.observeForUIV2())
            .subscribe {
                internalTools?.maybeStartInternalToolsActivity(this)
            }
            .addToDisposable(disposables)

        viewModel.outputs.showLoginTout()
            .compose(Transformers.observeForUIV2())
            .subscribe { startLoginToutActivity() }
            .addToDisposable(disposables)

        viewModel.outputs.showMessages()
            .compose(Transformers.observeForUIV2())
            .subscribe { startMessageThreadsActivity() }
            .addToDisposable(disposables)

        viewModel.outputs.showProfile()
            .compose(Transformers.observeForUIV2())
            .subscribe { startProfileActivity() }
            .addToDisposable(disposables)

        viewModel.outputs.showPledgedProjects()
            .compose(Transformers.observeForUIV2())
            .subscribe { startPledgedProjectsOverview() }
            .addToDisposable(disposables)

        viewModel.outputs.showSettings()
            .compose(Transformers.observeForUIV2())
            .subscribe { startSettingsActivity() }
            .addToDisposable(disposables)

        viewModel.outputs.navigationDrawerData()
            .compose(Transformers.observeForUIV2())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { drawerAdapter.takeData(it) }
            .addToDisposable(disposables)

        viewModel.closeDrawer()
            .compose(Transformers.observeForUIV2())
            .subscribe { binding.discoveryDrawerLayout.closeDrawer(GravityCompat.START) }
            .addToDisposable(disposables)

        viewModel.outputs.drawerMenuIcon()
            .compose(Transformers.observeForUIV2())
            .subscribe { drawerMenuIcon: Int -> updateDrawerMenuIcon(drawerMenuIcon) }
            .addToDisposable(disposables)

        viewModel.outputs.showSuccessMessage()
            .compose(Transformers.observeForUIV2())
            .subscribe { this@DiscoveryActivity.showSuccessSnackBar(binding.discoveryAnchorView, it) }
            .addToDisposable(disposables)

        viewModel.outputs.showErrorMessage()
            .compose(Transformers.observeForUIV2())
            .subscribe { this@DiscoveryActivity.showErrorSnackBar(binding.discoveryAnchorView, it ?: "") }
            .addToDisposable(disposables)
    }

    private fun activateFeatureFlags(environment: Environment) {
        environment.featureFlagClient()?.activate(this)
    }

    fun discoveryLayout(): DrawerLayout {
        return binding.discoveryDrawerLayout
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

    private fun updateDrawerMenuIcon(drawerMenuIcon: Int) {
        binding.discoveryToolbar.menuButton.setImageResource(drawerMenuIcon)
        if (binding.discoveryToolbar.menuButton.drawable is Animatable) {
            val menuDrawable = binding.discoveryToolbar.menuButton.drawable as Animatable
            menuDrawable.start()
        }
    }

    private fun startActivityFeedActivity() {
        startActivity(Intent(this, ActivityFeedActivity::class.java))
    }

    private fun startHelpSettingsActivity() {
        startActivity(Intent(this, HelpSettingsActivity::class.java))
    }

    private fun startLoginToutActivity() {
        val intent = Intent(this, LoginToutActivity::class.java)
            .putExtra(IntentKey.LOGIN_REASON, LoginReason.DEFAULT)
        this.startActivityWithTransition(
            intent,
            R.anim.slide_in_right,
            R.anim.fade_out_slide_out_left
        )
    }

    private fun startMessageThreadsActivity() {
        val intent = Intent(this, MessageThreadsActivity::class.java)
        this.startActivityWithTransition(
            intent,
            R.anim.slide_in_right,
            R.anim.fade_out_slide_out_left
        )
    }

    private fun startProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        this.startActivityWithTransition(
            intent,
            R.anim.slide_in_right,
            R.anim.fade_out_slide_out_left
        )
    }

    private fun startPledgedProjectsOverview() {
        val intent = Intent(this, PledgedProjectsOverviewActivity::class.java)
        this.startActivityWithTransition(
            intent,
            R.anim.slide_in_right,
            R.anim.fade_out_slide_out_left
        )
    }

    private fun startSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        this.startActivityWithTransition(
            intent,
            0,
            0
        )
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
