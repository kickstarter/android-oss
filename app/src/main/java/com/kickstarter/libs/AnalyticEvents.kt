package com.kickstarter.libs

import com.kickstarter.libs.KoalaContext.*
import com.kickstarter.libs.KoalaEvent.ProjectAction
import com.kickstarter.libs.utils.EventContext.CtaContextName.ADD_ONS_CONTINUE
import com.kickstarter.libs.utils.EventContext.CtaContextName.PLEDGE_INITIATE
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.AnalyticEventsUtils
import com.kickstarter.libs.utils.ExperimentData
import com.kickstarter.models.Activity
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.PushNotificationEnvelope
import com.kickstarter.ui.data.*
import com.kickstarter.ui.data.Mailbox
import kotlin.collections.HashMap

private const val CONTEXT_CTA = "context_cta"

class AnalyticEvents(trackingClients: List<TrackingClientType?>) {

    private val client = ProxyClient(trackingClients)

    // APPLICATION LIFECYCLE
    fun trackAppOpen() {
        client.track("App Open")
    }

    fun trackAppClose() {
        client.track("App Close")
    }

    fun trackMemoryWarning() {
        client.track("App Memory Warning")
    }

    fun trackOpenedAppBanner() {
        client.track("Opened App Banner")
    }

    // BACKING
    fun trackViewedPledgeInfo(project: Project) {
        client.track(KoalaEvent.VIEWED_PLEDGE_INFO, AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
    }

    // DISCOVERY
    fun trackDiscovery(params: DiscoveryParams, isOnboardingVisible: Boolean) {
        val props = AnalyticEventsUtils.discoveryParamsProperties(params).toMutableMap()
        props["discover_onboarding_is_visible"] = isOnboardingVisible
        client.track("Discover List View", props)
    }

    fun trackDiscoveryFilters() {
        val properties = hashMapOf<String, Any>()
        properties["modal_type"] = "filters"
        client.track("Discover Switch Modal", properties)
    }

    fun trackDiscoveryFilterSelected(params: DiscoveryParams) {
        client.track("Discover Modal Selected Filter", AnalyticEventsUtils.discoveryParamsProperties(params))
    }

    fun trackDiscoveryRefreshTriggered() {
        val properties = hashMapOf<String, Any>()
        properties["type"] = "swipe"
        client.track(KoalaEvent.TRIGGERED_REFRESH, properties)
    }

    /**
     * Tracks a project show event.
     *
     * @param projectData The Intent RefTag is the (nullable) RefTag present in the activity upon displaying the project.
     * The Cookie RefTag is the (nullable) RefTag extracted from the cookie store upon viewing the project.
     */
    fun trackProjectShow(projectData: ProjectData) {
        val properties = AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser())
        properties.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        client.track(KoalaEvent.PROJECT_PAGE, properties)
    }

    fun trackProjectActionButtonClicked(@ProjectAction eventName: String, project: Project) {
        val properties = AnalyticEventsUtils.projectProperties(project, client.loggedInUser())
        client.track(eventName, properties)
    }

    fun trackSelectRewardButtonClicked(project: Project, rewardMinimum: Int, rewardPosition: Int) {
        val properties = AnalyticEventsUtils.projectProperties(project, client.loggedInUser())
        properties["backer_reward_minimum"] = rewardMinimum
        properties["reward_position"] = rewardPosition
        client.track(KoalaEvent.SELECT_REWARD_BUTTON_CLICKED, properties)
    }

    fun trackCancelPledgeButtonClicked(project: Project) {
        val properties = AnalyticEventsUtils.projectProperties(project, client.loggedInUser())
        client.track(KoalaEvent.CANCEL_PLEDGE_BUTTON_CLICKED, properties)
    }

    // PROJECT STAR
    fun trackProjectStar(project: Project) {
        val props = AnalyticEventsUtils.projectProperties(project, client.loggedInUser())

        // Deprecated events
        client.track(if (project.isStarred) KoalaEvent.PROJECT_STAR else KoalaEvent.PROJECT_UNSTAR, props)
        client.track(if (project.isStarred) KoalaEvent.STARRED_PROJECT else KoalaEvent.UNSTARRED_PROJECT, props)
    }

    // PROJECT CREATOR BIO
    fun trackViewedCreatorBioModal(project: Project) {
        val loggedInUser = client.loggedInUser()
        val props = AnalyticEventsUtils.projectProperties(project, loggedInUser)
        props["modal_title"] = "creatorBioModal"
        client.track(KoalaEvent.MODAL_DIALOG_VIEW, props)
    }

