package com.kickstarter.mock.services

import com.google.gson.JsonObject
import com.kickstarter.libs.Config
import com.kickstarter.mock.factories.ActivityEnvelopeFactory
import com.kickstarter.mock.factories.ActivityFactory
import com.kickstarter.mock.factories.BackingFactory.backing
import com.kickstarter.mock.factories.CategoryFactory.artCategory
import com.kickstarter.mock.factories.CategoryFactory.bluesCategory
import com.kickstarter.mock.factories.CategoryFactory.ceramicsCategory
import com.kickstarter.mock.factories.CategoryFactory.gamesCategory
import com.kickstarter.mock.factories.CategoryFactory.musicCategory
import com.kickstarter.mock.factories.CategoryFactory.photographyCategory
import com.kickstarter.mock.factories.CategoryFactory.tabletopGamesCategory
import com.kickstarter.mock.factories.CategoryFactory.textilesCategory
import com.kickstarter.mock.factories.CategoryFactory.worldMusicCategory
import com.kickstarter.mock.factories.LocationFactory.sydney
import com.kickstarter.mock.factories.MessageThreadEnvelopeFactory
import com.kickstarter.mock.factories.MessageThreadsEnvelopeFactory
import com.kickstarter.mock.factories.ProjectFactory.allTheWayProject
import com.kickstarter.mock.factories.ProjectFactory.project
import com.kickstarter.mock.factories.ProjectFactory.successfulProject
import com.kickstarter.mock.factories.ShippingRulesEnvelopeFactory.shippingRules
import com.kickstarter.mock.factories.SurveyResponseFactory
import com.kickstarter.mock.factories.UpdateFactory
import com.kickstarter.mock.factories.UserFactory.user
import com.kickstarter.models.Backing
import com.kickstarter.models.Category
import com.kickstarter.models.Location
import com.kickstarter.models.Message
import com.kickstarter.models.MessageThread
import com.kickstarter.models.Project
import com.kickstarter.models.ProjectNotification
import com.kickstarter.models.Reward
import com.kickstarter.models.SurveyResponse
import com.kickstarter.models.Update
import com.kickstarter.models.User
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ActivityEnvelope
import com.kickstarter.services.apiresponses.DiscoverEnvelope
import com.kickstarter.services.apiresponses.EmailVerificationEnvelope
import com.kickstarter.services.apiresponses.EmailVerificationEnvelope.Companion.builder
import com.kickstarter.services.apiresponses.MessageThreadEnvelope
import com.kickstarter.services.apiresponses.MessageThreadsEnvelope
import com.kickstarter.services.apiresponses.OAuthTokenEnvelope
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope
import com.kickstarter.services.apiresponses.ProjectsEnvelope
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import com.kickstarter.services.apiresponses.UpdatesEnvelope
import com.kickstarter.ui.data.Mailbox
import com.kickstarter.ui.data.MessageSubject
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

open class MockApiClientV2 : ApiClientTypeV2 {
    private val observable = PublishSubject.create<Pair<String, Map<String, Any>>>()

    /**
     * Emits when endpoints on the client are called. The key in the pair is the underscore-separated
     * name of the method, and the value is a map of argument names/values.
     */
    fun observable(): Observable<Pair<String, Map<String, Any>>> {
        return observable
    }

    override fun config(): Observable<Config> {
        return Observable.empty()
    }

    override fun fetchActivities(): Observable<ActivityEnvelope> {
        return Observable.just(
            ActivityEnvelopeFactory.activityEnvelope(listOf(ActivityFactory.activity()))
        )
    }

    override fun fetchActivities(count: Int?): Observable<ActivityEnvelope> {
        return fetchActivities().take((count ?: 0).toLong())
    }

    override fun fetchActivitiesWithPaginationPath(paginationPath: String): Observable<ActivityEnvelope> {
        return Observable.empty()
    }

    override fun fetchCategories(): Observable<List<Category>> {
        return Observable.just(
            listOf(
                artCategory(),
                bluesCategory(),
                ceramicsCategory(),
                gamesCategory(),
                musicCategory(),
                photographyCategory(),
                tabletopGamesCategory(),
                textilesCategory(),
                worldMusicCategory()
            )
        )
    }

