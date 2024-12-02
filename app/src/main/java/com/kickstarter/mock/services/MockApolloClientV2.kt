package com.kickstarter.mock.services

import android.util.Pair
import com.kickstarter.CreatePasswordMutation
import com.kickstarter.DeletePaymentSourceMutation
import com.kickstarter.SendEmailVerificationMutation
import com.kickstarter.UpdateUserCurrencyMutation
import com.kickstarter.UpdateUserEmailMutation
import com.kickstarter.UpdateUserPasswordMutation
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsOverviewEnvelope
import com.kickstarter.features.pledgedprojectsoverview.data.PledgedProjectsOverviewQueryData
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.CategoryFactory
import com.kickstarter.mock.factories.CommentFactory
import com.kickstarter.mock.factories.ErroredBackingFactory
import com.kickstarter.mock.factories.PageInfoEnvelopeFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.StoredCardFactory
import com.kickstarter.mock.factories.UpdateFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.Category
import com.kickstarter.models.Checkout
import com.kickstarter.models.CheckoutPayment
import com.kickstarter.models.Comment
import com.kickstarter.models.CompleteOrderInput
import com.kickstarter.models.CompleteOrderPayload
import com.kickstarter.models.CreatePaymentIntentInput
import com.kickstarter.models.CreatorDetails
import com.kickstarter.models.ErroredBacking
import com.kickstarter.models.Location
import com.kickstarter.models.PaymentValidationResponse
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.models.User
import com.kickstarter.models.UserPrivacy
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.DiscoveryParams
import com.kickstarter.services.apiresponses.DiscoverEnvelope
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import com.kickstarter.services.apiresponses.commentresponse.CommentEnvelope
import com.kickstarter.services.apiresponses.updatesresponse.UpdatesGraphQlEnvelope
import com.kickstarter.services.mutations.CreateAttributionEventData
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.CreateCheckoutData
import com.kickstarter.services.mutations.CreateOrUpdateBackingAddressData
import com.kickstarter.services.mutations.PostCommentData
import com.kickstarter.services.mutations.SavePaymentMethodData
import com.kickstarter.services.mutations.UpdateBackingData
import com.kickstarter.type.CurrencyCode
import com.kickstarter.viewmodels.usecases.TPEventInputData
import java.util.Collections

open class MockApolloClientV2 : ApolloClientTypeV2 {

    override fun getRewardsFromProject(slug: String): io.reactivex.Observable<List<Reward>> {
        return io.reactivex.Observable.just(emptyList())
    }

    override fun watchProject(project: Project): io.reactivex.Observable<Project> {
        return io.reactivex.Observable.just(project.toBuilder().isStarred(true).build())
    }

    override fun unWatchProject(project: Project): io.reactivex.Observable<Project> {
        return io.reactivex.Observable.just(project.toBuilder().isStarred(false).build())
    }