    fun trackViewedMessageCreatorModal(project: Project) {
        val loggedInUser = client.loggedInUser()
        val props = AnalyticEventsUtils.projectProperties(project, loggedInUser)
        props["modal_title"] = "messageCreatorModal"
        client.track(KoalaEvent.MODAL_DIALOG_VIEW, props)
    }

    // COMMENTS
    fun trackLoadedOlderComments(project: Project, update: com.kickstarter.models.Update?, context: Comments) {
        val loggedInUser = client.loggedInUser()

        val props = update?.let {
            AnalyticEventsUtils.updateProperties(project, it, loggedInUser).toMutableMap()
        } ?: AnalyticEventsUtils.projectProperties(project, loggedInUser).toMutableMap()

        props["context"] = context.trackingString
        client.track(KoalaEvent.LOADED_OLDER_COMMENTS, props)
    }

    @Deprecated("Use {@link #trackLoadedOlderComments(Project, Update, KoalaContext.Comments)} instead.")
    fun trackLoadedOlderProjectComments(project: Project) {
        client.track(KoalaEvent.PROJECT_COMMENT_LOAD_OLDER, AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
    }

    fun trackPostedComment(project: Project, update: com.kickstarter.models.Update?,
                           context: CommentDialog) {
        val loggedInUser = client.loggedInUser()

        val props = update?.let {
            AnalyticEventsUtils.updateProperties(project, it, loggedInUser).toMutableMap()
        } ?: AnalyticEventsUtils.projectProperties(project, loggedInUser).toMutableMap()

        props["context"] = context.trackingString
        client.track(KoalaEvent.POSTED_COMMENT, props)
    }

    @Deprecated("Use {@link #trackPostedComment(Project, Update, KoalaContext.CommentDialog)} instead.")
    fun trackProjectCommentCreate(project: Project) {
        client.track(KoalaEvent.PROJECT_COMMENT_CREATE, AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
    }

    @Deprecated("Use {@link #trackViewedComments(Project, Update, KoalaContext.Comments)} instead.")
    fun trackProjectCommentsView(project: Project) {
        client.track(KoalaEvent.PROJECT_COMMENT_VIEW, AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
    }

    fun trackViewedComments(project: Project, update: com.kickstarter.models.Update?,
                            context: Comments) {
        val loggedInUser = client.loggedInUser()

        val props = update?.let {
            AnalyticEventsUtils.updateProperties(project, it, loggedInUser).toMutableMap()
        } ?: AnalyticEventsUtils.projectProperties(project, loggedInUser).toMutableMap()

        props["context"] = context.trackingString
        client.track(KoalaEvent.VIEWED_COMMENTS, props)
    }

    // ACTIVITY
    fun trackActivityView(pageCount: Int) {
        if (pageCount == 0) {
            client.track(KoalaEvent.ACTIVITY_VIEW)
        } else {
            val properties = hashMapOf<String, Any>()
            properties["page_count"] = pageCount
            client.track(KoalaEvent.ACTIVITY_LOAD_MORE, properties)
        }
    }

    // SEARCH
    fun trackSearchView() {
        client.track(KoalaEvent.VIEWED_SEARCH)
        // deprecated
        client.track(KoalaEvent.DISCOVER_SEARCH_LEGACY)
    }

    fun trackSearchResults(query: String, pageCount: Int) {
        if (pageCount == 1) {
            val params = hashMapOf<String, Any>()
            params["search_term"] = query

            client.track(KoalaEvent.LOADED_SEARCH_RESULTS, params)
            // deprecated
            client.track(KoalaEvent.DISCOVER_SEARCH_RESULTS_LEGACY, params)
        } else {
            val params = hashMapOf<String, Any>()
            params["search_term"] = query
            params["page_count"] = pageCount

            client.track(KoalaEvent.LOADED_MORE_SEARCH_RESULTS, params)
            // deprecated
            client.track(KoalaEvent.DISCOVER_SEARCH_RESULTS_LOAD_MORE_LEGACY, params)
        }
    }

    fun trackClearedSearchTerm() {
        client.track(KoalaEvent.CLEARED_SEARCH_TERM)
    }

    fun trackActivityTapped(activity: Activity) {
        client.track(KoalaEvent.ACTIVITY_VIEW_ITEM, AnalyticEventsUtils.activityProperties(activity, client.loggedInUser()))
    }

    // SESSION EVENTS
    fun trackLoginRegisterTout(loginReason: LoginReason) {
        val properties = hashMapOf<String, Any>()
        properties["intent"] = loginReason.trackingString()

        client.track("Application Login or Signup", properties)
    }

    fun trackLoginSuccess() {
        client.track(KoalaEvent.LOGIN)
    }

    fun trackLoginError() {
        client.track(KoalaEvent.ERRORED_USER_LOGIN)
    }

    fun trackTwoFactorAuthView() {
        client.track(KoalaEvent.TWO_FACTOR_AUTH_CONFIRM_VIEW)
    }

    fun trackTwoFactorResendCode() {
        client.track(KoalaEvent.TWO_FACTOR_AUTH_RESEND_CODE)
    }

    fun trackRegisterFormView() {
        client.track(KoalaEvent.USER_SIGNUP)
    }

    fun trackRegisterError() {
        client.track(KoalaEvent.ERRORED_USER_SIGNUP)
    }

    fun trackRegisterSuccess() {
        client.track(KoalaEvent.NEW_USER)
    }

    fun trackResetPasswordFormView() {
        client.track("Forgot Password View")
    }

    fun trackResetPasswordSuccess() {
        client.track("Forgot Password Requested")
    }

    fun trackResetPasswordError() {
        client.track("Forgot Password Errored")
    }

    fun trackFacebookConfirmation() {
        client.track(KoalaEvent.FACEBOOK_CONFIRM)
    }

    fun trackFacebookLoginError() {
        client.track("Errored Facebook Login")
    }

    fun trackLogout() {
        client.track("Logout")
    }

    fun trackSignupNewsletterToggle(sendNewsletters: Boolean) {
        val properties = hashMapOf<String, Any>()
        properties["send_newsletters"] = sendNewsletters
        client.track(KoalaEvent.SIGNUP_NEWSLETTER_TOGGLE, properties)
    }

    // SETTINGS
    fun trackChangedEmail() {
        client.track(KoalaEvent.CHANGED_EMAIL)
    }

    fun trackChangedPassword() {
        client.track(KoalaEvent.CHANGED_PASSWORD)
    }

    fun trackContactEmailClicked() {
        client.track("Contact Email Clicked")
    }

    fun trackCreatedPassword() {
        client.track(KoalaEvent.CREATED_PASSWORD)
    }

    fun trackDeletePaymentMethod() {
        client.track(KoalaEvent.DELETED_PAYMENT_METHOD)
    }

    fun trackErroredDeletePaymentMethod() {
        client.track(KoalaEvent.ERRORED_DELETE_PAYMENT_METHOD)
    }

    fun trackFailedPaymentMethodCreation() {
        client.track(KoalaEvent.FAILED_PAYMENT_METHOD_CREATION)
    }

    fun trackNewsletterToggle(sendNewsletter: Boolean) {
        if (sendNewsletter) {
            client.track("Newsletter Subscribe")
        } else {
            client.track("Newsletter Unsubscribe")
        }
    }

    fun trackResentVerificationEmail() {
        client.track(KoalaEvent.RESENT_VERIFICATION_EMAIL)
    }

    fun trackSavedPaymentMethod() {
        client.track(KoalaEvent.SAVED_PAYMENT_METHOD)
    }

    fun trackSelectedChosenCurrency(selectedCurrency: String?) {
        val properties = hashMapOf<String, Any>()
        properties["user_chosen_currency"] = selectedCurrency ?: ""
        client.track(KoalaEvent.SELECTED_CHOSEN_CURRENCY, properties)
    }

    fun trackSettingsView() {
        client.track(KoalaEvent.VIEWED_SETTINGS)
    }

    fun trackViewedAccount() {
        client.track(KoalaEvent.VIEWED_ACCOUNT)
    }

    fun trackViewedAddNewCard() {
        client.track(KoalaEvent.VIEWED_ADD_NEW_CARD)
    }

    fun trackViewedChangedEmail() {
        client.track(KoalaEvent.VIEWED_CHANGE_EMAIL)
    }

    fun trackViewedChangedPassword() {
        client.track(KoalaEvent.VIEWED_CHANGE_PASSWORD)
    }

    fun trackViewedCreatedPassword() {
        client.track(KoalaEvent.VIEWED_CREATE_PASSWORD)
    }

    fun trackViewedNotifications() {
        client.track(KoalaEvent.VIEWED_NOTIFICATIONS)
    }

    fun trackViewedNewsletter() {
        client.track(KoalaEvent.VIEWED_NEWSLETTER)
    }

    fun trackViewedPaymentMethods() {
        client.track(KoalaEvent.VIEWED_PAYMENT_METHODS)
    }

    fun trackViewedPrivacy() {
        client.track(KoalaEvent.VIEWED_PRIVACY)
    }

    // CHECKOUT
    fun trackCheckoutShowShareSheet() {
        client.track("Checkout Show Share Sheet")
    }

    fun trackCheckoutShowFacebookShareView() {
        val properties = hashMapOf<String, Any>()
        properties.put("share_type", "facebook")
        client.track("Checkout Show Share", properties)
    }

    fun trackCheckoutShowTwitterShareView() {
        val properties = hashMapOf<String, Any>()
        properties["share_type"] = "twitter"
        client.track("Checkout Show Share", properties)
    }

    fun trackCheckoutFinishJumpToDiscovery() {
        client.track("Checkout Finished Discover More")
    }

    fun trackCheckoutFinishJumpToProject(project: Project) {
        val props = AnalyticEventsUtils.projectProperties(project, client.loggedInUser())
        client.track("Checkout Finished Discover Open Project", props)
    }

    fun trackManagePledgeOptionClicked(project: Project, cta: String) {
        val properties = AnalyticEventsUtils.projectProperties(project, client.loggedInUser())
        properties["cta"] = cta
        client.track(KoalaEvent.MANAGE_PLEDGE_OPTION_CLICKED, properties)
    }

    fun trackAddNewCardButtonClicked(project: Project, pledgeTotal: Double) {
        val properties = AnalyticEventsUtils.projectProperties(project, client.loggedInUser())

        //Overwrite the pledge_total with the latest value
        properties["pledge_total"] = pledgeTotal
        client.track(KoalaEvent.ADD_NEW_CARD_BUTTON_CLICKED, properties)
    }

    fun trackPledgeButtonClicked(project: Project, pledgeTotal: Double) {
        val properties = AnalyticEventsUtils.projectProperties(project, client.loggedInUser())

        //Overwrite the pledge_total with the latest value
        properties["pledge_total"] = pledgeTotal
        client.track(KoalaEvent.PLEDGE_BUTTON_CLICKED, properties)
    }

    fun trackUpdatePledgeButtonClicked(project: Project, pledgeTotal: Double) {
        val properties = AnalyticEventsUtils.projectProperties(project, client.loggedInUser())

        //Overwrite the pledge_total with the latest value
        properties["pledge_total"] = pledgeTotal
        client.track(KoalaEvent.UPDATE_PLEDGE_BUTTON_CLICKED, properties)
    }

    fun trackUpdatePaymentMethodButtonClicked(project: Project) {
        val properties = AnalyticEventsUtils.projectProperties(project, client.loggedInUser())
        client.track(KoalaEvent.UPDATE_PAYMENT_METHOD_BUTTON_CLICKED, properties)
    }

    fun trackPledgeScreenViewed(project: Project) {
        val properties = AnalyticEventsUtils.projectProperties(project, client.loggedInUser())
        client.track(KoalaEvent.PLEDGE_SCREEN_VIEWED, properties)
    }

    // SHARE
    fun trackShowProjectShareSheet(project: Project) {
        val props = AnalyticEventsUtils.projectProperties(project, client.loggedInUser())
        props["context"] = Share.PROJECT

        // deprecated
        client.track(KoalaEvent.PROJECT_SHOW_SHARE_SHEET_LEGACY)
        client.track(KoalaEvent.SHOWED_SHARE_SHEET, props)
    }

    // MESSAGES
    fun trackSentMessage(project: Project, context: Message) {
        val props = AnalyticEventsUtils.projectProperties(project, client.loggedInUser())
        props["context"] = context.trackingString
        client.track(KoalaEvent.SENT_MESSAGE, props)
    }

    fun trackViewedMailbox(mailbox: Mailbox, project: Project?,
                           intentRefTag: RefTag?, context: KoalaContext.Mailbox) {
        val props = if (project == null) HashMap() else AnalyticEventsUtils.projectProperties(project, client.loggedInUser())
        props["context"] = context.trackingString
        if (intentRefTag != null) {
            props["ref_tag"] = intentRefTag.tag()
        }
        when (mailbox) {
            Mailbox.INBOX -> client.track(KoalaEvent.VIEWED_MESSAGE_INBOX, props)
            Mailbox.SENT -> client.track(KoalaEvent.VIEWED_SENT_MESSAGES, props)
        }
    }

    fun trackViewedMessageThread(project: Project) {
        client.track(KoalaEvent.VIEWED_MESSAGE_THREAD, AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
    }

    // PROFILE
    fun trackProfileView() {
        // deprecated
        client.track(KoalaEvent.PROFILE_VIEW_MY)
        client.track(KoalaEvent.VIEWED_PROFILE)
    }

    // RATING
    fun trackAppRatingNow() {
        client.track("Checkout Finished Alert App Store Rating Rate Now")
    }

    fun trackAppRatingRemindLater() {
        client.track("Checkout Finished Alert App Store Rating Remind Later")
    }

    fun trackAppRatingNoThanks() {
        client.track("Checkout Finished Alert App Store Rating No Thanks")
    }

    // VIDEO
    fun trackVideoStart(project: Project) {
        client.track("Project Video Start", AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
    }

    // PROJECT UPDATES
    fun trackViewedUpdate(project: Project, context: Update) {
        val props = AnalyticEventsUtils.projectProperties(project, client.loggedInUser())
        props["context"] = context.trackingString
        client.track(KoalaEvent.VIEWED_UPDATE, props)
    }

    fun trackViewedUpdates(project: Project) {
        client.track(KoalaEvent.VIEWED_UPDATES, AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
    }

    // PUSH NOTIFICATIONS
    fun trackPushNotification(envelope: PushNotificationEnvelope) {
        val properties = hashMapOf<String, Any>()

        properties["notification_type"] = "push"
        if (envelope.activity() != null) {
            properties["notification_subject"] = "activity"
            properties["notification_activity_category"] = envelope.activity()?.category() ?: ""
        }

        // deprecated
        client.track(KoalaEvent.NOTIFICATION_OPENED_LEGACY, properties)
        client.track(KoalaEvent.OPENED_NOTIFICATION, properties)
    }

    // WEBVIEWS
    fun trackOpenedExternalLink(project: Project, context: ExternalLink) {
        val props = AnalyticEventsUtils.projectProperties(project, client.loggedInUser())
        props["context"] = context.trackingString
        client.track(KoalaEvent.OPENED_EXTERNAL_LINK, props)
    }

    // DEEP LINK
    fun trackContinueUserActivityAndOpenedDeepLink() {
        client.track(KoalaEvent.CONTINUE_USER_ACTIVITY)
        client.track(KoalaEvent.OPENED_DEEP_LINK)
    }

    // CREATOR DASHBOARD
    fun trackOpenedProjectSwitcher() {
        client.track(KoalaEvent.OPENED_PROJECT_SWITCHER)
    }

    fun trackSwitchedProjects(project: Project) {
        val properties = AnalyticEventsUtils.projectProperties(project, client.loggedInUser())
        client.track(KoalaEvent.SWITCHED_PROJECTS, properties)
    }

    fun trackViewedProjectDashboard(project: Project) {
        val properties = AnalyticEventsUtils.projectProperties(project, client.loggedInUser())
        client.track(KoalaEvent.VIEWED_PROJECT_DASHBOARD, properties)
    }

    //region Discover a Project
    fun trackActivityFeedViewed() {
        client.track(ACTIVITY_FEED_VIEWED)
    }

    fun trackEditorialCardClicked(discoveryParams: DiscoveryParams, editorial: Editorial) {
        val props = AnalyticEventsUtils.discoveryParamsProperties(discoveryParams).toMutableMap()
        props["session_ref_tag"] = RefTag.collection(editorial.tagId).tag()
        client.track(EDITORIAL_CARD_CLICKED, props)
    }

    fun trackExplorePageViewed(discoveryParams: DiscoveryParams) {
        val props = AnalyticEventsUtils.discoveryParamsProperties(discoveryParams)
        client.track(EXPLORE_PAGE_VIEWED, props)
    }

    fun trackExploreSortClicked(discoveryParams: DiscoveryParams) {
        val props = AnalyticEventsUtils.discoveryParamsProperties(discoveryParams)
        client.track(EXPLORE_SORT_CLICKED, props)
    }

    fun trackFilterClicked(discoveryParams: DiscoveryParams) {
        val props = AnalyticEventsUtils.discoveryParamsProperties(discoveryParams)
        client.track(FILTER_CLICKED, props)
    }

    fun trackHamburgerMenuClicked(discoveryParams: DiscoveryParams) {
        val props = AnalyticEventsUtils.discoveryParamsProperties(discoveryParams)
        client.track(HAMBURGER_MENU_CLICKED, props)
    }

    fun trackProjectPageViewed(projectData: ProjectData, pledgeFlowContext: PledgeFlowContext?) {
        val props = AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser())
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        if (pledgeFlowContext != null) {
            props["context_pledge_flow"] = pledgeFlowContext.trackingString
        }
        client.track(PROJECT_PAGE_VIEWED, props)
    }

    fun trackSearchButtonClicked() {
        client.track(SEARCH_BUTTON_CLICKED)
    }

    /**
     * Sends data associated with the search CTA click event to the client.
     *
     * @param discoveryParams: The discovery data.
     */
    fun trackSearchCTAButtonClicked(discoveryParams: DiscoveryParams) {
        val props = AnalyticEventsUtils.discoveryParamsProperties(discoveryParams)
        client.track(EventName.CTA_CLICKED.eventName, props)
    }

    fun trackSearchPageViewed(discoveryParams: DiscoveryParams) {
        val props = AnalyticEventsUtils.discoveryParamsProperties(discoveryParams)
        client.track(SEARCH_PAGE_VIEWED, props)
    }

    fun trackSearchResultsLoaded(discoveryParams: DiscoveryParams) {
        val props = AnalyticEventsUtils.discoveryParamsProperties(discoveryParams)
        client.track(SEARCH_RESULTS_LOADED, props)
    }

    //endregion
    //region Back a project
    fun trackCheckoutPaymentPageViewed(pledgeData: PledgeData) {
        val props = AnalyticEventsUtils.pledgeDataProperties(pledgeData, client.loggedInUser())
        client.track(CHECKOUT_PAYMENT_PAGE_VIEWED, props)
    }

    fun trackPledgeSubmitButtonClicked(checkoutData: CheckoutData, pledgeData: PledgeData) {
        val props = AnalyticEventsUtils.checkoutDataProperties(checkoutData, pledgeData, client.loggedInUser())
        client.track(PLEDGE_SUBMIT_BUTTON_CLICKED, props)
    }

    fun trackManagePledgeButtonClicked(projectData: ProjectData, context: PledgeFlowContext?) {
        val props = AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser())
        if (context != null) {
            props["context_pledge_flow"] = context.trackingString
        }
        client.track(MANAGE_PLEDGE_BUTTON_CLICKED, props)
    }

    fun trackProjectPagePledgeButtonClicked(projectData: ProjectData, pledgeFlowContext: PledgeFlowContext?) {
        val props = AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser())
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        if (pledgeFlowContext != null) {
            props["context_pledge_flow"] = pledgeFlowContext.trackingString
            if (pledgeFlowContext === PledgeFlowContext.NEW_PLEDGE) {
                props.putAll(optimizelyProperties(projectData))
            }
        }
        client.track(PROJECT_PAGE_PLEDGE_BUTTON_CLICKED, props)
    }

    /**
     * Sends data associated with the initial pledge CTA click event to the client.
     *
     * @param projectData: The project data.
     */
    fun trackPledgeInitiateCTA(projectData: ProjectData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA to PLEDGE_INITIATE.contextName)
        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser()))
        client.track(EventName.CTA_CLICKED.eventName, props)
    }

