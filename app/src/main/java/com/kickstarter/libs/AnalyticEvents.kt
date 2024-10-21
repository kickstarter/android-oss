package com.kickstarter.libs

import com.kickstarter.features.pledgedprojectsoverview.data.PPOCard
import com.kickstarter.libs.utils.AnalyticEventsUtils
import com.kickstarter.libs.utils.ContextPropertyKeyName.COMMENT_BODY
import com.kickstarter.libs.utils.ContextPropertyKeyName.COMMENT_CHARACTER_COUNT
import com.kickstarter.libs.utils.ContextPropertyKeyName.COMMENT_ID
import com.kickstarter.libs.utils.ContextPropertyKeyName.COMMENT_ROOT_ID
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_CTA
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_LOCATION
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_PAGE
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_SECTION
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_TYPE
import com.kickstarter.libs.utils.ContextPropertyKeyName.PROJECT_UPDATE_ID
import com.kickstarter.libs.utils.EventContextValues
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.ACTIVITY_FEED
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.ADD_ONS
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.CHANGE_PAYMENT
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.CHECKOUT
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.LOGIN
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.LOGIN_SIGN_UP
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.MANAGE_PLEDGE
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.PROJECT
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.PROJECT_ALERTS
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.REWARDS
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.SIGN_UP
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.THANKS
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.TWO_FACTOR_AUTH
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.UPDATE_PLEDGE
import com.kickstarter.libs.utils.EventContextValues.ContextTypeName.ADDRESS
import com.kickstarter.libs.utils.EventContextValues.ContextTypeName.CREDIT_CARD
import com.kickstarter.libs.utils.EventContextValues.ContextTypeName.REPLY
import com.kickstarter.libs.utils.EventContextValues.ContextTypeName.ROOT
import com.kickstarter.libs.utils.EventContextValues.ContextTypeName.UNWATCH
import com.kickstarter.libs.utils.EventContextValues.ContextTypeName.WATCH
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.ADD_ONS_CONTINUE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.CAMPAIGN_DETAILS
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.COMMENT_POST
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.CONFIRM_INITIATE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.CONFIRM_SUBMIT
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.DISCOVER
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.DISCOVER_FILTER
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.DISCOVER_SORT
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.EDIT
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.FIX_PLEDGE_INITIATE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.LATE_PLEDGE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.LOGIN_INITIATE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.LOGIN_OR_SIGN_UP
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.LOGIN_SUBMIT
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.MESSAGE_CREATOR_INITIATE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.PLEDGE_CONFIRM
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.PLEDGE_INITIATE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.PLEDGE_SUBMIT
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.REWARD_CONTINUE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.SEARCH
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.SIGN_UP_INITIATE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.SIGN_UP_SUBMIT
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.SURVEY_RESPONSE_INITIATE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.WATCH_PROJECT
import com.kickstarter.libs.utils.EventContextValues.DiscoveryContextType.ALL
import com.kickstarter.libs.utils.EventContextValues.DiscoveryContextType.CATEGORY_NAME
import com.kickstarter.libs.utils.EventContextValues.DiscoveryContextType.PWL
import com.kickstarter.libs.utils.EventContextValues.DiscoveryContextType.RECOMMENDED
import com.kickstarter.libs.utils.EventContextValues.DiscoveryContextType.RESULTS
import com.kickstarter.libs.utils.EventContextValues.DiscoveryContextType.SOCIAL
import com.kickstarter.libs.utils.EventContextValues.DiscoveryContextType.SUBCATEGORY_NAME
import com.kickstarter.libs.utils.EventContextValues.DiscoveryContextType.WATCHED
import com.kickstarter.libs.utils.EventContextValues.LocationContextName.CURATED
import com.kickstarter.libs.utils.EventContextValues.LocationContextName.DISCOVER_ADVANCED
import com.kickstarter.libs.utils.EventContextValues.LocationContextName.DISCOVER_OVERLAY
import com.kickstarter.libs.utils.EventContextValues.LocationContextName.GLOBAL_NAV
import com.kickstarter.libs.utils.EventContextValues.LocationContextName.SEARCH_RESULTS
import com.kickstarter.libs.utils.EventName.CARD_CLICKED
import com.kickstarter.libs.utils.EventName.CTA_CLICKED
import com.kickstarter.libs.utils.EventName.PAGE_VIEWED
import com.kickstarter.libs.utils.EventName.VIDEO_PLAYBACK_COMPLETED
import com.kickstarter.libs.utils.EventName.VIDEO_PLAYBACK_STARTED
import com.kickstarter.libs.utils.checkoutProperties
import com.kickstarter.libs.utils.extensions.isNonZero
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.ProjectData
import java.util.Locale
import kotlin.collections.HashMap