    override fun fetchProjectNotifications(): Observable<List<ProjectNotification>> {
        return Observable.empty()
    }

    override fun fetchProject(param: String): Observable<Project> {
        return Observable.just(
            project()
                .toBuilder()
                .slug(param)
                .build()
        )
    }

    override fun fetchProject(project: Project): Observable<Project> {
        return Observable.just(project)
    }

    override fun fetchProjects(params: DiscoveryParams): Observable<DiscoverEnvelope> {
        return Observable.just(
            DiscoverEnvelope
                .builder()
                .projects(
                    listOf(
                        project(),
                        allTheWayProject(),
                        successfulProject()
                    )
                )
                .urls(
                    DiscoverEnvelope.UrlsEnvelope
                        .builder()
                        .api(
                            DiscoverEnvelope.UrlsEnvelope.ApiEnvelope
                                .builder()
                                .moreProjects("http://more.projects.please")
                                .build()
                        )
                        .build()
                )
                .stats(
                    DiscoverEnvelope.StatsEnvelope
                        .builder()
                        .count(10)
                        .build()
                )
                .build()
        )
    }

    override fun fetchProjects(isMember: Boolean): Observable<ProjectsEnvelope> {
        return Observable.empty()
    }

    override fun fetchProjects(paginationUrl: String): Observable<DiscoverEnvelope> {
        return Observable.empty()
    }

    override fun fetchProjectStats(project: Project): Observable<ProjectStatsEnvelope> {
        return Observable.empty()
    }

    override fun fetchMessagesForBacking(backing: Backing): Observable<MessageThreadEnvelope> {
        return Observable.just(MessageThreadEnvelopeFactory.messageThreadEnvelope())
    }

    override fun fetchMessagesForThread(messageThread: MessageThread): Observable<MessageThreadEnvelope> {
        return Observable.just(MessageThreadEnvelopeFactory.messageThreadEnvelope())
    }

    override fun fetchMessagesForThread(messageThreadId: Long): Observable<MessageThreadEnvelope> {
        return Observable.just(MessageThreadEnvelopeFactory.messageThreadEnvelope())
    }

    override fun fetchMessageThreads(
        project: Project?,
        mailbox: Mailbox
    ): Observable<MessageThreadsEnvelope> {
        return Observable.just(MessageThreadsEnvelopeFactory.messageThreadsEnvelope())
    }

    override fun fetchMessageThreadsWithPaginationPath(paginationPath: String): Observable<MessageThreadsEnvelope> {
        return Observable.empty()
    }

    override fun fetchShippingRules(
        project: Project,
        reward: Reward
    ): Observable<ShippingRulesEnvelope> {
        return Observable.just(shippingRules())
    }

    override fun fetchUpdate(projectParam: String, updateParam: String): Observable<Update> {
        return Observable.just(UpdateFactory.update())
    }

    override fun fetchUpdate(update: Update): Observable<Update> {
        return Observable.empty()
    }

    override fun fetchUpdates(project: Project): Observable<UpdatesEnvelope> {
        return Observable.just(
            UpdatesEnvelope
                .builder()
                .updates(
                    listOf(
                        UpdateFactory.update(),
                        UpdateFactory.update()
                    )
                )
                .urls(
                    UpdatesEnvelope.UrlsEnvelope
                        .builder()
                        .api(
                            UpdatesEnvelope.UrlsEnvelope.ApiEnvelope
                                .builder()
                                .moreUpdates("http://more.updates.please")
                                .build()
                        )
                        .build()
                )
                .build()
        )
    }

    override fun fetchUpdates(paginationPath: String): Observable<UpdatesEnvelope> {
        return Observable.empty()
    }

    override fun loginWithFacebook(accessToken: String): Observable<AccessTokenEnvelope> {
        return Observable.just(
            AccessTokenEnvelope.builder()
                .user(
                    user()
                        .toBuilder()
                        .build()
                )
                .accessToken("deadbeef")
                .build()
        )
    }

    override fun loginWithFacebook(
        fbAccessToken: String,
        code: String
    ): Observable<AccessTokenEnvelope> {
        return Observable.just(
            AccessTokenEnvelope.builder()
                .user(
                    user()
                        .toBuilder()
                        .build()
                )
                .accessToken("deadbeef")
                .build()
        )
    }