    override fun updateUserPassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): io.reactivex.Observable<UpdateUserPasswordMutation.Data> {
        return io.reactivex.Observable.just(
            UpdateUserPasswordMutation.Data(
                UpdateUserPasswordMutation.UpdateUserAccount(
                    UpdateUserPasswordMutation.User("some@email.com", true, true)
                )
            )
        )
    }
    override fun updateUserEmail(
        email: String,
        currentPassword: String
    ): io.reactivex.Observable<UpdateUserEmailMutation.Data> {
        return io.reactivex.Observable.just(
            UpdateUserEmailMutation.Data(
                UpdateUserEmailMutation.UpdateUserAccount(
                    UpdateUserEmailMutation.User("Some Name", "some@email.com")
                )
            )
        )
    }

    override fun sendVerificationEmail(): io.reactivex.Observable<SendEmailVerificationMutation.Data> {
        return io.reactivex.Observable.just(
            SendEmailVerificationMutation.Data(
                SendEmailVerificationMutation.UserSendEmailVerification(
                    "12345"
                )
            )
        )
    }

    override fun getProject(project: Project): io.reactivex.Observable<Project> {
        return io.reactivex.Observable.just(ProjectFactory.backedProject())
    }

    override fun getProject(slug: String): io.reactivex.Observable<Project> {
        return io.reactivex.Observable.just(ProjectFactory.backedProject())
    }

    override fun getProjects(discoveryParams: DiscoveryParams, cursor: String?): io.reactivex.Observable<DiscoverEnvelope> {
        return io.reactivex.Observable.just(
            DiscoverEnvelope
                .builder()
                .projects(
                    listOf(
                        ProjectFactory.project(),
                        ProjectFactory.allTheWayProject(),
                        ProjectFactory.successfulProject()
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

    override fun createSetupIntent(project: Project?): io.reactivex.Observable<String> {
        return io.reactivex.Observable.just("")
    }

    override fun getStoredCards(): io.reactivex.Observable<List<StoredCard>> {
        return io.reactivex.Observable.just(Collections.singletonList(StoredCardFactory.discoverCard()))
    }

    override fun deletePaymentSource(paymentSourceId: String): io.reactivex.Observable<DeletePaymentSourceMutation.Data> {
        return io.reactivex.Observable.just(DeletePaymentSourceMutation.Data(DeletePaymentSourceMutation.PaymentSourceDelete("")))
    }

    override fun savePaymentMethod(savePaymentMethodData: SavePaymentMethodData): io.reactivex.Observable<StoredCard> {
        return io.reactivex.Observable.just(StoredCardFactory.discoverCard())
    }

    override fun createFlagging(project: Project?, details: String, flaggingKind: String): io.reactivex.Observable<String> {
        return io.reactivex.Observable.empty<String>()
    }

    override fun userPrivacy(): io.reactivex.Observable<UserPrivacy> {
        return io.reactivex.Observable.just(
            UserPrivacy(
                "Some Name",
                "some@email.com",
                true,
                true,
                true,
                true,
                "USD"
            )
        )
    }

    override fun updateUserCurrencyPreference(currency: CurrencyCode): io.reactivex.Observable<UpdateUserCurrencyMutation.Data> {
        return io.reactivex.Observable.empty()
    }

    override fun getShippingRules(reward: Reward): io.reactivex.Observable<ShippingRulesEnvelope> {
        return io.reactivex.Observable.empty()
    }

    override fun getProjectAddOns(
        slug: String,
        locationId: Location
    ): io.reactivex.Observable<List<Reward>> {
        return io.reactivex.Observable.empty()
    }
    override fun updateBacking(updateBackingData: UpdateBackingData): io.reactivex.Observable<Checkout> {
        return io.reactivex.Observable.empty()
    }

    override fun createBacking(createBackingData: CreateBackingData): io.reactivex.Observable<Checkout> {
        return io.reactivex.Observable.empty()
    }
    override fun triggerThirdPartyEvent(eventInput: TPEventInputData): io.reactivex.Observable<Pair<Boolean, String>> {
        return io.reactivex.Observable.empty()
    }

    override fun createPassword(password: String, confirmPassword: String): io.reactivex.Observable<CreatePasswordMutation.Data> {
        return io.reactivex.Observable.empty()
    }

    override fun creatorDetails(slug: String): io.reactivex.Observable<CreatorDetails> {
        return io.reactivex.Observable.empty()
    }

    override fun sendMessage(project: Project, recipient: User, body: String): io.reactivex.Observable<Long> {
        return io.reactivex.Observable.just(1L)
    }

    override fun cancelBacking(backing: Backing, note: String): io.reactivex.Observable<Any> {
        return io.reactivex.Observable.just(true)
    }

    override fun fetchCategory(param: String): io.reactivex.Observable<Category?> {
        return io.reactivex.Observable.just(CategoryFactory.musicCategory())
    }

    override fun getBacking(backingId: String): io.reactivex.Observable<Backing> {
        return io.reactivex.Observable.just(BackingFactory.backing())
    }

    override fun fetchCategories(): io.reactivex.Observable<List<Category>> {
        return io.reactivex.Observable.just(CategoryFactory.rootCategories())
    }

    override fun getProjectUpdates(
        slug: String,
        cursor: String,
        limit: Int
    ): io.reactivex.Observable<UpdatesGraphQlEnvelope> {
        return io.reactivex.Observable.just(
            UpdatesGraphQlEnvelope.builder()
                .pageInfoEnvelope(
                    PageInfoEnvelopeFactory.pageInfoEnvelope()
                )
                .updates(listOf(UpdateFactory.update()))
                .totalCount(1)
                .build()
        )
    }
    override fun getComment(commentableId: String): io.reactivex.Observable<Comment> {
        return io.reactivex.Observable.just(CommentFactory.comment())
    }

    override fun getProjectUpdateComments(
        updateId: String,
        cursor: String,
        limit: Int
    ): io.reactivex.Observable<CommentEnvelope> {
        return io.reactivex.Observable.just(
            CommentEnvelope.builder()
                .pageInfoEnvelope(
                    PageInfoEnvelopeFactory.pageInfoEnvelope()
                )
                .comments(listOf(CommentFactory.comment()))
                .commentableId(updateId)
                .totalCount(1)
                .build()
        )
    }

    override fun getProjectComments(slug: String, cursor: String, limit: Int): io.reactivex.Observable<CommentEnvelope> {
        return io.reactivex.Observable.just(
            CommentEnvelope.builder()
                .pageInfoEnvelope(
                    PageInfoEnvelopeFactory.pageInfoEnvelope()
                )
                .comments(listOf(CommentFactory.comment()))
                .totalCount(1)
                .build()
        )
    }

    override fun getRepliesForComment(
        comment: Comment,
        cursor: String?,
        pageSize: Int
    ): io.reactivex.Observable<CommentEnvelope> {
        return io.reactivex.Observable.empty()
    }

    override fun createComment(comment: PostCommentData): io.reactivex.Observable<Comment> {
        return io.reactivex.Observable.just(CommentFactory.comment())
    }

    override fun erroredBackings(): io.reactivex.Observable<List<ErroredBacking>> {
        return io.reactivex.Observable.just(Collections.singletonList(ErroredBackingFactory.erroredBacking()))
    }

    override fun clearUnseenActivity(): io.reactivex.Observable<Int> {
        return io.reactivex.Observable.just(0)
    }

    override fun getProjectBacking(slug: String): io.reactivex.Observable<Backing> {
        return io.reactivex.Observable.just(BackingFactory.backing())
    }

    override fun createCheckout(createCheckoutData: CreateCheckoutData): io.reactivex.Observable<CheckoutPayment> {
        return io.reactivex.Observable.empty()
    }

    override fun createAttributionEvent(eventInput: CreateAttributionEventData): io.reactivex.Observable<Boolean> {
        return io.reactivex.Observable.empty()
    }

    override fun validateCheckout(
        checkoutId: String,
        paymentIntentClientSecret: String,
        paymentSourceId: String
    ): io.reactivex.Observable<PaymentValidationResponse> {
        return io.reactivex.Observable.empty()
    }

    override fun completeOnSessionCheckout(
        checkoutId: String,
        paymentIntentClientSecret: String,
        paymentSourceId: String?,
        paymentSourceReusable: Boolean
    ): io.reactivex.Observable<Pair<String, Boolean>> {
        return io.reactivex.Observable.empty()
    }

    override fun createPaymentIntent(createPaymentIntentInput: CreatePaymentIntentInput): io.reactivex.Observable<String> {
        return io.reactivex.Observable.empty()
    }

    override fun createOrUpdateBackingAddress(eventInput: CreateOrUpdateBackingAddressData): io.reactivex.Observable<Boolean> {
        return io.reactivex.Observable.empty()
    }

    override fun completeOrder(orderInput: CompleteOrderInput): io.reactivex.Observable<CompleteOrderPayload> {
        return io.reactivex.Observable.empty()
    }

    override fun getPledgedProjectsOverviewPledges(inputData: PledgedProjectsOverviewQueryData): io.reactivex.Observable<PledgedProjectsOverviewEnvelope> {
        return io.reactivex.Observable.empty()
    }

    override fun cleanDisposables() {
        TODO("Not yet implemented")
    }
}
