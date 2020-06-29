package com.kickstarter.services

import CancelBackingMutation
import ClearUserUnseenActivityMutation
import CreateBackingMutation
import CreatePasswordMutation
import DeletePaymentSourceMutation
import ErroredBackingsQuery
import GetProjectBackingQuery
import ProjectCreatorDetailsQuery
import SavePaymentMethodMutation
import SendEmailVerificationMutation
import SendMessageMutation
import UpdateBackingMutation
import UpdateUserCurrencyMutation
import UpdateUserEmailMutation
import UpdateUserPasswordMutation
import UserPaymentsQuery
import UserPrivacyQuery
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.google.android.gms.common.util.Base64Utils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.*
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.SavePaymentMethodData
import com.kickstarter.services.mutations.UpdateBackingData
import org.joda.time.DateTime
import rx.Observable
import rx.subjects.PublishSubject
import type.BackingState
import type.CreditCardPaymentType
import type.CurrencyCode
import type.PaymentTypes
import java.nio.charset.Charset
import kotlin.math.absoluteValue

class KSApolloClient(val service: ApolloClient) : ApolloClientType {

    override fun cancelBacking(backing: Backing, note: String): Observable<Any> {
        return Observable.defer {
            val ps = PublishSubject.create<Any>()
            service.mutate(CancelBackingMutation.builder()
                    .backingId(encodeRelayId(backing))
                    .note(note)
                    .build())
                    .enqueue(object : ApolloCall.Callback<CancelBackingMutation.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<CancelBackingMutation.Data>) {
                            if (response.hasErrors()) {
                                ps.onNext(response.errors().first().message())
                            } else {
                                val state = response.data()?.cancelBacking()?.backing()?.status()
                                val success = state == BackingState.CANCELED
                                ps.onNext(success)
                            }
                            ps.onCompleted()
                        }
                    })
            return@defer ps
        }
    }

    override fun createBacking(createBackingData: CreateBackingData): Observable<Checkout> {
        return Observable.defer {
            val createBackingMutation = CreateBackingMutation.builder()
                    .projectId(encodeRelayId(createBackingData.project))
                    .amount(createBackingData.amount)
                    .paymentType(PaymentTypes.CREDIT_CARD.rawValue())
                    .paymentSourceId(createBackingData.paymentSourceId)
                    .locationId(createBackingData.locationId?.let { it })
                    .rewardId(createBackingData.reward?.let { encodeRelayId(it) })
                    .refParam(createBackingData.refTag?.tag())
                    .build()

            val ps = PublishSubject.create<Checkout>()

            this.service.mutate(createBackingMutation)
                    .enqueue(object : ApolloCall.Callback<CreateBackingMutation.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<CreateBackingMutation.Data>) {
                            if (response.hasErrors()) {
                                ps.onError(java.lang.Exception(response.errors().first().message()))
                            }

                            val checkoutPayload = response.data()?.createBacking()?.checkout()
                            val backing = Checkout.Backing.builder()
                                    .clientSecret(checkoutPayload?.backing()?.clientSecret())
                                    .requiresAction(checkoutPayload?.backing()?.requiresAction()?: false)
                                    .build()

                            ps.onNext(Checkout.builder()
                                    .id(decodeRelayId(checkoutPayload?.id()))
                                    .backing(backing)
                                    .build())
                            ps.onCompleted()
                        }
                    })
            return@defer ps
        }
    }

    override fun getBacking(backingId: String): Observable<Backing> {
        return Observable.defer {
            val ps = PublishSubject.create<Backing>()

            this.service.query(GetBackingQuery.builder()
                    .backingId(backingId).build())
                    .enqueue(object : ApolloCall.Callback<GetBackingQuery.Data>() {
                        override fun onFailure(e: ApolloException) {
                            ps.onError(e)
                        }

                        override fun onResponse(response: Response<GetBackingQuery.Data>) {
                            response.data()?.let {data ->
                                Observable.just(data.backing())
                                        .filter { it?.fragments()?.backing() != null }
                                        .map { backingObj -> createBackingObject(backingObj?.fragments()?.backing()) }
                                        .filter { ObjectUtils.isNotNull(it) }
                                        .subscribe {
                                            ps.onNext(it)
                                            ps.onCompleted()
                                        }
                            }
                        }
                    })
            return@defer ps
        }
    }

    override fun clearUnseenActivity(): Observable<Int> {
        return Observable.defer {
            val ps = PublishSubject.create<Int>()
            service.mutate(ClearUserUnseenActivityMutation.builder()
                    .build())
                    .enqueue(object : ApolloCall.Callback<ClearUserUnseenActivityMutation.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<ClearUserUnseenActivityMutation.Data>) {
                            if (response.hasErrors()) {
                                ps.onError(java.lang.Exception(response.errors().first().message()))
                            }
                            response.data()?.clearUserUnseenActivity()?.activityIndicatorCount().let {
                                handleResponse(it, ps)
                            }
                        }
                    })
            return@defer ps
        }
    }

    override fun createPassword(password: String, confirmPassword: String): Observable<CreatePasswordMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<CreatePasswordMutation.Data>()
            service.mutate(CreatePasswordMutation.builder()
                    .password(password)
                    .passwordConfirmation(confirmPassword)
                    .build())
                    .enqueue(object : ApolloCall.Callback<CreatePasswordMutation.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<CreatePasswordMutation.Data>) {
                            if (response.hasErrors()) {
                                ps.onError(java.lang.Exception(response.errors().first().message()))
                            }
                            ps.onNext(response.data())
                            ps.onCompleted()
                        }
                    })
            return@defer ps
        }
    }

    override fun creatorDetails(slug: String): Observable<CreatorDetails> {
        return Observable.defer {
            val ps = PublishSubject.create<CreatorDetails>()
            service.query(ProjectCreatorDetailsQuery.builder()
                    .slug(slug)
                    .build())
                    .enqueue(object : ApolloCall.Callback<ProjectCreatorDetailsQuery.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<ProjectCreatorDetailsQuery.Data>) {
                            if (response.hasErrors()) {
                                ps.onError(Exception(response.errors().first().message()))
                            }

                            response.data()?.project()?.creator()?.let {
                                ps.onNext(CreatorDetails.builder()
                                        .backingsCount(it.backingsCount())
                                        .launchedProjectsCount(it.launchedProjects()?.totalCount() ?: 1)
                                        .build())
                                ps.onCompleted()
                            }
                        }
                    })
            return@defer ps
        }
    }

    override fun deletePaymentSource(paymentSourceId: String): Observable<DeletePaymentSourceMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<DeletePaymentSourceMutation.Data>()
            service.mutate(DeletePaymentSourceMutation.builder()
                    .paymentSourceId(paymentSourceId)
                    .build())
                    .enqueue(object : ApolloCall.Callback<DeletePaymentSourceMutation.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<DeletePaymentSourceMutation.Data>) {
                            if (response.hasErrors()) {
                                ps.onError(Exception(response.errors().first().message()))
                            }
                            ps.onNext(response.data())
                            ps.onCompleted()
                        }
                    })
            return@defer ps
        }
    }

    override fun erroredBackings(): Observable<List<ErroredBacking>> {
        return Observable.defer {
            val ps = PublishSubject.create<List<ErroredBacking>>()
            this.service.query(ErroredBackingsQuery.builder().build())
                    .enqueue(object : ApolloCall.Callback<ErroredBackingsQuery.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<ErroredBackingsQuery.Data>) {
                            if (response.hasErrors()) {
                                ps.onError(Exception(response.errors().first().message()))
                            } else {
                                Observable.just(response.data())
                                        .map { cards -> cards?.me()?.backings()?.nodes() }
                                        .map { list ->
                                            val erroredBackings = list?.asSequence()?.map {
                                                val project = ErroredBacking.Project.builder()
                                                        .finalCollectionDate(it.project()?.finalCollectionDate())
                                                        .name(it.project()?.name())
                                                        .slug(it.project()?.slug())
                                                        .build()
                                                ErroredBacking.builder()
                                                        .project(project)
                                                        .build()
                                            }
                                            erroredBackings?.toList() ?: listOf()
                                        }
                                        .subscribe {
                                            ps.onNext(it)
                                            ps.onCompleted()
                                        }
                            }
                        }
                    })
            return@defer ps
        }
    }

    override fun getProjectBacking(slug: String): Observable<Backing> {
        return Observable.defer {
            val ps = PublishSubject.create<Backing>()

            this.service.query(GetProjectBackingQuery.builder()
                    .slug(slug).build())
                    .enqueue(object : ApolloCall.Callback<GetProjectBackingQuery.Data>() {
                        override fun onFailure(e: ApolloException) {
                            ps.onError(e)
                        }

                        override fun onResponse(response: Response<GetProjectBackingQuery.Data>) {
                            response.data()?.let {data ->
                                Observable.just(data.project()?.backing())
                                        .filter { it?.fragments()?.backing() != null }
                                        .map { backingObj -> createBackingObject(backingObj?.fragments()?.backing()) }
                                        .subscribe {
                                            ps.onNext(it)
                                            ps.onCompleted()
                                        }
                            }
                        }
                    })
            return@defer ps
        }
    }

    override fun getStoredCards(): Observable<List<StoredCard>> {
        return Observable.defer {
            val ps = PublishSubject.create<List<StoredCard>>()
            this.service.query(UserPaymentsQuery.builder().build())
                    .enqueue(object : ApolloCall.Callback<UserPaymentsQuery.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<UserPaymentsQuery.Data>) {
                            if (response.hasErrors()) {
                                ps.onError(Exception(response.errors().first().message()))
                            } else {
                                Observable.just(response.data())
                                        .map { cards -> cards?.me()?.storedCards()?.nodes() }
                                        .map { list ->
                                            val storedCards = list?.asSequence()?.map {
                                                StoredCard.builder()
                                                        .expiration(it.expirationDate())
                                                        .id(it.id())
                                                        .lastFourDigits(it.lastFour())
                                                        .type(it.type())
                                                        .build()
                                            }
                                            storedCards?.toList() ?: listOf()
                                        }
                                        .subscribe {
                                            ps.onNext(it)
                                            ps.onCompleted()
                                        }
                            }
                        }
                    })
            return@defer ps
        }
    }

    override fun savePaymentMethod(savePaymentMethodData: SavePaymentMethodData): Observable<StoredCard> {
        return Observable.defer {
            val ps = PublishSubject.create<StoredCard>()
            service.mutate(SavePaymentMethodMutation.builder()
                    .paymentType(savePaymentMethodData.paymentType)
                    .stripeToken(savePaymentMethodData.stripeToken)
                    .stripeCardId(savePaymentMethodData.stripeCardId)
                    .reusable(savePaymentMethodData.reusable)
                    .build())
                    .enqueue(object : ApolloCall.Callback<SavePaymentMethodMutation.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<SavePaymentMethodMutation.Data>) {
                            if (response.hasErrors()) {
                                ps.onError(Exception(response.errors().first().message()))
                            }

                            val paymentSource = response.data()?.createPaymentSource()?.paymentSource()
                            paymentSource?.let {
                                val storedCard = StoredCard.builder()
                                        .expiration(it.expirationDate())
                                        .id(it.id())
                                        .lastFourDigits(it.lastFour())
                                        .type(it.type())
                                        .build()
                                ps.onNext(storedCard)
                                ps.onCompleted()
                            }
                        }
                    })
            return@defer ps
        }
    }

    override fun sendMessage(project: Project, recipient: User, body: String): Observable<Long> {
        return Observable.defer {
            val ps = PublishSubject.create<Long>()
            service.mutate(SendMessageMutation.builder()
                    .projectId(encodeRelayId(project))
                    .recipientId(encodeRelayId(recipient))
                    .body(body)
                    .build())
                    .enqueue(object : ApolloCall.Callback<SendMessageMutation.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<SendMessageMutation.Data>) {
                            if (response.hasErrors()) {
                                ps.onError(Exception(response.errors().first().message()))
                            }
                            handleResponse(decodeRelayId(response.data()?.sendMessage()?.conversation()?.id()), ps)
                        }
                    })
            return@defer ps
        }
    }

    override fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<SendEmailVerificationMutation.Data>()
            service.mutate(SendEmailVerificationMutation.builder()
                    .build())
                    .enqueue(object : ApolloCall.Callback<SendEmailVerificationMutation.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<SendEmailVerificationMutation.Data>) {
                            if (response.hasErrors()) {
                                ps.onError(Exception(response.errors().first().message()))
                            }
                            ps.onNext(response.data())
                            ps.onCompleted()
                        }
                    })
            return@defer ps
        }
    }

    override fun updateBacking(updateBackingData: UpdateBackingData): Observable<Checkout> {
        return Observable.defer {
            val updateBackingMutation = UpdateBackingMutation.builder()
                    .backingId(encodeRelayId(updateBackingData.backing))
                    .amount(updateBackingData.amount.toString())
                    .locationId(updateBackingData.locationId)
                    .rewardId(updateBackingData.reward?.let { encodeRelayId(it) })
                    .paymentSourceId(updateBackingData.paymentSourceId)
                    .build()

            val ps = PublishSubject.create<Checkout>()
            service.mutate(updateBackingMutation)
                    .enqueue(object : ApolloCall.Callback<UpdateBackingMutation.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<UpdateBackingMutation.Data>) {
                            if (response.hasErrors()) {
                                ps.onError(java.lang.Exception(response.errors().first().message()))
                            }

                            val checkoutPayload = response.data()?.updateBacking()?.checkout()
                            val backing = Checkout.Backing.builder()
                                    .clientSecret(checkoutPayload?.backing()?.clientSecret())
                                    .requiresAction(checkoutPayload?.backing()?.requiresAction()?: false)
                                    .build()

                            ps.onNext(Checkout.builder()
                                    .id(decodeRelayId(checkoutPayload?.id()))
                                    .backing(backing)
                                    .build())
                            ps.onCompleted()
                        }
                    })
            return@defer ps
        }
    }

    override fun updateUserCurrencyPreference(currency: CurrencyCode): Observable<UpdateUserCurrencyMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<UpdateUserCurrencyMutation.Data>()
            service.mutate(UpdateUserCurrencyMutation.builder()
                    .chosenCurrency(currency)
                    .build())
                    .enqueue(object : ApolloCall.Callback<UpdateUserCurrencyMutation.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<UpdateUserCurrencyMutation.Data>) {
                            if (response.hasErrors()) {
                                ps.onError(Exception(response.errors().first().message()))
                            }
                            ps.onNext(response.data())
                            ps.onCompleted()
                        }
                    })
            return@defer ps
        }
    }

    override fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<UpdateUserEmailMutation.Data>()
            service.mutate(UpdateUserEmailMutation.builder()
                    .email(email)
                    .currentPassword(currentPassword)
                    .build())
                    .enqueue(object : ApolloCall.Callback<UpdateUserEmailMutation.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<UpdateUserEmailMutation.Data>) {
                            if (response.hasErrors()) {
                                ps.onError(Exception(response.errors().first().message()))
                            }
                            ps.onNext(response.data())
                            ps.onCompleted()
                        }
                    })
            return@defer ps
        }
    }

    override fun updateUserPassword(currentPassword: String, newPassword: String, confirmPassword: String): Observable<UpdateUserPasswordMutation.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<UpdateUserPasswordMutation.Data>()
            service.mutate(UpdateUserPasswordMutation.builder()
                    .currentPassword(currentPassword)
                    .password(newPassword)
                    .passwordConfirmation(confirmPassword)
                    .build())
                    .enqueue(object : ApolloCall.Callback<UpdateUserPasswordMutation.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<UpdateUserPasswordMutation.Data>) {
                            if (response.hasErrors()) {
                                ps.onError(Exception(response.errors().first().message()))
                            }
                            ps.onNext(response.data())
                            ps.onCompleted()
                        }
                    })
            return@defer ps
        }
    }

    override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
        return Observable.defer {
            val ps = PublishSubject.create<UserPrivacyQuery.Data>()
            service.query(UserPrivacyQuery.builder().build())
                    .enqueue(object : ApolloCall.Callback<UserPrivacyQuery.Data>() {
                        override fun onFailure(exception: ApolloException) {
                            ps.onError(exception)
                        }

                        override fun onResponse(response: Response<UserPrivacyQuery.Data>) {
                            ps.onNext(response.data())
                            ps.onCompleted()
                        }
                    })
            return@defer ps
        }
    }
}