    fun trackSelectRewardButtonClicked(pledgeData: PledgeData) {
        val props = AnalyticEventsUtils.pledgeDataProperties(pledgeData, client.loggedInUser())
        client.track(SELECT_REWARD_BUTTON_CLICKED, props)
    }

    fun trackThanksPageViewed(checkoutData: CheckoutData, pledgeData: PledgeData) {
        val props = AnalyticEventsUtils.checkoutDataProperties(checkoutData, pledgeData, client.loggedInUser())
        client.track(THANKS_PAGE_VIEWED, props)
    }

    fun trackFixPledgeButtonClicked(projectData: ProjectData) {
        val props = AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser())
        props["context_pledge_flow"] = PledgeFlowContext.FIX_ERRORED_PLEDGE.trackingString
        client.track(FIX_PLEDGE_BUTTON_CLICKED, props)
    }

    fun trackAddOnsPageViewed(pledgeData: PledgeData) {
        val props = AnalyticEventsUtils.pledgeDataProperties(pledgeData, client.loggedInUser())
        client.track(ADD_ONS_PAGE_VIEWED, props)
    }

    fun trackAddOnsContinueButtonClicked(pledgeData: PledgeData) {
        val props = AnalyticEventsUtils.pledgeDataProperties(pledgeData, client.loggedInUser())
        client.track(ADD_ONS_CONTINUED_BUTTON_CLICKED, props)
    }

    /**
     * Sends data associated with the add ons continue CTA clicked event to the client.
     *
     * @param pledgeData: The selected pledge data.
     */
    fun trackAddOnsContinueCTA(pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA to ADD_ONS_CONTINUE.contextName)
        props.putAll(AnalyticEventsUtils.pledgeDataProperties(pledgeData, client.loggedInUser()))
        client.track(EventName.CTA_CLICKED.eventName, props)
    }

    //endregion
    //region Log In or Signup
    fun trackFacebookLogInSignUpButtonClicked() {
        client.track(FACEBOOK_LOG_IN_OR_SIGNUP_BUTTON_CLICKED)
    }

    fun trackForgotPasswordPageViewed() {
        client.track(FORGOT_PASSWORD_PAGE_VIEWED)
    }

    fun trackLogInButtonClicked() {
        client.track(LOG_IN_BUTTON_CLICKED)
    }

    fun trackLogInSignUpButtonClicked() {
        client.track(LOG_IN_OR_SIGNUP_BUTTON_CLICKED)
    }

    fun trackLogInSignUpPageViewed() {
        client.track(LOG_IN_OR_SIGN_UP_PAGE_VIEWED)
    }

    fun trackLogInSubmitButtonClicked() {
        client.track(LOG_IN_SUBMIT_BUTTON_CLICKED)
    }

    fun trackSignUpButtonClicked() {
        client.track(SIGN_UP_BUTTON_CLICKED)
    }

    fun trackSignUpSubmitButtonClicked() {
        client.track(SIGN_UP_SUBMIT_BUTTON_CLICKED)
    }

    fun trackTwoFactorConfirmationViewed() {
        client.track(TWO_FACTOR_CONFIRMATION_VIEWED)
    }

    fun trackVerificationScreenViewed() {
        client.track(VERIFICATION_SCREEN_VIEWED)
    }

    fun trackSkipVerificationButtonClicked() {
        client.track(SKIP_VERIFICATION_BUTTON_CLICKED)
    }

    //endregion
    //region Experiments
    fun trackCampaignDetailsButtonClicked(projectData: ProjectData) {
        client.track(CAMPAIGN_DETAILS_BUTTON_CLICKED, experimentProperties(projectData))
    }

    fun trackCampaignDetailsPledgeButtonClicked(projectData: ProjectData) {
        client.track(CAMPAIGN_DETAILS_PLEDGE_BUTTON_CLICKED, experimentProperties(projectData))
    }

    fun trackCreatorDetailsClicked(projectData: ProjectData) {
        client.track(CREATOR_DETAILS_CLICKED, experimentProperties(projectData))
    }

    fun reset() {
        client.reset()
    }

    //endregion
    private fun experimentProperties(projectData: ProjectData): Map<String, Any> {
        val props = AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser())
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        props.putAll(optimizelyProperties(projectData))
        props["context_pledge_flow"] = PledgeFlowContext.NEW_PLEDGE.trackingString
        return props
    }

    private fun optimizelyProperties(projectData: ProjectData): Map<String, Any> {
        val experimentData = ExperimentData(client.loggedInUser(), projectData.refTagFromIntent(), projectData.refTagFromCookie())
        return client.optimizely()?.optimizelyProperties(experimentData) ?: emptyMap()
    }

    private class ProxyClient(private val clients: List<TrackingClientType?>) {
        fun track(eventName: String) {
            clients.forEach { client ->
                client?.track(eventName)
            }
        }

        fun track(eventName: String, additionalProperties: Map<String, Any>) {
            clients.forEach { client ->
                client?.track(eventName, additionalProperties)
            }
        }

        fun reset() = clients.forEach { it?.reset() }

        fun loggedInUser(): User? = clients.firstOrNull()?.loggedInUser()

        fun optimizely(): ExperimentsClientType? = clients.firstOrNull()?.optimizely()
    }
}