    override fun registerWithFacebook(
        fbAccessToken: String,
        sendNewsletters: Boolean
    ): Observable<AccessTokenEnvelope> {
        return Observable.just(
            AccessTokenEnvelope.builder()
                .user(
                    user()
                        .toBuilder()
                        .build()
                )
                .accessToken("deadbeef")
                .build()
        )
    }

    override fun fetchProjectBacking(project: Project, user: User): Observable<Backing> {
        return Observable.just(backing(project, user))
    }

    override fun fetchCategory(param: String): Observable<Category> {
        return Observable.just(musicCategory())
    }

    override fun fetchCategory(category: Category): Observable<Category> {
        return Observable.empty()
    }

    override fun fetchCurrentUser(): Observable<User> {
        return Observable.empty()
    }

    override fun fetchLocation(param: String): Observable<Location> {
        return Observable.just(sydney())
    }

    override fun loginWithCodes(
        codeVerifier: String,
        code: String,
        clientId: String
    ): Observable<OAuthTokenEnvelope> {
        return Observable.just(
            OAuthTokenEnvelope.builder()
                .accessToken("deadbeef")
                .build()
        )
    }
    override fun login(email: String, password: String): Observable<AccessTokenEnvelope> {
        return Observable.just(
            AccessTokenEnvelope.builder()
                .user(
                    user()
                        .toBuilder()
                        .build()
                )
                .accessToken("deadbeef")
                .build()
        )
    }

    override fun login(
        email: String,
        password: String,
        code: String
    ): Observable<AccessTokenEnvelope> {
        return Observable.just(
            AccessTokenEnvelope.builder()
                .user(
                    user()
                        .toBuilder()
                        .build()
                )
                .accessToken("deadbeef")
                .build()
        )
    }

    override fun markAsRead(messageThread: MessageThread): Observable<MessageThread> {
        return Observable.empty()
    }

    override fun postBacking(
        project: Project,
        backing: Backing,
        checked: Boolean
    ): Observable<Backing> {
        return Observable.just(backing())
    }

    override fun registerPushToken(token: String): Observable<JsonObject> {
        return Observable.empty()
    }

    override fun resetPassword(email: String): Observable<User> {
        return Observable.just(user())
    }

    override fun sendMessage(messageSubject: MessageSubject, body: String): Observable<Message> {
        return Observable.empty()
    }

    override fun signup(
        name: String,
        email: String,
        password: String,
        passwordConfirmation: String,
        sendNewsletters: Boolean
    ): Observable<AccessTokenEnvelope> {
        return Observable.just(
            AccessTokenEnvelope.builder()
                .user(
                    user()
                        .toBuilder()
                        .name(name)
                        .build()
                )
                .accessToken("deadbeef")
                .build()
        )
    }

    override fun saveProject(project: Project): Observable<Project> {
        return Observable.just(project.toBuilder().isStarred(true).build())
    }

    override fun fetchSurveyResponse(surveyResponseId: Long): Observable<SurveyResponse> {
        return Observable.just(
            SurveyResponseFactory.surveyResponse().toBuilder().id(surveyResponseId).build()
        )
    }

    override fun toggleProjectSave(project: Project): Observable<Project> {
        return Observable.just(project.toBuilder().isStarred(!project.isStarred()).build())
    }

    override fun fetchUnansweredSurveys(): Observable<List<SurveyResponse>> {
        return Observable.just(
            listOf(
                SurveyResponseFactory.surveyResponse(),
                SurveyResponseFactory.surveyResponse()
            )
        )
    }

    override fun updateProjectNotifications(
        projectNotification: ProjectNotification,
        checked: Boolean
    ): Observable<ProjectNotification> {
        return Observable.just(
            projectNotification.toBuilder().email(checked).mobile(checked).build()
        )
    }

    override fun updateUserSettings(user: User): Observable<User> {
        val map: Map<String, Any> = mapOf(Pair("user", user))
        observable.onNext(
            Pair("update_user_settings", map)
        )
        return Observable.just(user)
    }

    override fun verifyEmail(token: String): Observable<EmailVerificationEnvelope> {
        return Observable.just(
            builder()
                .code(200)
                .message("")
                .build()
        )
    }
}
