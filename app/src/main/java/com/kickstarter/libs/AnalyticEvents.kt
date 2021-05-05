package com.kickstarter.libs

import com.kickstarter.libs.utils.AnalyticEventsUtils
import com.kickstarter.libs.utils.BooleanUtils
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_CTA
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_LOCATION
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_PAGE
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_SECTION
import com.kickstarter.libs.utils.ContextPropertyKeyName.CONTEXT_TYPE
import com.kickstarter.libs.utils.EventContextValues
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.ACTIVITY_FEED
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.ADD_ONS
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.CHANGE_PAYMENT
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.CHECKOUT
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.LOGIN
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.LOGIN_SIGN_UP
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.MANAGE_PLEDGE
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.PROJECT
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.REWARDS
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.SIGN_UP
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.THANKS
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.TWO_FACTOR_AUTH
import com.kickstarter.libs.utils.EventContextValues.ContextPageName.UPDATE_PLEDGE
import com.kickstarter.libs.utils.EventContextValues.ContextTypeName.CREDIT_CARD
import com.kickstarter.libs.utils.EventContextValues.ContextTypeName.UNWATCH
import com.kickstarter.libs.utils.EventContextValues.ContextTypeName.WATCH
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.ADD_ONS_CONTINUE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.CAMPAIGN_DETAILS
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.CREATOR_DETAILS
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.DISCOVER
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.DISCOVER_FILTER
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.DISCOVER_SORT
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.LOGIN_INITIATE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.LOGIN_OR_SIGN_UP
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.LOGIN_SUBMIT
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.PLEDGE_INITIATE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.PLEDGE_SUBMIT
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.REWARD_CONTINUE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.SEARCH
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.SIGN_UP_INITIATE
import com.kickstarter.libs.utils.EventContextValues.CtaContextName.SIGN_UP_SUBMIT
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
import com.kickstarter.libs.utils.ExperimentData
import com.kickstarter.libs.utils.checkoutProperties
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.User
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.Editorial
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.ProjectData
import java.util.Locale
import kotlin.collections.HashMap

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

    // DISCOVERY
    fun trackDiscovery(params: DiscoveryParams, isOnboardingVisible: Boolean) {
        val props = AnalyticEventsUtils.discoveryParamsProperties(params).toMutableMap()
        props["discover_onboarding_is_visible"] = isOnboardingVisible
        client.track("Discover List View", props)
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
            BooleanUtils.isTrue(discoveryParams.category()?.isRoot) ||
                discoveryParams.category() != null ||
                BooleanUtils.isTrue(discoveryParams.staffPicks()) ||
                BooleanUtils.isTrue(discoveryParams.isAllProjects) -> RESULTS.contextName
            BooleanUtils.isTrue(discoveryParams.recommended()) -> RECOMMENDED.contextName
            else -> ""
        }

        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser()))
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        props.putAll(AnalyticEventsUtils.discoveryParamsProperties(discoveryParams))

        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackFilterClicked(discoveryParams: DiscoveryParams) {
        val props = AnalyticEventsUtils.discoveryParamsProperties(discoveryParams)
        client.track(FILTER_CLICKED, props)
    }

    /**
     * Sends data to the client when items in the discovery sort is selected.
     *
     * @param discoveryParams: The discovery parameters.
     */
    fun trackDiscoveryPageViewed(discoveryParams: DiscoveryParams) {
        val props = AnalyticEventsUtils.discoveryParamsProperties(discoveryParams).toMutableMap()
        props[DISCOVER_SORT.contextName] = discoveryParams.sort()?.name?.toLowerCase(Locale.ROOT) ?: ""
        props[CONTEXT_PAGE.contextName] = DISCOVER.contextName
        client.track(PAGE_VIEWED.eventName, props)
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
            BooleanUtils.isTrue(discoveryParams.staffPicks()) -> PWL.contextName
            BooleanUtils.isTrue(discoveryParams.recommended()) -> RECOMMENDED.contextName
            BooleanUtils.isIntTrue(discoveryParams.starred()) -> WATCHED.contextName
            BooleanUtils.isIntTrue(discoveryParams.social()) -> SOCIAL.contextName
            BooleanUtils.isTrue(discoveryParams.category()?.isRoot) -> CATEGORY_NAME.contextName
            discoveryParams.category() != null -> SUBCATEGORY_NAME.contextName
            BooleanUtils.isTrue(discoveryParams.isAllProjects) -> ALL.contextName
            else -> ALL.contextName
        }
        props.putAll(AnalyticEventsUtils.discoveryParamsProperties(discoveryParams))

        client.track(CTA_CLICKED.eventName, props)
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

    fun trackSearchButtonClicked() {
        client.track(SEARCH_BUTTON_CLICKED)
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

    fun trackPledgeSubmitButtonClicked(checkoutData: CheckoutData, pledgeData: PledgeData) {
        val props = AnalyticEventsUtils.checkoutDataProperties(checkoutData, pledgeData, client.loggedInUser())
        client.track(PLEDGE_SUBMIT_BUTTON_CLICKED, props)
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

    fun trackManagePledgeButtonClicked(projectData: ProjectData, context: PledgeFlowContext?) {
        val props = AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser())
        if (context != null) {
            props["context_pledge_flow"] = context.trackingString
        }
        client.track(MANAGE_PLEDGE_BUTTON_CLICKED, props)
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
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to PLEDGE_INITIATE.contextName)
        props[CONTEXT_PAGE.contextName] = PROJECT.contextName
        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
    }

    fun trackSelectRewardButtonClicked(pledgeData: PledgeData) {
        val props = AnalyticEventsUtils.pledgeDataProperties(pledgeData, client.loggedInUser())
        client.track(SELECT_REWARD_BUTTON_CLICKED, props)
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

    fun trackThanksPageViewed(checkoutData: CheckoutData, pledgeData: PledgeData) {
        val props = AnalyticEventsUtils.checkoutDataProperties(checkoutData, pledgeData, client.loggedInUser())
        client.track(THANKS_PAGE_VIEWED, props)
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

    fun trackFixPledgeButtonClicked(projectData: ProjectData) {
        val props = AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser())
        props["context_pledge_flow"] = PledgeFlowContext.FIX_ERRORED_PLEDGE.trackingString
        client.track(FIX_PLEDGE_BUTTON_CLICKED, props)
    }

    fun trackAddOnsPageViewed(pledgeData: PledgeData) {
        val props = AnalyticEventsUtils.pledgeDataProperties(pledgeData, client.loggedInUser())
        client.track(ADD_ONS_PAGE_VIEWED, props)
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
    fun trackWatchProjectCTA(project: Project) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to WATCH_PROJECT.contextName)
        props[CONTEXT_TYPE.contextName] = if (project.isStarred) WATCH.contextName else UNWATCH.contextName
        props[CONTEXT_PAGE.contextName] = PROJECT.contextName
        props.putAll(AnalyticEventsUtils.projectProperties(project, client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
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

    /**
     * Sends data to the client when the submit button is clicked.
     */
    fun trackSignUpSubmitCtaClicked() {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to SIGN_UP_SUBMIT.contextName)
        props[CONTEXT_PAGE.contextName] = SIGN_UP.contextName
        client.track(CTA_CLICKED.eventName, props)
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

    fun trackCampaignDetailsPledgeButtonClicked(projectData: ProjectData) {
        client.track(CAMPAIGN_DETAILS_PLEDGE_BUTTON_CLICKED, experimentProperties(projectData))
    }

    fun trackCreatorDetailsClicked(projectData: ProjectData) {
        client.track(CREATOR_DETAILS_CLICKED, experimentProperties(projectData))
    }

    /**
     * Sends data to the client when the creator details CTA is clicked on the project screen.
     *
     * @param projectData: The data for the current project.
     */
    fun trackCreatorDetailsCTA(projectData: ProjectData) {
        val props: HashMap<String, Any> = hashMapOf(CONTEXT_CTA.contextName to CREATOR_DETAILS.contextName)
        props[CONTEXT_PAGE.contextName] = PROJECT.contextName
        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), client.loggedInUser()))
        client.track(CTA_CLICKED.eventName, props)
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