private fun createBackingObject(backingGr: fragment.Backing?): Backing {
    val payment = backingGr?.paymentSource()?.fragments()?.payment()?.let { payment ->
        Backing.PaymentSource.builder()
                .state(payment.state().toString())
                .type(payment.type().rawValue())
                .paymentType(CreditCardPaymentType.CREDIT_CARD.rawValue())
                .id(payment.id())
                .expirationDate(payment.expirationDate())
                .lastFour(payment.lastFour())
                .build()
    }

    val addOns = backingGr?.addOns()?.let {
        return@let getAddOnsList(it)
    }

    val id = decodeRelayId(backingGr?.id())?.let { it } ?: 0

    val location = backingGr?.location()?.fragments()?.location()
    val locationId = decodeRelayId(location?.id())
    val projectId = decodeRelayId(backingGr?.project()?.fragments()?.project()?.id()) ?: -1
    val shippingAmount = backingGr?.shippingAmount()?.fragments()

    val reward = backingGr?.reward()?.fragments()?.reward()?.let { reward ->
        val rewardId = decodeRelayId(reward.id()) ?: -1
        val rewardAmount = reward.amount().fragments().amount().amount()?.toDouble()
        val rewardSingleLocation = location?.let { location ->
            return@let Reward.SingleLocation.builder()
                    .localizedName(location.displayableName())
                    .id(decodeRelayId(location.id())?:-1)
                    .build()
        }

        return@let Reward.builder()
                .title(reward.name())
                .minimum(rewardAmount?: -1.0)
                .description(reward.description())
                .isAddOn(false)
                .estimatedDeliveryOn(DateTime(reward.estimatedDeliveryOn()))
                .shippingSingleLocation(rewardSingleLocation)
                .id(rewardId)
                .build()
    }

    val backerData = backingGr?.backer()?.fragments()?.user()
    val nameBacker = backerData?.name()
    val backerId= decodeRelayId(backerData?.id()) ?: -1
    val avatar = Avatar.builder()
            .medium(backerData?.imageUrl())
            .build()

    val backer = User.builder()
            .id(backerId)
            .name(nameBacker)
            .avatar(avatar)
            .build()

    return Backing.builder()
            .amount(backingGr?.amount()?.fragments()?.amount()?.amount()?.toDouble() ?: 0.0)
            .bonusAmount(backingGr?.bonusAmount()?.fragments()?.amount()?.amount()?.toDouble() ?: 0.0)
            .paymentSource(payment)
            .backerId(backerId)
            .backerUrl(backerData?.imageUrl())
            .backerName(nameBacker)
            .backer(backer)
            .id(id)
            .reward(reward)
            .addOns(addOns)
            .rewardId(reward?.id())
            .locationId(locationId)
            .locationName(location?.displayableName())
            .pledgedAt(backingGr?.pledgedOn())
            .projectId(projectId)
            .sequence(backingGr?.sequence()?.toLong() ?: 0)
            .shippingAmount(shippingAmount?.amount()?.amount()?.toFloat() ?: 0f)
            .status(backingGr?.status()?.rawValue())
            .cancelable(backingGr?.cancelable() ?: false)
            .build()
}

