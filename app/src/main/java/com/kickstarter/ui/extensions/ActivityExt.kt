package com.kickstarter.ui.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Pair
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.AnimRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MessagePreviousScreenType
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.TransitionUtils
import com.kickstarter.libs.utils.UrlUtils
import com.kickstarter.libs.utils.extensions.getCreatorBioWebViewActivityIntent
import com.kickstarter.libs.utils.extensions.getPledgeRedemptionIntent
import com.kickstarter.libs.utils.extensions.getPreLaunchProjectActivity
import com.kickstarter.libs.utils.extensions.getProjectUpdatesActivityIntent
import com.kickstarter.libs.utils.extensions.getReportProjectActivityIntent
import com.kickstarter.libs.utils.extensions.getRootCommentsActivityIntent
import com.kickstarter.libs.utils.extensions.getUpdatesActivityIntent
import com.kickstarter.libs.utils.extensions.getVideoActivityIntent
import com.kickstarter.libs.utils.extensions.reduceProjectPayload
import com.kickstarter.libs.utils.extensions.withData
import com.kickstarter.models.Project
import com.kickstarter.models.chrome.ChromeTabsHelperActivity
import com.kickstarter.services.ConnectivityReceiver
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.DisclaimerItems
import com.kickstarter.ui.activities.HelpActivity
import com.kickstarter.ui.activities.LoginToutActivity
import com.kickstarter.ui.activities.MessagesActivity
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.fragments.CrowdfundCheckoutFragment
import com.kickstarter.ui.fragments.PledgeFragment
import timber.log.Timber

fun Activity.startActivityWithTransition(
    intent: Intent,
    @AnimRes enterAnim: Int,
    @AnimRes exitAnim: Int
) {
    startActivity(intent)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(
            ComponentActivity.OVERRIDE_TRANSITION_OPEN,
            enterAnim,
            exitAnim
        )
    } else {
        overridePendingTransition(enterAnim, exitAnim)
    }
}