class AnalyticEvents(trackingClients: List<TrackingClientType?>) {

    private val client = ProxyClient(trackingClients)

    // APPLICATION LIFECYCLE
    fun trackAppOpen() {
        client.track("App Open")
    }

    /**
     * Sends data to the client when Activity Screen is viewed.
     */
    fun trackActivityFeedPageViewed() {
        val props = hashMapOf<String, Any>()
        props[CONTEXT_PAGE.contextName] = ACTIVITY_FEED.contextName
        client.track(PAGE_VIEWED.eventName, props)
    }

    /**
     * Sends data to the client when the discover projects CTA is tapped on the activity screen.
     */
    fun trackDiscoverProjectCTAClicked() {
        val props = hashMapOf<String, Any>()
        props[CONTEXT_CTA.contextName] = DISCOVER.contextName
        props[CONTEXT_PAGE.contextName] = ACTIVITY_FEED.contextName
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data to the client when the signup or login CTAs are clicked.
     *
     * @param type: The type of sign-up the user is doing, if applicable.
     * @param page: The page where the CTA was clicked.
     */
    fun trackLoginOrSignUpCtaClicked(type: String?, page: String) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to LOGIN_OR_SIGN_UP.contextName)
        props[CONTEXT_PAGE.contextName] = page
        type?. let { props[CONTEXT_TYPE.contextName] = it }
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data to the client when the signup page is viewed.
     */
    fun trackSignUpPageViewed() {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to SIGN_UP.contextName)
        client.track(PAGE_VIEWED.eventName, props)
    }

    /**
     * Sends data to the client when the login/sign-up page is viewed.
     */
    fun trackLoginOrSignUpPagedViewed() {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to LOGIN_SIGN_UP.contextName)
        client.track(PAGE_VIEWED.eventName, props)
    }

    /**
     * Sends data to the client when the login page is viewed.
     */
    fun trackLoginPagedViewed() {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to LOGIN.contextName)
        client.track(PAGE_VIEWED.eventName, props)
    }

    // VIDEO
    /**
     *Sends data to the client when the any of video started .
     *
     * @param videoLength: Length of video in seconds.
     * @param videoPosition: Index position of the playhead,
     */
    fun trackVideoStarted(project: Project, videoLength: Long, videoPosition: Long) {
        val props: HashMap<String, Any> = HashMap()
        props[CONTEXT_PAGE.contextName] = PROJECT.contextName
        props.putAll(AnalyticEventsUtils.videoProperties(videoLength, videoPosition))
        props.putAll(AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
        client.track(VIDEO_PLAYBACK_STARTED.eventName, props)
    }

    /**
     *Sends data to the client when the any of video completed .
     *
     * @param videoLength: Length of video in seconds.
     * @param videoPosition:Index position of the playhead,
     */
    fun trackVideoCompleted(project: Project, videoLength: Long, videoPosition: Long) {
        val props: HashMap<String, Any> = HashMap()
        props[CONTEXT_PAGE.contextName] = PROJECT.contextName
        props.putAll(AnalyticEventsUtils.videoProperties(videoLength, videoPosition))
        props.putAll(AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
        client.track(VIDEO_PLAYBACK_COMPLETED.eventName, props)
    }

    /**
     * Sends data to the client when the any of the discover sort tabs are clicked.
     *
     * @param currentSort: The sort the user was in before changing sorts.
     * @param discoveryParams: The discovery parameters.
     */
    fun trackDiscoverSortCTA(currentSort: DiscoveryParams.Sort, discoverParams: DiscoveryParams) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to DISCOVER_SORT.contextName)
        props[CONTEXT_LOCATION.contextName] = DISCOVER_ADVANCED.contextName
        props[CONTEXT_PAGE.contextName] = DISCOVER.contextName
        props[CONTEXT_TYPE.contextName] = discoverParams.sort()?.let {
            when (it) {
                DiscoveryParams.Sort.POPULAR -> "popular"
                DiscoveryParams.Sort.ENDING_SOON -> "ending_soon"
                else -> it.toString()
            }
        } ?: ""
        props.putAll(AnalyticEventsUtils.discoveryParamsProperties(discoverParams, currentSort))
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Tracks a discover project clicks on the search result
     * @param discoveryParams: The search discovery parameters.
     * @param projectData: selected project from serach result
     * @param count: search result count
     * @param sort: DiscoveryParams.Sort type
     */
    fun trackDiscoverSearchResultProjectCATClicked(
        discoveryParams: DiscoveryParams,
        projectData: ProjectData,
        count: Int,
        sort: DiscoveryParams.Sort
    ) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to PROJECT.contextName)
        props[CONTEXT_PAGE.contextName] = SEARCH.contextName
        props[CONTEXT_LOCATION.contextName] = SEARCH_RESULTS.contextName
        props[CONTEXT_TYPE.contextName] = EventContextValues.ContextTypeName.RESULTS.contextName
        discoveryParams.term()?.let { props["discover_search_term"] = it }
        props["discover_search_results_count"] = count
        props.putAll(AnalyticEventsUtils.discoveryParamsProperties(discoveryParams, sort).toMutableMap())
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data to the client when a project in the discovery is clicked
     * sending sort and filter properties.
     *
     * @param discoveryParams: The discovery parameters.
     * @param projectData: The projectData parameters.
     */
    fun trackDiscoverProjectCtaClicked(discoveryParams: DiscoveryParams, projectData: ProjectData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to PROJECT.contextName)
        props[CONTEXT_LOCATION.contextName] = DISCOVER_ADVANCED.contextName
        props[CONTEXT_PAGE.contextName] = DISCOVER.contextName
        props[CONTEXT_TYPE.contextName] = when {
            discoveryParams.recommended().isTrue() -> RECOMMENDED.contextName
            discoveryParams.category()?.isRoot.isTrue() ||
                discoveryParams.category() != null ||
                discoveryParams.staffPicks().isTrue() ||
                discoveryParams.isAllProjects.isTrue() -> RESULTS.contextName
            else -> ""
        }

        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser()))
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        props.putAll(AnalyticEventsUtils.discoveryParamsProperties(discoveryParams))

        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data to the client when items in the discovery sort is selected.
     *
     * @param discoveryParams: The discovery parameters.
     */
    fun trackDiscoveryPageViewed(discoveryParams: DiscoveryParams) {
        val props = AnalyticEventsUtils.discoveryParamsProperties(discoveryParams).toMutableMap()
        props[DISCOVER_SORT.contextName] = discoveryParams.sort()?.name?.lowercase(Locale.ROOT) ?: ""
        props[CONTEXT_PAGE.contextName] = DISCOVER.contextName
        client.track(PAGE_VIEWED.eventName, props)
    }

    /**
     * Sends data to the client when pledged project overview screen is viewed
     *
     * @param ppoCards: The list of alerts.
     * @param totalCount: The total number of alerts.
     */
    fun trackPledgedProjectsOverviewPageViewed(ppoCards: List<PPOCard>, totalCount: Int) {
        val props = AnalyticEventsUtils.notificationProperties(ppoCards, totalCount).toMutableMap()
        props[CONTEXT_PAGE.contextName] = PROJECT_ALERTS.contextName
        client.track(PAGE_VIEWED.eventName, props)
    }

    /**
     * Sends data to the client when message creator is tapped on a ppo card
     *
     * @param projectID: The id of the project.
     * @param creatorID: The id of the creator.
     * @param ppoCards: The list of alerts.
     * @param totalCount: The total number of alerts.
     */
    fun trackPPOMessageCreatorCTAClicked(projectID: String, ppoCards: List<PPOCard?>, totalCount: Int, creatorID: String) {
        val props = AnalyticEventsUtils.notificationProperties(ppoCards, totalCount).toMutableMap()
        props["interaction_target_id"] = creatorID
        props["project_pid"] = projectID
        props[CONTEXT_PAGE.contextName] = PROJECT_ALERTS.contextName
        props[CONTEXT_CTA.contextName] = MESSAGE_CREATOR_INITIATE.contextName
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data to the client when fix payment is tapped on a fix payment ppo card
     *
     * @param projectID: The id of the project.
     * @param ppoCards: The list of alerts.
     * @param totalCount: The total number of alerts.
     */
    fun trackPPOFixPaymentCTAClicked(projectID: String, ppoCards: List<PPOCard?>, totalCount: Int) {
        val props = AnalyticEventsUtils.notificationProperties(ppoCards, totalCount).toMutableMap()
        props["project_pid"] = projectID
        props[CONTEXT_PAGE.contextName] = PROJECT_ALERTS.contextName
        props[CONTEXT_CTA.contextName] = FIX_PLEDGE_INITIATE.contextName
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data to the client when open survey is tapped on open survey ppo card
     *
     * @param projectID: The id of the project.
     * @param ppoCards: The list of alerts.
     * @param totalCount: The total number of alerts.
     * @param surveyID: The id of the survey.
     */
    fun trackPPOOpenSurveyCTAClicked(projectID: String, ppoCards: List<PPOCard?>, totalCount: Int, surveyID: String) {
        val props = AnalyticEventsUtils.notificationProperties(ppoCards, totalCount).toMutableMap()
        props["project_pid"] = projectID
        props["survey_id"] = surveyID
        props[CONTEXT_PAGE.contextName] = PROJECT_ALERTS.contextName
        props[CONTEXT_CTA.contextName] = SURVEY_RESPONSE_INITIATE.contextName
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data to the client when confirm address is initiated on a confirm address ppo card
     *
     * @param projectID: The id of the project.
     * @param ppoCards: The list of alerts.
     * @param totalCount: The total number of alerts.
     */
    fun trackPPOConfirmAddressInitiateCTAClicked(projectID: String, ppoCards: List<PPOCard?>, totalCount: Int) {
        val props = AnalyticEventsUtils.notificationProperties(ppoCards, totalCount).toMutableMap()
        props["project_pid"] = projectID
        props[CONTEXT_PAGE.contextName] = PROJECT_ALERTS.contextName
        props[CONTEXT_CTA.contextName] = CONFIRM_INITIATE.contextName
        props[CONTEXT_TYPE.contextName] = ADDRESS.contextName
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data to the client when confirm address is submitted on a confirm address ppo card dialog box
     *
     * @param projectID: The id of the project.
     * @param ppoCards: The list of alerts.
     * @param totalCount: The total number of alerts.
     */
    fun trackPPOConfirmAddressSubmitCTAClicked(projectID: String, ppoCards: List<PPOCard?>, totalCount: Int) {
        val props = AnalyticEventsUtils.notificationProperties(ppoCards, totalCount).toMutableMap()
        props["project_pid"] = projectID
        props[CONTEXT_PAGE.contextName] = PROJECT_ALERTS.contextName
        props[CONTEXT_CTA.contextName] = CONFIRM_SUBMIT.contextName
        props[CONTEXT_TYPE.contextName] = ADDRESS.contextName
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data to the client when edit on confirm address card is tapped
     *
     * @param projectID: The id of the project.
     * @param ppoCards: The list of alerts.
     * @param totalCount: The total number of alerts.
     */
    fun trackPPOConfirmAddressEditCTAClicked(projectID: String, ppoCards: List<PPOCard?>, totalCount: Int) {
        val props = AnalyticEventsUtils.notificationProperties(ppoCards, totalCount).toMutableMap()
        props["project_pid"] = projectID
        props[CONTEXT_PAGE.contextName] = PROJECT_ALERTS.contextName
        props[CONTEXT_CTA.contextName] = EDIT.contextName
        props[CONTEXT_TYPE.contextName] = ADDRESS.contextName
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data to the client when items in the discovery overflow menu are tapped.
     *
     * @param discoveryParams: The discovery parameters.
     */
    fun trackDiscoverFilterCTA(discoveryParams: DiscoveryParams) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to DISCOVER_FILTER.contextName)
        props[CONTEXT_LOCATION.contextName] = DISCOVER_OVERLAY.contextName
        props[CONTEXT_PAGE.contextName] = DISCOVER.contextName
        props[CONTEXT_TYPE.contextName] = when {
            discoveryParams.staffPicks().isTrue() -> PWL.contextName
            discoveryParams.recommended().isTrue() -> RECOMMENDED.contextName
            discoveryParams.starred().isNonZero() -> WATCHED.contextName
            discoveryParams.social().isNonZero() -> SOCIAL.contextName
            discoveryParams.category()?.isRoot.isTrue() -> CATEGORY_NAME.contextName
            discoveryParams.category() != null -> SUBCATEGORY_NAME.contextName
            discoveryParams.isAllProjects.isTrue() -> ALL.contextName
            else -> ALL.contextName
        }
        props.putAll(AnalyticEventsUtils.discoveryParamsProperties(discoveryParams))

        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data to the client when the projects screen is loaded.
     *
     * @param pledgeData: The selected pledge data.
     * @param pageSectionContext: The section of the project page being viewed.
     */
    fun trackProjectScreenViewed(projectData: ProjectData, pageSectionContext: String) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to PROJECT.contextName)
        props[CONTEXT_SECTION.contextName] = pageSectionContext
        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser()))
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        client.track(PAGE_VIEWED.eventName, props)
    }

    /**
     * Sends data to the client open TwoFactorAuth screen
     */
    fun trackTwoFactorAuthPageViewed() {
        val props = hashMapOf(CONTEXT_PAGE.contextName to TWO_FACTOR_AUTH.contextName)
        client.track(PAGE_VIEWED.eventName, props)
    }

    /**
     * Sends data to the client when the projects screen is loaded.
     *
     * @param project: The selected project.
     * @param pageContext: The page/screen of the app where the project card was clicked.
     */
    fun trackProjectCardClicked(project: Project, pageContext: String) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_TYPE.contextName to PROJECT.contextName)
        props[CONTEXT_PAGE.contextName] = pageContext
        props.putAll(AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
        client.track(CARD_CLICKED.eventName, props)
    }

    /**
     * Sends data to the client when the projects card at thanks activity is clicked
     *
     * @param project: The selected project.
     * @param checkoutData: The page/screen of the app where the project card was clicked.
     * @param pledgeData: The selected pledge data.
     */
    fun trackThanksActivityProjectCardClicked(projectData: ProjectData, checkoutData: CheckoutData, pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to PROJECT.contextName)
        props[CONTEXT_PAGE.contextName] = THANKS.contextName
        props[CONTEXT_LOCATION.contextName] = CURATED.contextName
        props[CONTEXT_TYPE.contextName] = RECOMMENDED.contextName
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser()))
        props.putAll(AnalyticEventsUtils.checkoutDataProperties(checkoutData, pledgeData, client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data associated with the search CTA click event to the client.
     * @param discoveryParams: DiscoveryParams
     */
    fun trackSearchCTAButtonClicked(discoveryParams: DiscoveryParams) {
        val props = AnalyticEventsUtils.discoveryParamsProperties(discoveryParams).toMutableMap()
        props[CONTEXT_PAGE.contextName] = DISCOVER.contextName
        props[CONTEXT_CTA.contextName] = SEARCH.contextName
        props[CONTEXT_LOCATION.contextName] = GLOBAL_NAV.contextName
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data associated with the search results page viewed to segment.
     * @param discoveryParams: DiscoveryParams
     * @param count: Int
     * @param sort: DiscoveryParams.Sort
     */
    fun trackSearchResultPageViewed(discoveryParams: DiscoveryParams, count: Int, sort: DiscoveryParams.Sort) {
        val props = AnalyticEventsUtils.discoveryParamsProperties(discoveryParams, sort).toMutableMap()
        props[CONTEXT_PAGE.contextName] = SEARCH.contextName
        discoveryParams.term()?.let { props["discover_search_term"] = it }
        props["discover_search_results_count"] = count
        client.track(PAGE_VIEWED.eventName, props)
    }

    /**
     * Sends data to the client when the checkout screen is loaded.
     *
     * @param checkoutData: The checkout data.
     * @param pledgeData: The selected pledge data.
     */
    fun trackCheckoutScreenViewed(checkoutData: CheckoutData, pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to CHECKOUT.contextName)
        props.putAll(AnalyticEventsUtils.checkoutDataProperties(checkoutData, pledgeData, client.loggedInUser()))
        client.track(PAGE_VIEWED.eventName, props)
    }

    /**
     * Sends data to the client when the update pledge screen is view.
     *
     * @param checkoutData: The checkout data.
     * @param pledgeData: The selected pledge data.
     */
    fun trackUpdatePledgePageViewed(checkoutData: CheckoutData, pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to UPDATE_PLEDGE.contextName)
        props.putAll(AnalyticEventsUtils.checkoutDataProperties(checkoutData, pledgeData, client.loggedInUser()))
        client.track(PAGE_VIEWED.eventName, props)
    }

    /**
     * Sends data associated with the Confirm CTA click event to the client.
     *
     * @param checkoutData: The checkout data.
     * @param pledgeData: The selected pledge data.
     */
    fun trackPledgeConfirmCTA(checkoutData: CheckoutData, pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to PLEDGE_CONFIRM.contextName)
        props[CONTEXT_TYPE.contextName] = CREDIT_CARD.contextName
        props[CONTEXT_PAGE.contextName] = CHECKOUT.contextName
        props.putAll(AnalyticEventsUtils.checkoutDataProperties(checkoutData, pledgeData, client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data associated with the submit CTA click event to the client.
     *
     * @param checkoutData: The checkout data.
     * @param pledgeData: The selected pledge data.
     */
    fun trackPledgeSubmitCTA(checkoutData: CheckoutData, pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to PLEDGE_SUBMIT.contextName)
        props[CONTEXT_TYPE.contextName] = CREDIT_CARD.contextName
        props[CONTEXT_PAGE.contextName] = CHECKOUT.contextName
        props.putAll(AnalyticEventsUtils.checkoutDataProperties(checkoutData, pledgeData, client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data associated with the submit CTA click event to the client.
     *
     * @param checkoutData: The checkout data.
     * @param pledgeData: The selected pledge data.
     */
    fun trackLatePledgeSubmitCTA(checkoutData: CheckoutData, pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to LATE_PLEDGE.contextName)
        props[CONTEXT_TYPE.contextName] = CREDIT_CARD.contextName
        props[CONTEXT_PAGE.contextName] = CHECKOUT.contextName
        props.putAll(AnalyticEventsUtils.checkoutDataProperties(checkoutData, pledgeData, client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data associated with page view event to the client.
     *
     * @param backing Information regarding the backing
     * @param projectData Information regarding projectData
     */
    fun trackManagePledgePageViewed(backing: Backing, projectData: ProjectData) {
        val checkoutData = checkoutProperties(
            amount = backing.amount(),
            checkoutId = null,
            bonus = backing.bonusAmount(),
            shippingAmount = backing.shippingAmount().toDouble()
        )

        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to MANAGE_PLEDGE.contextName)
        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser()))
        props.putAll(AnalyticEventsUtils.checkoutProperties(checkoutData, projectData.project(), backing.addOns()))
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))

        client.track(PAGE_VIEWED.eventName, props)
    }

    /**
     * Sends data associated with the initial pledge CTA click event to the client.
     *
     * @param projectData: The project data.
     */
    fun trackPledgeInitiateCTA(projectData: ProjectData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to PLEDGE_INITIATE.contextName)
        props[CONTEXT_PAGE.contextName] = PROJECT.contextName
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data associated with the select reward CTA click event to the client.
     *
     * @param pledgeData: The pledge data.
     */
    fun trackSelectRewardCTA(pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to REWARD_CONTINUE.contextName)
        props[CONTEXT_PAGE.contextName] = REWARDS.contextName
        props.putAll(AnalyticEventsUtils.pledgeDataProperties(pledgeData, client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data to the client when the thanks page after backing a project is viewed.
     *
     * @param checkoutData: The checkout data for the backed project.
     * @param pledgeData: The pledge data for the backed project.
     */
    fun trackThanksScreenViewed(checkoutData: CheckoutData, pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to THANKS.contextName)
        props[CONTEXT_TYPE.contextName] = pledgeData.pledgeFlowContext().trackingString
        props.putAll(AnalyticEventsUtils.checkoutDataProperties(checkoutData, pledgeData, client.loggedInUser()))
        client.track(PAGE_VIEWED.eventName, props)
    }

    /**
     * Sends data to the client when the add-ons screen is loaded.
     *
     * @param pledgeData: The selected pledge data.
     */
    fun trackAddOnsScreenViewed(pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to ADD_ONS.contextName)
        props.putAll(AnalyticEventsUtils.pledgeDataProperties(pledgeData, client.loggedInUser()))
        client.track(PAGE_VIEWED.eventName, props)
    }

    /**
     * Sends data to the client when the heart icon is tapped on a project page.
     *
     * @param project: The watched or unwatched project.
     */
    fun trackWatchProjectCTA(project: Project, contextPageName: EventContextValues.ContextPageName) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to WATCH_PROJECT.contextName)
        props[CONTEXT_TYPE.contextName] = if (project.isStarred()) WATCH.contextName else UNWATCH.contextName
        props[CONTEXT_PAGE.contextName] = contextPageName.contextName
        props.putAll(AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data associated with the add ons continue CTA clicked event to the client.
     *
     * @param pledgeData: The selected pledge data.
     */
    fun trackAddOnsContinueCTA(pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to ADD_ONS_CONTINUE.contextName)
        props[CONTEXT_PAGE.contextName] = ADD_ONS.contextName
        props.putAll(AnalyticEventsUtils.pledgeDataProperties(pledgeData, client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data to the client when the rewards carousel is loaded.
     *
     * @param projectData: The selected project data.
     */
    fun trackRewardsCarouselViewed(projectData: ProjectData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to REWARDS.contextName)
        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser()))
        client.track(PAGE_VIEWED.eventName, props)
    }

    /**
     * Sends data to the client when the payment method is changed.
     *
     * @param pledgeData: The selected pledge data for project.
     */
    fun trackChangePaymentMethod(pledgeData: PledgeData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to CHANGE_PAYMENT.contextName)
        props.putAll(AnalyticEventsUtils.pledgeDataProperties(pledgeData, client.loggedInUser()))
        client.track(PAGE_VIEWED.eventName, props)
    }

    /**
     * Sends data to the client when the selected project tabs  changed.
     *
     * @param pledgeData: The selected pledge data for project.
     */

    fun trackProjectPageTabChanged(projectData: ProjectData, pageSectionContext: String) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to PROJECT.contextName)
        props[CONTEXT_SECTION.contextName] = pageSectionContext
        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser()))
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        client.track(PAGE_VIEWED.eventName, props)
    }

    /**
     * Sends data to the client when the login button is clicked.
     */
    fun trackLogInButtonCtaClicked() {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_PAGE.contextName to LOGIN.contextName)
        props[CONTEXT_CTA.contextName] = LOGIN_SUBMIT.contextName
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data to the client when the login CTA is clicked on the login/signup screen.
     */
    fun trackLogInInitiateCtaClicked() {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to LOGIN_INITIATE.contextName)
        props[CONTEXT_PAGE.contextName] = LOGIN_SIGN_UP.contextName
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data to the client when the signup CTA is clicked on the login/signup screen.
     */
    fun trackSignUpInitiateCtaClicked() {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to SIGN_UP_INITIATE.contextName)
        props[CONTEXT_PAGE.contextName] = LOGIN_SIGN_UP.contextName
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data to the client when the submit button is clicked.
     */
    fun trackSignUpSubmitCtaClicked() {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to SIGN_UP_SUBMIT.contextName)
        props[CONTEXT_PAGE.contextName] = SIGN_UP.contextName
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data to the client when the campaign details CTA is clicked on the project screen.
     *
     * @param projectData: The data for the current project.
     */
    fun trackCampaignDetailsCTAClicked(projectData: ProjectData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to CAMPAIGN_DETAILS.contextName)
        props[CONTEXT_PAGE.contextName] = PROJECT.contextName
        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser()))
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data associated with the comment screen page viewed to segment.
     * @param project: The current project.
     * @param projectUpdateId: the update id
     */
    fun trackRootCommentPageViewed(project: Project, projectUpdateId: String? = null) {
        val props: HashMap<String, Any> = HashMap()
        props.putAll(createCommentPagePropMap(projectUpdateId))
        props.putAll(AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
        client.track(PAGE_VIEWED.eventName, props)
    }

    /**
     * Sends data associated with the comment screen page viewed to segment.
     * @param project: The current project.
     * @param rootCommentId: The root comment id.
     * @param projectUpdateId: the update id
     */
    fun trackThreadCommentPageViewed(project: Project, rootCommentId: String, projectUpdateId: String? = null) {
        val props: HashMap<String, Any> = HashMap()
        props.putAll(createCommentPagePropMap(projectUpdateId))
        props[COMMENT_ROOT_ID.contextName] = rootCommentId
        props.putAll(AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
        client.track(PAGE_VIEWED.eventName, props)
    }

    /**
     * Sends data to the client when reply is clicked on the project comment screen.
     *
     * @param project: The current project.
     * @param commentId: The comment id.
     * @param comment: The reply.
     * @param projectUpdateId: the update id
     */
    fun trackCommentCTA(project: Project, commentId: String, comment: String, projectUpdateId: String? = null) {
        val props: HashMap<String, Any> =
            createCommentPropMap(projectUpdateId, comment)
        props[CONTEXT_TYPE.contextName] = ROOT.contextName
        props[COMMENT_ID.contextName] = commentId
        props.putAll(AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }

    /**
     * Sends data to the client when reply is clicked on the project root comment screen.
     *
     * @param project: The current project.
     * @param commentReply: The reply.
     * @param commentId: The comment id.
     * @param rootCommentId: The root comment id.
     * @param projectUpdateId: the update id
     *
     */
    fun trackRootCommentReplyCTA(
        project: Project,
        commentId: String,
        commentReply: String,
        rootCommentId: String,
        projectUpdateId: String? = null
    ) {
        val props: HashMap<String, Any> =
            createCommentPropMap(projectUpdateId, commentReply)

        props[CONTEXT_TYPE.contextName] = REPLY.contextName
        props[COMMENT_ID.contextName] = commentId
        props[COMMENT_ROOT_ID.contextName] = rootCommentId

        props.putAll(AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }

    private fun createCommentPropMap(
        projectUpdateId: String?,
        commentReply: String
    ): HashMap<String, Any> {
        return hashMapOf<String, Any>(CONTEXT_CTA.contextName to COMMENT_POST.contextName).also { map ->
            map.putAll(createCommentPagePropMap(projectUpdateId))
            map[COMMENT_BODY.contextName] = commentReply
            map[COMMENT_CHARACTER_COUNT.contextName] = commentReply.length
        }
    }

    private fun createCommentPagePropMap(
        projectUpdateId: String?
    ): HashMap<String, Any> {
        return hashMapOf<String, Any>().also { map ->
            map[CONTEXT_PAGE.contextName] = PROJECT.contextName
            map[CONTEXT_SECTION.contextName] =
                EventContextValues.ContextSectionName.COMMENTS.contextName

            projectUpdateId?.let {
                map[CONTEXT_SECTION.contextName] =
                    EventContextValues.ContextSectionName.UPDATES.contextName
                map[CONTEXT_LOCATION.contextName] =
                    EventContextValues.LocationContextName.COMMENTS.contextName
                map[PROJECT_UPDATE_ID.contextName] = projectUpdateId
            }
        }
    }

    fun reset() {
        client.reset()
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
    }
}