fun <T : Relay> encodeRelayId(relay: T): String {
    val classSimpleName = relay.javaClass.simpleName.replaceFirst("AutoParcel_", "")
    val id = relay.id()
    return Base64Utils.encodeUrlSafe(("$classSimpleName-$id").toByteArray(Charset.defaultCharset()))
}

fun decodeRelayId(encodedRelayId: String?): Long? {
    return try {
        String(Base64Utils.decode(encodedRelayId), Charset.defaultCharset())
                .replaceBeforeLast("-", "", "")
                .toLong()
                .absoluteValue
    } catch (e: Exception) {
        null
    }
}

private fun <T : Any?> handleResponse(it: T, ps: PublishSubject<T>) {
    when {
        ObjectUtils.isNull(it) -> {
            ps.onError(Exception())
        }
        else -> {
            ps.onNext(it)
            ps.onCompleted()
        }
    }
}

/**
 * For addOns we receive this kind of data structure :[D, D, D, D, D, C, E, E]
 * and we need to transform it in : D(5),C(1),E(2)
 */
fun getAddOnsList(addOns: fragment.Backing.AddOns): List<Reward> {
    val mutableMap = mutableMapOf<Reward, Int>()

    val rewardsList = addOns.nodes()?.map { node ->
        val rewardGr = node.fragments().reward()
        val amount = rewardGr.amount().fragments().amount().amount()?.toDouble() ?: 0.0
        val convertedAmount = rewardGr.convertedAmount().fragments().amount().amount()?.toDouble() ?: 0.0
        val desc = rewardGr.description()
        val title = rewardGr.name()
        val estimatedDelivery = DateTime(rewardGr.estimatedDeliveryOn())
        val rewardId = decodeRelayId(rewardGr.id()) ?: -1

        val items = rewardGr.items()?.let {
            return@let getAddonItems(it)
        }

        return@map Reward.builder()
                .title(title)
                .convertedMinimum(convertedAmount)
                .minimum(amount)
                .description(desc)
                .estimatedDeliveryOn(estimatedDelivery)
                .isAddOn(true)
                .addOnsItems(items)
                .id(rewardId)
                .build()
    }

    rewardsList?.map {
        var addOnQuantity = mutableMap.getOrPut(it, {0})
        if (addOnQuantity != null) addOnQuantity += 1

        mutableMap.put(it, addOnQuantity)
    }

    return mutableMap.map {
        return@map it.key
                .toBuilder()
                .quantity(it.value)
                .build()
    }.toList()
}

fun getAddonItems(items: fragment.Reward.Items): List<RewardsItem> {
    val rewardItems = items.edges()?.map { edge ->
        val quantity = edge.quantity()
        val description = edge.node()?.name()
        val hasBackers = edge.node()?.hasBackers() ?: false
        val id = decodeRelayId(edge.node()?.id()) ?: -1
        val projectId = decodeRelayId(edge.node()?.project()?.id()) ?: -1

        val item = Item.builder()
                .description(description)
                .id(id)
                .projectId(projectId)
                .build()

        return@map RewardsItem.builder()
                .item(item)
                .hasBackers(hasBackers)
                .quantity(quantity)
                .build()
    } ?: emptyList()
    return rewardItems.toList()
}
