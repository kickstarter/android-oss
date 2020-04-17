package com.kickstarter.libs

import com.kickstarter.libs.utils.KoalaUtils
import com.kickstarter.models.Activity
import com.kickstarter.models.Project
import com.kickstarter.models.Update
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.PushNotificationEnvelope
import com.kickstarter.ui.data.*
import java.util.*

class Koala(private val client: TrackingClientType) {

    fun client(): TrackingClientType {
        return this.client
    }

    //region APPLICATION LIFECYCLE
    fun trackAppOpen() {
        this.client.track("App Open")
    }

    fun trackAppClose() {
        this.client.track("App Close")
    }

    fun trackMemoryWarning() {
        this.client.track("App Memory Warning")
    }

    fun trackOpenedAppBanner() {
        this.client.track("Opened App Banner")
    }
    //endregion

    //region BACKING
    fun trackViewedPledgeInfo(project: Project) {
        this.client.track(KoalaEvent.VIEWED_PLEDGE_INFO, KoalaUtils.projectProperties(project, this.client.loggedInUser()))
    }
    //endregion

    //region DISCOVERY
    fun trackDiscovery(params: DiscoveryParams, isOnboardingVisible: Boolean) {
        val props = KoalaUtils.discoveryParamsProperties(params)
        props["discover_onboarding_is_visible"] = isOnboardingVisible
        this.client.track("Discover List View", props)
    }

    fun trackDiscoveryFilters() {
        this.client.track("Discover Switch Modal", object : HashMap<String, Any>() {
            init {
                put("modal_type", "filters")
            }
        })
    }

    fun trackDiscoveryFilterSelected(params: DiscoveryParams) {
        this.client.track("Discover Modal Selected Filter", KoalaUtils.discoveryParamsProperties(params))
    }

    fun trackDiscoveryRefreshTriggered() {
        this.client.track(KoalaEvent.TRIGGERED_REFRESH, object : HashMap<String, Any>() {
            init {
                put("type", "swipe")
            }
        })
    }

    fun trackEditorialCardClicked(editorial: Editorial) {
        this.client.track(KoalaEvent.EDITORIAL_CARD_CLICKED, object : HashMap<String, Any>() {
            init {
                put("ref_tag", RefTag.collection(editorial.tagId).tag())
            }
        })
    }
    //endregion

    //region PROJECT
    /**
     * Tracks a project show event.
     *
     * @param projectData The Intent RefTag is the (nullable) RefTag present in the activity upon displaying the project.
     * The Cookie RefTag is the (nullable) RefTag extracted from the cookie store upon viewing the project.
     */
    fun trackProjectShow(projectData: ProjectData) {
        val properties = KoalaUtils.projectProperties(projectData.project(), this.client.loggedInUser())
        properties.putAll(KoalaUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))