fun Activity.hideKeyboard() {
    val inputManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (inputManager.isAcceptingText) {
        inputManager.hideSoftInputFromWindow(
            currentFocus?.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
        currentFocus?.clearFocus()
    }
}

fun Activity.selectPledgeFragment(
    pledgeData: PledgeData,
    pledgeReason: PledgeReason,
    ffEnabled: Boolean = false
): Fragment {
    val fragment = when (pledgeReason) {
        PledgeReason.FIX_PLEDGE ->
            if (ffEnabled) CrowdfundCheckoutFragment()
            else PledgeFragment()
        else -> CrowdfundCheckoutFragment()
    }
    return fragment.withData(pledgeData, pledgeReason)
}

fun Activity.showSnackbar(anchor: View, stringResId: Int) {
    showSnackbar(anchor, getString(stringResId))
}

fun showSnackbar(anchor: View, message: String) {
    snackbar(anchor, message).show()
}

fun Activity.showSuccessSnackBar(anchor: View, message: String) {
    val backgroundColor = this.resources.getColor(R.color.kds_create_300, this.theme)
    val textColor = this.resources.getColor(R.color.kds_support_700, this.theme)
    showSnackbarWithColor(anchor, message, backgroundColor, textColor)
}

fun Activity.showErrorSnackBar(anchor: View, message: String) {
    val backgroundColor = this.resources.getColor(R.color.kds_alert, this.theme)
    val textColor = this.resources.getColor(R.color.kds_white, this.theme)
    showSnackbarWithColor(anchor, message, backgroundColor, textColor)
}

fun Activity.setUpConnectivityStatusCheck(lifecycle: Lifecycle) {
    val callBack = object : ConnectivityReceiver.ConnectivityReceiverListener {
        override fun onNetworkConnectionChanged(isConnected: Boolean) {
            if (!isConnected) {
                showSnackbar(findViewById(android.R.id.content), getString(R.string.Youre_offline))
            }
        }
    }
    val connectivityReceiver = ConnectivityReceiver(callBack, this)
    lifecycle.addObserver(connectivityReceiver)
}

fun Activity.showRatingDialogWidget() {
    val manager = ReviewManagerFactory.create(this)
    val requestReviewTask = manager.requestReviewFlow()

    requestReviewTask.addOnCompleteListener { request ->
        if (request.isSuccessful) {
            Timber.v("${this.localClassName} : showRatingDialogWidget request: ${request.isSuccessful} ")
            // Request succeeded and a ReviewInfo instance was received
            val reviewInfo: ReviewInfo = request.result

            // Start the review flow UI
            val flow = manager.launchReviewFlow(this, reviewInfo)

            flow.addOnSuccessListener {
                Timber.v("${this.localClassName} : showRatingDialogWidget launchReviewFlow: Success")
            }

            flow.addOnFailureListener {
                Timber.v("${this.localClassName} : showRatingDialogWidget launchReviewFlow: Failure")
            }

            flow.addOnCompleteListener {
                Timber.v("${this.localClassName} : showRatingDialogWidget launchReviewFlow: Complete")
            }
        } else {
            Timber.v("${this.localClassName} : showRatingDialogWidget request: ${request.isSuccessful} ")
        }
    }
}

fun Activity.startPledgeRedemption(project: Project) {
    startActivity(
        Intent().getPledgeRedemptionIntent(this, project)
    )

    this.let {
        TransitionUtils.transition(it, TransitionUtils.slideInFromRight())
    }
}

/**
 * This function starts the RootCommentActivity with Transition animation included
 * @param projectData
 * @param commentableId -> specific for deeplinking to a concrete thread
 */
fun Activity.startRootCommentsActivity(projectData: ProjectData, commentableId: String? = null) {
    val reducedProject = projectData.project().reduceProjectPayload()
    startActivity(
        Intent().getRootCommentsActivityIntent(this, projectData.toBuilder().project(reducedProject).build(), commentableId)
    )

    this.let {
        TransitionUtils.transition(it, TransitionUtils.slideInFromRight())
    }
}

fun Activity.startLoginActivity() {
    startActivity(Intent(this, LoginToutActivity::class.java))
}

fun Activity.startReportProjectActivity(
    project: Project,
    startForResult: ActivityResultLauncher<Intent>
) {
    startForResult.launch(Intent().getReportProjectActivityIntent(this, project = project.reduceProjectPayload()))
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
}

fun Activity.startCreatorBioWebViewActivity(project: Project) {
    startActivity(Intent().getCreatorBioWebViewActivityIntent(this, project.reduceProjectPayload()))
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
}

fun Activity.startVideoActivity(
    startForResult: ActivityResultLauncher<Intent>,
    videoSource: String,
    videoSeekPosition: Long
) {
    startForResult.launch(Intent().getVideoActivityIntent(this, videoSource, videoSeekPosition))
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
}

/**
 * This function starts the General Updates activity with Transition animation included
 * the Transition occurs slide_in_right -> fade_out_slide_out_left
 * @param project
 * @param updatePostId -> for deeplink to an specific post update
 * @param isUpdateComment -> for deeplink navigation into a comment for a concrete post
 * @param comment -> to open the comments activity to a specific thread
 */
fun Activity.startUpdatesActivity(
    project: Project,
    updatePostId: String? = null,
    isUpdateComment: Boolean? = null,
    comment: String? = null
) {
    startActivity(
        Intent().getUpdatesActivityIntent(
            this,
            project,
            updatePostId,
            isUpdateComment,
            comment
        )
    )
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
}

/**
 * This function starts the Project Updates activity with Transition animation included
 * the Transition occurs slide_in_right -> fade_out_slide_out_left
 * @param projectAndData
 */
fun Activity.startProjectUpdatesActivity(projectAndData: ProjectData) {
    val reducedProject = projectAndData.project().reduceProjectPayload()
    startActivity(Intent().getProjectUpdatesActivityIntent(this, projectAndData.toBuilder().project(reducedProject).build()))
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
}

fun Activity.finishWithAnimation(withResult: String? = null, intentKey: String? = null) {
    intentKey?.let { key ->
        withResult?.let { result ->
            val intent = Intent().putExtra(key, result)
            setResult(Activity.RESULT_OK, intent)
        }
    }
    finish()
    TransitionUtils.transition(this, TransitionUtils.slideInFromLeft())
}

fun Activity.transition(transition: Pair<Int, Int>) {
    overridePendingTransition(transition.first, transition.second)
}

@SuppressLint("IntentWithNullActionLaunch") // Lint bug: https://issuetracker.google.com/issues/294200850
fun Activity.startPreLaunchProjectActivity(uri: Uri, project: Project, previousScreen: String? = null) {
    val intent = Intent().getPreLaunchProjectActivity(
        this,
        project.slug(),
        project.reduceProjectPayload()
    )
    // Pass full deeplink for attribution tracking purposes when launching from deeplink
    intent.setData(uri)
    val ref = UrlUtils.refTag(uri.toString())
    ref?.let { intent.putExtra(IntentKey.REF_TAG, RefTag.from(ref)) }
    previousScreen?.let { intent.putExtra(IntentKey.PREVIOUS_SCREEN, it) }
    startActivity(intent)
    TransitionUtils.transition(this, TransitionUtils.slideInFromRight())
}

fun Activity.startDisclaimerChromeTab(disclaimerItem: DisclaimerItems, environment: Environment?) {
    val path = when (disclaimerItem) {
        DisclaimerItems.TERMS -> HelpActivity.TERMS_OF_USE
        DisclaimerItems.PRIVACY -> HelpActivity.PRIVACY
        DisclaimerItems.COOKIES -> HelpActivity.COOKIES
        DisclaimerItems.HELP -> Secrets.HelpCenter.ENDPOINT
    }

    val url = if (disclaimerItem == DisclaimerItems.HELP) {
        path
    } else environment?.let {
        UrlUtils.appendPath(it.webEndpoint(), path)
    } ?: run {
        ""
    }

    val intent = when (disclaimerItem) {
        DisclaimerItems.TERMS -> Intent(this, HelpActivity.Terms::class.java)
        DisclaimerItems.PRIVACY -> Intent(this, HelpActivity.Privacy::class.java)
        DisclaimerItems.COOKIES -> Intent(this, HelpActivity.CookiePolicy::class.java)
        DisclaimerItems.HELP -> Intent(Intent.ACTION_VIEW, Uri.parse(Secrets.HelpCenter.ENDPOINT))
    }

    val uri = Uri.parse(url)

    val fallback = object : ChromeTabsHelperActivity.CustomTabFallback {
        override fun openUri(activity: Activity, uri: Uri) {
            activity.startActivity(intent)
        }
    }

    ChromeTabsHelperActivity.openCustomTab(this, UrlUtils.baseCustomTabsIntent(this), uri, fallback)
}

fun Activity.startCreatorMessageActivity(project: Project, previousScreen: MessagePreviousScreenType) {
    startActivity(
        Intent(this, MessagesActivity::class.java)
            .putExtra(IntentKey.MESSAGE_SCREEN_SOURCE_CONTEXT, previousScreen)
            .putExtra(IntentKey.PROJECT, project)
            .putExtra(IntentKey.BACKING, project.backing())
    )
}
