package com.kickstarter.libs.utils

import androidx.fragment.app.FragmentManager
import com.kickstarter.R
import com.kickstarter.libs.Config
import com.kickstarter.libs.utils.extensions.EMAIL_VERIFICATION_FLOW
import com.kickstarter.libs.utils.extensions.isFeatureFlagEnabled
import com.kickstarter.models.User
import com.kickstarter.models.extensions.isUserEmailVerified
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.ui.fragments.Callbacks
import com.kickstarter.ui.fragments.EmailVerificationInterstitialFragment

object LoginHelper {

    /**
     * Takes and user and a configuration:
     * @param user User
     * @param config Config
     *
     * @return Boolean?
     *     True in case feature flag active and verified
     *     False in case feature flag active and not verified
     *     True in case not active feature flag
     *     Null in case no current User (not logged)
     *
     * Note: this method should be deleted once EMAIL_VERIFICATION_FLOW feature
     * flag is no longer needed.
     */
    fun hasCurrentUserVerifiedEmail(user: User?, config: Config): Boolean? {
        if (user == null) {
            return null
        }

        return if (config.isFeatureFlagEnabled(EMAIL_VERIFICATION_FLOW)) {
            user.isUserEmailVerified()
        } else true
    }

    /**
     * Start the transition to present the EmailVerificationInterstitialFragment
     * This will be called for Login flows and Account creation flows.
     *
     * @param supportFragmentManager
     * @param user
     * @param R.id.loginViewId
     * @param callbacks callback for dismissing the stack of
     * screens -> LoginToutActivity -> LoginActivity -> InterstitialFragment
     *         -> LoginToutActivity -> SignupActivity -> InterstitialFragment
     *
     * Note: R.id.viewId Container ID for the fragment:
     * layout/login_layout.xml
     * layout/signup_layout.xml
     */
    fun showInterstitialFragment(supportFragmentManager: FragmentManager, envelope: AccessTokenEnvelope, loginViewId: Int, callbacks: Callbacks) {
        if (supportFragmentManager.findFragmentByTag(EmailVerificationInterstitialFragment::class.java.simpleName) == null) {
            val emailValidationFragment: EmailVerificationInterstitialFragment = EmailVerificationInterstitialFragment.newInstance(envelope)
            val presented = supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                    .add(loginViewId, emailValidationFragment)
                    .addToBackStack(null)
                    .commit()

            if (presented >= 0) {
                emailValidationFragment.configureWithCallback(callbacks)
            }
        }
    }
}