        this.client.track(KoalaEvent.PROJECT_PAGE, properties)
    }

    fun trackProjectActionButtonClicked(@KoalaEvent.ProjectAction eventName: String, project: Project) {
        val properties = KoalaUtils.projectProperties(project, this.client.loggedInUser())
        this.client.track(eventName, properties)
    }

    fun trackSelectRewardButtonClicked(project: Project, rewardMinimum: Int, rewardPosition: Int) {
        val properties = KoalaUtils.projectProperties(project, this.client.loggedInUser())

        properties["backer_reward_minimum"] = rewardMinimum
        properties["reward_position"] = rewardPosition

        this.client.track(KoalaEvent.SELECT_REWARD_BUTTON_CLICKED, properties)
    }

    fun trackCancelPledgeButtonClicked(project: Project) {
        val properties = KoalaUtils.projectProperties(project, this.client.loggedInUser())

        this.client.track(KoalaEvent.CANCEL_PLEDGE_BUTTON_CLICKED, properties)
    }

    // PROJECT STAR
    fun trackProjectStar(project: Project) {
        val props = KoalaUtils.projectProperties(project, this.client.loggedInUser())

        // Deprecated events
        this.client.track(if (project.isStarred) KoalaEvent.PROJECT_STAR else KoalaEvent.PROJECT_UNSTAR, props)

        this.client.track(if (project.isStarred) KoalaEvent.STARRED_PROJECT else KoalaEvent.UNSTARRED_PROJECT, props)
    }

    // PROJECT CREATOR BIO
    fun trackViewedCreatorBioModal(project: Project) {
        val loggedInUser = this.client.loggedInUser()
        val props = KoalaUtils.projectProperties(project, loggedInUser)
        props["modal_title"] = "creatorBioModal"

        this.client.track(KoalaEvent.MODAL_DIALOG_VIEW, props)
    }

    fun trackViewedMessageCreatorModal(project: Project) {
        val loggedInUser = this.client.loggedInUser()
        val props = KoalaUtils.projectProperties(project, loggedInUser)
        props["modal_title"] = "messageCreatorModal"

        this.client.track(KoalaEvent.MODAL_DIALOG_VIEW, props)
    }

    // COMMENTS
    fun trackLoadedOlderComments(project: Project, update: Update?,
                                 context: KoalaContext.Comments) {

        val loggedInUser = this.client.loggedInUser()
        val props = if (update == null)
            KoalaUtils.projectProperties(project, loggedInUser)
        else
            KoalaUtils.updateProperties(project, update, loggedInUser)
        props["context"] = context.trackingString

        this.client.track(KoalaEvent.LOADED_OLDER_COMMENTS, props)
    }


    @Deprecated("Use {@link #trackLoadedOlderComments(Project, Update, KoalaContext.Comments)} instead.")
    fun trackLoadedOlderProjectComments(project: Project) {
        this.client.track(KoalaEvent.PROJECT_COMMENT_LOAD_OLDER, KoalaUtils.projectProperties(project, this.client.loggedInUser()))
    }

    fun trackPostedComment(project: Project, update: Update?,
                           context: KoalaContext.CommentDialog) {

        val loggedInUser = this.client.loggedInUser()
        val props = if (update == null)
            KoalaUtils.projectProperties(project, loggedInUser)
        else
            KoalaUtils.updateProperties(project, update, loggedInUser)
        props["context"] = context.trackingString

        this.client.track(KoalaEvent.POSTED_COMMENT, props)
    }


    @Deprecated("Use {@link #trackPostedComment(Project, Update, KoalaContext.CommentDialog)} instead.")
    fun trackProjectCommentCreate(project: Project) {
        this.client.track(KoalaEvent.PROJECT_COMMENT_CREATE, KoalaUtils.projectProperties(project, this.client.loggedInUser()))
    }


    @Deprecated("Use {@link #trackViewedComments(Project, Update, KoalaContext.Comments)} instead.")
    fun trackProjectCommentsView(project: Project) {
        this.client.track(KoalaEvent.PROJECT_COMMENT_VIEW, KoalaUtils.projectProperties(project, this.client.loggedInUser()))
    }

    fun trackViewedComments(project: Project, update: Update?,
                            context: KoalaContext.Comments) {

        val loggedInUser = this.client.loggedInUser()
        val props = if (update == null)
            KoalaUtils.projectProperties(project, loggedInUser)
        else
            KoalaUtils.updateProperties(project, update, loggedInUser)

        props["context"] = context.trackingString
        this.client.track(KoalaEvent.VIEWED_COMMENTS, props)
    }
    //endregion

    //region ACTIVITY
    fun trackActivityView(pageCount: Int) {
        if (pageCount == 0) {
            this.client.track(KoalaEvent.ACTIVITY_VIEW)
        } else {
            this.client.track(KoalaEvent.ACTIVITY_LOAD_MORE, object : HashMap<String, Any>() {
                init {
                    put("page_count", pageCount)
                }
            })
        }
    }
    //endregion

    //region SEARCH
    fun trackSearchView() {
        this.client.track(KoalaEvent.VIEWED_SEARCH)
        // deprecated
        this.client.track(KoalaEvent.DISCOVER_SEARCH_LEGACY)
    }

    fun trackSearchResults(query: String, pageCount: Int) {
        if (pageCount == 1) {
            val params = object : HashMap<String, Any>() {
                init {
                    put("search_term", query)
                }
            }
            this.client.track(KoalaEvent.LOADED_SEARCH_RESULTS, params)
            // deprecated
            this.client.track(KoalaEvent.DISCOVER_SEARCH_RESULTS_LEGACY, params)
        } else {
            val params = object : HashMap<String, Any>() {
                init {
                    put("search_term", query)
                    put("page_count", pageCount)
                }
            }
            this.client.track(KoalaEvent.LOADED_MORE_SEARCH_RESULTS, params)
            // deprecated
            this.client.track(KoalaEvent.DISCOVER_SEARCH_RESULTS_LOAD_MORE_LEGACY, params)
        }
    }

    fun trackClearedSearchTerm() {
        this.client.track(KoalaEvent.CLEARED_SEARCH_TERM)
    }

    fun trackActivityTapped(activity: Activity) {
        this.client.track(KoalaEvent.ACTIVITY_VIEW_ITEM, KoalaUtils.activityProperties(activity, this.client.loggedInUser()))
    }
    //endregion

    //region SESSION EVENTS
    fun trackLoginRegisterTout(loginReason: LoginReason) {
        this.client.track("Application Login or Signup", object : HashMap<String, Any>() {
            init {
                put("intent", loginReason.trackingString())
            }
        })
    }

    fun trackLoginSuccess() {
        this.client.track(KoalaEvent.LOGIN)
    }

    fun trackLoginError() {
        this.client.track(KoalaEvent.ERRORED_USER_LOGIN)
    }

    fun trackTwoFactorAuthView() {
        this.client.track(KoalaEvent.TWO_FACTOR_AUTH_CONFIRM_VIEW)
    }

    fun trackTwoFactorResendCode() {
        this.client.track(KoalaEvent.TWO_FACTOR_AUTH_RESEND_CODE)
    }

    fun trackRegisterFormView() {
        this.client.track(KoalaEvent.USER_SIGNUP)
    }

    fun trackRegisterError() {
        this.client.track(KoalaEvent.ERRORED_USER_SIGNUP)
    }

    fun trackRegisterSuccess() {
        this.client.track(KoalaEvent.NEW_USER)
    }

    fun trackResetPasswordFormView() {
        this.client.track("Forgot Password View")
    }

    fun trackResetPasswordSuccess() {
        this.client.track("Forgot Password Requested")
    }

    fun trackResetPasswordError() {
        this.client.track("Forgot Password Errored")
    }

    fun trackFacebookConfirmation() {
        this.client.track(KoalaEvent.FACEBOOK_CONFIRM)
    }

    fun trackFacebookLoginError() {
        this.client.track("Errored Facebook Login")
    }

    fun trackLogout() {
        this.client.track("Logout")
    }

    fun trackSignupNewsletterToggle(sendNewsletters: Boolean) {
        this.client.track(KoalaEvent.SIGNUP_NEWSLETTER_TOGGLE, object : HashMap<String, Any>() {
            init {
                put("send_newsletters", sendNewsletters)
            }
        })
    }
    //endregion

    //region SETTINGS
    fun trackChangedEmail() {
        this.client.track(KoalaEvent.CHANGED_EMAIL)
    }

    fun trackChangedPassword() {
        this.client.track(KoalaEvent.CHANGED_PASSWORD)
    }

    fun trackContactEmailClicked() {
        this.client.track("Contact Email Clicked")
    }

    fun trackCreatedPassword() {
        this.client.track(KoalaEvent.CREATED_PASSWORD)
    }

    fun trackDeletePaymentMethod() {
        this.client.track(KoalaEvent.DELETED_PAYMENT_METHOD)
    }

    fun trackErroredDeletePaymentMethod() {
        this.client.track(KoalaEvent.ERRORED_DELETE_PAYMENT_METHOD)
    }

    fun trackFailedPaymentMethodCreation() {
        this.client.track(KoalaEvent.FAILED_PAYMENT_METHOD_CREATION)
    }

    fun trackNewsletterToggle(sendNewsletter: Boolean) {
        if (sendNewsletter) {
            this.client.track("Newsletter Subscribe")
        } else {
            this.client.track("Newsletter Unsubscribe")
        }
    }

    fun trackResentVerificationEmail() {
        this.client.track(KoalaEvent.RESENT_VERIFICATION_EMAIL)
    }

    fun trackSavedPaymentMethod() {
        this.client.track(KoalaEvent.SAVED_PAYMENT_METHOD)
    }

    fun trackSelectedChosenCurrency(selectedCurrency: String) {
        this.client.track(KoalaEvent.SELECTED_CHOSEN_CURRENCY, object : HashMap<String, Any>() {
            init {
                put("user_chosen_currency", selectedCurrency)
            }
        })
    }

    fun trackSettingsView() {
        this.client.track(KoalaEvent.VIEWED_SETTINGS)
    }

    fun trackViewedAccount() {
        this.client.track(KoalaEvent.VIEWED_ACCOUNT)
    }

    fun trackViewedAddNewCard() {
        this.client.track(KoalaEvent.VIEWED_ADD_NEW_CARD)
    }

    fun trackViewedChangedEmail() {
        this.client.track(KoalaEvent.VIEWED_CHANGE_EMAIL)
    }

    fun trackViewedChangedPassword() {
        this.client.track(KoalaEvent.VIEWED_CHANGE_PASSWORD)
    }

    fun trackViewedCreatedPassword() {
        this.client.track(KoalaEvent.VIEWED_CREATE_PASSWORD)
    }

    fun trackViewedNotifications() {
        this.client.track(KoalaEvent.VIEWED_NOTIFICATIONS)
    }

    fun trackViewedNewsletter() {
        this.client.track(KoalaEvent.VIEWED_NEWSLETTER)
    }

    fun trackViewedPaymentMethods() {
        this.client.track(KoalaEvent.VIEWED_PAYMENT_METHODS)
    }

    fun trackViewedPrivacy() {
        this.client.track(KoalaEvent.VIEWED_PRIVACY)
    }
    //endregion

    //region CHECKOUT
    fun trackCheckoutShowShareSheet() {
        this.client.track("Checkout Show Share Sheet")
    }

    fun trackCheckoutShowFacebookShareView() {
        this.client.track("Checkout Show Share", object : HashMap<String, Any>() {
            init {
                put("share_type", "facebook")
            }
        })
    }

    fun trackCheckoutShowTwitterShareView() {
        this.client.track("Checkout Show Share", object : HashMap<String, Any>() {
            init {
                put("share_type", "twitter")
            }
        })
    }

    fun trackCheckoutFinishJumpToDiscovery() {
        this.client.track("Checkout Finished Discover More")
    }

    fun trackCheckoutFinishJumpToProject(project: Project) {
        val props = KoalaUtils.projectProperties(project, this.client.loggedInUser())
        this.client.track("Checkout Finished Discover Open Project", props)
    }

    fun trackManagePledgeOptionClicked(project: Project, cta: String) {
        val properties = KoalaUtils.projectProperties(project, this.client.loggedInUser())

        properties["cta"] = cta

        this.client.track(KoalaEvent.MANAGE_PLEDGE_OPTION_CLICKED, properties)
    }

    fun trackAddNewCardButtonClicked(project: Project, pledgeTotal: Double) {
        val properties = KoalaUtils.projectProperties(project, this.client.loggedInUser())

        //Overwrite the pledge_total with the latest value
        properties["pledge_total"] = pledgeTotal

        this.client.track(KoalaEvent.ADD_NEW_CARD_BUTTON_CLICKED, properties)
    }

    fun trackPledgeButtonClicked(project: Project, pledgeTotal: Double) {
        val properties = KoalaUtils.projectProperties(project, this.client.loggedInUser())

        //Overwrite the pledge_total with the latest value
        properties["pledge_total"] = pledgeTotal

        this.client.track(KoalaEvent.PLEDGE_BUTTON_CLICKED, properties)
    }

    fun trackUpdatePledgeButtonClicked(project: Project, pledgeTotal: Double) {
        val properties = KoalaUtils.projectProperties(project, this.client.loggedInUser())

        //Overwrite the pledge_total with the latest value
        properties["pledge_total"] = pledgeTotal

        this.client.track(KoalaEvent.UPDATE_PLEDGE_BUTTON_CLICKED, properties)
    }

    fun trackUpdatePaymentMethodButtonClicked(project: Project) {
        val properties = KoalaUtils.projectProperties(project, this.client.loggedInUser())

        this.client.track(KoalaEvent.UPDATE_PAYMENT_METHOD_BUTTON_CLICKED, properties)
    }

    fun trackPledgeScreenViewed(project: Project) {
        val properties = KoalaUtils.projectProperties(project, this.client.loggedInUser())

        this.client.track(KoalaEvent.PLEDGE_SCREEN_VIEWED, properties)
    }
    //endregion

    //region SHARE
    fun trackShowProjectShareSheet(project: Project) {
        val props = KoalaUtils.projectProperties(project, this.client.loggedInUser())
        props["context"] = KoalaContext.Share.PROJECT

        // deprecated
        this.client.track(KoalaEvent.PROJECT_SHOW_SHARE_SHEET_LEGACY)

        this.client.track(KoalaEvent.SHOWED_SHARE_SHEET, props)
    }
    //endregion

    //region MESSAGES
    fun trackSentMessage(project: Project, context: KoalaContext.Message) {
        val props = KoalaUtils.projectProperties(project, this.client.loggedInUser())
        props["context"] = context.trackingString

        this.client.track(KoalaEvent.SENT_MESSAGE, props)
    }

    fun trackViewedMailbox(mailbox: Mailbox, project: Project?,
                           intentRefTag: RefTag?, context: KoalaContext.Mailbox) {
        val props = if (project == null) HashMap() else KoalaUtils.projectProperties(project, this.client.loggedInUser())

        props["context"] = context.trackingString
        if (intentRefTag != null) {
            props["ref_tag"] = intentRefTag.tag()
        }

        when (mailbox) {
            Mailbox.INBOX -> this.client.track(KoalaEvent.VIEWED_MESSAGE_INBOX, props)
            Mailbox.SENT -> this.client.track(KoalaEvent.VIEWED_SENT_MESSAGES, props)
        }
    }

    fun trackViewedMessageThread(project: Project) {
        this.client.track(KoalaEvent.VIEWED_MESSAGE_THREAD, KoalaUtils.projectProperties(project, this.client.loggedInUser()))
    }
    //endregion

    //region PROFILE
    fun trackProfileView() {
        // deprecated
        this.client.track(KoalaEvent.PROFILE_VIEW_MY)

        this.client.track(KoalaEvent.VIEWED_PROFILE)
    }
    //endregion

    //region RATING
    fun trackAppRatingNow() {
        this.client.track("Checkout Finished Alert App Store Rating Rate Now")
    }

    fun trackAppRatingRemindLater() {
        this.client.track("Checkout Finished Alert App Store Rating Remind Later")
    }

    fun trackAppRatingNoThanks() {
        this.client.track("Checkout Finished Alert App Store Rating No Thanks")
    }
    //endregion

    //region VIDEO
    fun trackVideoStart(project: Project) {
        this.client.track("Project Video Start", KoalaUtils.projectProperties(project, this.client.loggedInUser()))
    }
    //endregion

    //region PROJECT UPDATES
    fun trackViewedUpdate(project: Project, context: KoalaContext.Update) {
        val props = KoalaUtils.projectProperties(project, this.client.loggedInUser())
        props["context"] = context.trackingString
        this.client.track(KoalaEvent.VIEWED_UPDATE, props)
    }

    fun trackViewedUpdates(project: Project) {
        this.client.track(KoalaEvent.VIEWED_UPDATES, KoalaUtils.projectProperties(project, this.client.loggedInUser()))
    }
    //endregion

    //region PUSH NOTIFICATIONS
    fun trackPushNotification(envelope: PushNotificationEnvelope) {
        val properties = object : HashMap<String, Any>() {
            init {
                put("notification_type", "push")

                if (envelope.activity() != null) {
                    put("notification_subject", "activity")
                    put("notification_activity_category", envelope.activity()!!.category())
                }
            }
        }

        // deprecated
        this.client.track(KoalaEvent.NOTIFICATION_OPENED_LEGACY, properties)

        this.client.track(KoalaEvent.OPENED_NOTIFICATION, properties)
    }
    //endregion

    //region WEBVIEWS
    fun trackOpenedExternalLink(project: Project, context: KoalaContext.ExternalLink) {
        val props = KoalaUtils.projectProperties(project, this.client.loggedInUser())
        props["context"] = context.trackingString

        this.client.track(KoalaEvent.OPENED_EXTERNAL_LINK, props)
    }
    //endregion

    //region DEEP LINK
    fun trackContinueUserActivityAndOpenedDeepLink() {
        this.client.track(KoalaEvent.CONTINUE_USER_ACTIVITY)

        this.client.track(KoalaEvent.OPENED_DEEP_LINK)
    }
    //endregion

    //region CREATOR DASHBOARD
    fun trackOpenedProjectSwitcher() {
        this.client.track(KoalaEvent.OPENED_PROJECT_SWITCHER)
    }

    fun trackSwitchedProjects(project: Project) {
        val properties = KoalaUtils.projectProperties(project, this.client.loggedInUser())

        this.client.track(KoalaEvent.SWITCHED_PROJECTS, properties)
    }

    fun trackViewedProjectDashboard(project: Project) {
        val properties = KoalaUtils.projectProperties(project, this.client.loggedInUser())

        this.client.track(KoalaEvent.VIEWED_PROJECT_DASHBOARD, properties)
    }
    //endregion

    //region Discover a Project
    fun trackActivityFeedViewed() {
        this.client.track(ACTIVITY_FEED_VIEWED)
    }

    fun trackExplorePageViewed(discoveryParams: DiscoveryParams) {
        val props = KoalaUtils.discoveryParamsProperties(discoveryParams)

        this.client.track(EXPLORE_PAGE_VIEWED, props)
    }

    fun trackExploreSortClicked(discoveryParams: DiscoveryParams) {
        val props = KoalaUtils.discoveryParamsProperties(discoveryParams)

        this.client.track(EXPLORE_SORT_CLICKED, props)
    }

    fun trackFilterClicked(discoveryParams: DiscoveryParams) {
        val props = KoalaUtils.discoveryParamsProperties(discoveryParams)

        this.client.track(FILTER_CLICKED, props)
    }

    fun trackHamburgerMenuClicked(discoveryParams: DiscoveryParams) {
        val props = KoalaUtils.discoveryParamsProperties(discoveryParams)

        this.client.track(HAMBURGER_MENU_CLICKED, props)
    }

    fun trackProjectPageViewed(projectData: ProjectData, pledgeFlowContext: PledgeFlowContext?) {
        val props = KoalaUtils.projectProperties(projectData.project(), this.client.loggedInUser())
        props.putAll(KoalaUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        if (pledgeFlowContext != null) {
            props["context_pledge_flow"] = pledgeFlowContext.trackingString
        }

        this.client.track(PROJECT_PAGE_VIEWED, props)
    }

    fun trackSearchButtonClicked() {
        this.client.track(SEARCH_BUTTON_CLICKED)
    }

    fun trackSearchPageViewed(discoveryParams: DiscoveryParams) {
        val props = KoalaUtils.discoveryParamsProperties(discoveryParams)

        this.client.track(SEARCH_PAGE_VIEWED, props)
    }

    fun trackSearchResultsLoaded(discoveryParams: DiscoveryParams) {
        val props = KoalaUtils.discoveryParamsProperties(discoveryParams)

        this.client.track(SEARCH_RESULTS_LOADED, props)
    }
    //endregion

    //region Back a project
    fun trackCheckoutPaymentPageViewed(pledgeData: PledgeData) {
        val props = KoalaUtils.pledgeDataProperties(pledgeData, this.client.loggedInUser())

        this.client.track(CHECKOUT_PAYMENT_PAGE_VIEWED, props)
    }

    fun trackPledgeSubmitButtonClicked(checkoutData: CheckoutData, pledgeData: PledgeData) {
        val props = KoalaUtils.checkoutDataProperties(checkoutData, pledgeData, this.client.loggedInUser())

        this.client.track(PLEDGE_SUBMIT_BUTTON_CLICKED, props)
    }

    fun trackProjectPagePledgeButtonClicked(projectData: ProjectData, pledgeFlowContext: PledgeFlowContext?) {
        val props = KoalaUtils.projectProperties(projectData.project(), this.client.loggedInUser())
        props.putAll(KoalaUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        if (pledgeFlowContext != null) {
            props["context_pledge_flow"] = pledgeFlowContext.trackingString
        }

        this.client.track(PROJECT_PAGE_PLEDGE_BUTTON_CLICKED, props)
    }

    fun trackSelectRewardButtonClicked(pledgeData: PledgeData) {
        val props = KoalaUtils.pledgeDataProperties(pledgeData, this.client.loggedInUser())

        this.client.track(SELECT_REWARD_BUTTON_CLICKED, props)
    }

    fun trackThanksPageViewed(checkoutData: CheckoutData, pledgeData: PledgeData) {
        val props = KoalaUtils.checkoutDataProperties(checkoutData, pledgeData, this.client.loggedInUser())

        this.client.track(THANKS_PAGE_VIEWED, props)
    }
    //endregion

    //region Log In or Signup
    fun trackFacebookLogInSignUpButtonClicked() {
        this.client.track(FACEBOOK_LOG_IN_OR_SIGNUP_BUTTON_CLICKED)
    }

    fun trackForgotPasswordPageViewed() {
        this.client.track(FORGOT_PASSWORD_PAGE_VIEWED)
    }

    fun trackLogInButtonClicked() {
        this.client.track(LOG_IN_BUTTON_CLICKED)
    }

    fun trackLogInSignUpButtonClicked() {
        this.client.track(LOG_IN_OR_SIGNUP_BUTTON_CLICKED)
    }

    fun trackLogInSignUpPageViewed() {
        this.client.track(LOG_IN_OR_SIGN_UP_PAGE_VIEWED)
    }

    fun trackLogInSubmitButtonClicked() {
        this.client.track(LOG_IN_SUBMIT_BUTTON_CLICKED)
    }

    fun trackSignUpButtonClicked() {
        this.client.track(SIGN_UP_BUTTON_CLICKED)
    }

    fun trackSignUpSubmitButtonClicked() {
        this.client.track(SIGN_UP_SUBMIT_BUTTON_CLICKED)
    }

    fun trackTwoFactorConfirmationViewed() {
        this.client.track(TWO_FACTOR_CONFIRMATION_VIEWED)
    }
    //endregion
}
