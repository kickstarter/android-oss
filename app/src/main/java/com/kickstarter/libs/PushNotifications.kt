package com.kickstarter.libs

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Pair
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.R
import com.kickstarter.libs.RefTag.Companion.push
import com.kickstarter.libs.qualifiers.ApplicationContext
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.getProjectIntent
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.MessageThread
import com.kickstarter.models.SurveyResponse
import com.kickstarter.models.Update
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.apiresponses.MessageThreadEnvelope
import com.kickstarter.services.apiresponses.PushNotificationEnvelope
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.ActivityFeedActivity
import com.kickstarter.ui.activities.MessagesActivity
import com.kickstarter.ui.activities.ProjectPageActivity
import com.kickstarter.ui.activities.SurveyResponseActivity
import com.kickstarter.ui.activities.UpdateActivity
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.ExecutionException

class PushNotifications(
    @field:ApplicationContext @param:ApplicationContext private val context: Context,
    private val client: ApiClientTypeV2
) {
    private val notifications: PublishSubject<PushNotificationEnvelope> = PublishSubject.create()
    private val subscriptions = CompositeDisposable()

    @VisibleForTesting
    var messageThreadIntent: Intent? = null

    fun initialize() {
        createNotificationChannels()

        subscriptions.add(
            notifications
                .filter { obj: PushNotificationEnvelope -> obj.isErroredPledge() }
                .observeOn(Schedulers.newThread())
                .subscribe { envelope: PushNotificationEnvelope ->
                    this.displayNotificationFromErroredPledge(
                        envelope
                    )
                }
        )

        subscriptions.add(
            notifications
                .filter { obj: PushNotificationEnvelope -> obj.isFriendFollow() }
                .observeOn(Schedulers.newThread())
                .subscribe { envelope: PushNotificationEnvelope ->
                    this.displayNotificationFromFriendFollowActivity(
                        envelope
                    )
                }
        )

        subscriptions.add(
            notifications
                .filter { obj: PushNotificationEnvelope -> obj.isMessage() }
                .flatMap { envelope: PushNotificationEnvelope ->
                    this.fetchMessageThreadWithEnvelope(
                        envelope
                    )
                }
                .filter { isNotNull() }
                .observeOn(Schedulers.newThread())
                .subscribe { envelopeAndMessageThread ->
                    this.displayNotificationFromMessageActivity(
                        envelopeAndMessageThread.first, envelopeAndMessageThread.second
                    )
                }
        )

        subscriptions.add(
            notifications
                .filter { obj: PushNotificationEnvelope -> obj.isProjectActivity() }
                .observeOn(Schedulers.newThread())
                .subscribe { envelope: PushNotificationEnvelope ->
                    this.displayNotificationFromProjectActivity(
                        envelope
                    )
                }
        )

        subscriptions.add(
            notifications
                .filter { obj: PushNotificationEnvelope -> obj.isProjectReminder() }
                .observeOn(Schedulers.newThread())
                .subscribe { envelope: PushNotificationEnvelope ->
                    this.displayNotificationFromProjectReminder(
                        envelope
                    )
                }
        )

        subscriptions.add(
            notifications
                .filter { obj: PushNotificationEnvelope -> obj.isProjectUpdateActivity() }
                .flatMap { envelope: PushNotificationEnvelope ->
                    this.fetchUpdateWithEnvelope(
                        envelope
                    )
                }
                .filter { isNotNull() }
                .observeOn(Schedulers.newThread())
                .subscribe { envelopeAndUpdate: Pair<PushNotificationEnvelope, Update> ->
                    this.displayNotificationFromUpdateActivity(
                        envelopeAndUpdate.first,
                        envelopeAndUpdate.second
                    )
                }
        )

        subscriptions.add(
            notifications
                .filter { obj: PushNotificationEnvelope -> obj.isSurvey() }
                .filter {
                    isNotNull()
                }
                .observeOn(Schedulers.newThread())
                .subscribe {
                    this.displayNotificationFromSurveyResponseActivity(
                        it
                    )
                }
        )
        subscriptions.add(
            notifications
                .filter { obj: PushNotificationEnvelope -> obj.isPledgeRedemption() }
                .filter {
                    isNotNull()
                }
                .observeOn(Schedulers.newThread())
                .subscribe {
                    this.displayNotificationFromPledgeRedemption(
                        it
                    )
                }
        )
    }

    fun add(envelope: PushNotificationEnvelope) {
        notifications.onNext(envelope)
    }

    private fun createNotificationChannels() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (ApiCapabilities.canCreateNotificationChannels()) {
            val channels = listOfNotificationChannels
            // Register the channels with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = context.getSystemService(
                NotificationManager::class.java
            )
            if (notificationManager.isNotNull<NotificationManager>()) {
                notificationManager.createNotificationChannels(channels)
            }
        }
    }

    @get:TargetApi(Build.VERSION_CODES.O)
    private val listOfNotificationChannels: List<NotificationChannel>
        get() {
            val channels: MutableList<NotificationChannel> = ArrayList(NOTIFICATION_CHANNELS.size)
            channels.add(
                getNotificationChannel(
                    CHANNEL_ERRORED_PLEDGES,
                    R.string.Fix_your_payment_method,
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
            channels.add(
                getNotificationChannel(
                    CHANNEL_MESSAGES,
                    R.string.Messages,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
            channels.add(
                getNotificationChannel(
                    CHANNEL_PROJECT_ACTIVITY,
                    R.string.Project_activity,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
            channels.add(
                getNotificationChannel(
                    CHANNEL_PROJECT_REMINDER,
                    R.string.Project_reminders,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
            channels.add(
                getNotificationChannel(
                    CHANNEL_PROJECT_UPDATES,
                    R.string.Project_updates,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
            channels.add(
                getNotificationChannel(
                    CHANNEL_PLEDGE_REDEMPTION,
                    R.string.Pledge_management,
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
            val followingChannel = getNotificationChannel(
                CHANNEL_FOLLOWING,
                R.string.Following,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            followingChannel.description =
                context.getString(R.string.When_following_is_on_you_can_follow_the_acticity_of_others)
            channels.add(followingChannel)
            channels.add(
                getNotificationChannel(
                    CHANNEL_SURVEY,
                    R.string.Reward_surveys,
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
            return channels
        }

    @TargetApi(Build.VERSION_CODES.O)
    private fun getNotificationChannel(
        channelId: String,
        nameResId: Int,
        importance: Int
    ): NotificationChannel {
        val name: CharSequence = context.getString(nameResId)
        return NotificationChannel(channelId, name, importance)
    }

    private fun displayNotificationFromErroredPledge(envelope: PushNotificationEnvelope) {
        val gcm = envelope.gcm()

        val erroredPledge = envelope.erroredPledge() ?: return

        val projectId = erroredPledge.projectId()
        val projectIntent = projectIntent(envelope, projectId.toString())
            .putExtra(IntentKey.EXPAND_PLEDGE_SHEET, true)
        val notification = notificationBuilder(gcm.title(), gcm.alert(), CHANNEL_PROJECT_REMINDER)
            .setContentIntent(projectContentIntent(envelope, projectIntent))
            .build()

        notificationManager().notify(envelope.signature(), notification)
    }

    private fun displayNotificationFromFriendFollowActivity(envelope: PushNotificationEnvelope) {
        val gcm = envelope.gcm()

        val activity = envelope.activity() ?: return

        val notification = notificationBuilder(gcm.title(), gcm.alert(), CHANNEL_FOLLOWING)
            .setLargeIcon(fetchBitmap(activity.userPhoto(), true))
            .setContentIntent(friendFollowActivityIntent(envelope))
            .build()
        notificationManager().notify(envelope.signature(), notification)
    }

    private fun displayNotificationFromMessageActivity(
        envelope: PushNotificationEnvelope,
        messageThread: MessageThread?
    ) {
        val gcm = envelope.gcm()

        val message = envelope.message()

        messageThread?.let {
            val notification = notificationBuilder(gcm.title(), gcm.alert(), CHANNEL_MESSAGES)
                .setContentIntent(messageThreadIntent(envelope, messageThread))
                .build()

            notificationManager().notify(envelope.signature(), notification)
        }
    }

    private fun displayNotificationFromProjectActivity(envelope: PushNotificationEnvelope) {
        val gcm = envelope.gcm()

        val activity = envelope.activity() ?: return
        val projectId = activity.projectId() ?: return
        val projectPhoto = activity.projectPhoto()

        var notificationBuilder =
            notificationBuilder(gcm.title(), gcm.alert(), CHANNEL_PROJECT_ACTIVITY)
                .setContentIntent(
                    projectContentIntent(
                        envelope,
                        projectIntent(envelope, projectId.toString())
                    )
                )
        if (projectPhoto != null) {
            notificationBuilder = notificationBuilder.setLargeIcon(fetchBitmap(projectPhoto, false))
        }
        val notification = notificationBuilder.build()

        notificationManager().notify(envelope.signature(), notification)
    }

    private fun displayNotificationFromProjectReminder(envelope: PushNotificationEnvelope) {
        val gcm = envelope.gcm()

        val project = envelope.project() ?: return

        val projectIntent = projectIntent(envelope, project.id().toString())
        val notification = notificationBuilder(gcm.title(), gcm.alert(), CHANNEL_PROJECT_REMINDER)
            .setContentIntent(projectContentIntent(envelope, projectIntent))
            .setLargeIcon(fetchBitmap(project.photo(), false))
            .build()

        notificationManager().notify(envelope.signature(), notification)
    }

    private fun displayNotificationFromSurveyResponseActivity(
        envelope: PushNotificationEnvelope,
    ) {
        val gcm = envelope.gcm()

        val surveyUrlPath = envelope.survey()?.urls()?.web()?.survey() ?: return

        val notification = notificationBuilder(gcm.title(), gcm.alert(), CHANNEL_SURVEY)
            .setContentIntent(surveyResponseContentIntent(envelope, surveyUrlPath, IntentKey.NOTIFICATION_SURVEY_RESPONSE))
            .build()
        notificationManager().notify(envelope.signature(), notification)
    }

    private fun displayNotificationFromPledgeRedemption(
        envelope: PushNotificationEnvelope,
    ) {
        val gcm = envelope.gcm()

        val pledgeRedemptionPath = envelope.pledgeRedemption()?.pledgeRedemptionPath() ?: return

        val notification = notificationBuilder(gcm.title(), gcm.alert(), CHANNEL_PLEDGE_REDEMPTION)
            .setContentIntent(surveyResponseContentIntent(envelope, pledgeRedemptionPath, IntentKey.NOTIFICATION_PLEDGE_REDEMPTION))
            .build()
        notificationManager().notify(envelope.signature(), notification)
    }

    private fun displayNotificationFromUpdateActivity(
        envelope: PushNotificationEnvelope,
        update: Update
    ) {
        val gcm = envelope.gcm()

        val activity = envelope.activity() ?: return
        val updateId = activity.updateId() ?: return
        val projectId = activity.projectId() ?: return

        val projectParam = projectId.toString()

        val notification = notificationBuilder(gcm.title(), gcm.alert(), CHANNEL_PROJECT_UPDATES)
            .setContentIntent(projectUpdateContentIntent(envelope, update, projectParam))
            .setLargeIcon(fetchBitmap(activity.projectPhoto(), false))
            .build()
        notificationManager().notify(envelope.signature(), notification)
    }

    private fun friendFollowActivityIntent(envelope: PushNotificationEnvelope): PendingIntent {
        val messageThreadIntent = Intent(this.context, ActivityFeedActivity::class.java)

        val taskStackBuilder = TaskStackBuilder.create(this.context)
            .addNextIntentWithParentStack(messageThreadIntent)

        return taskStackBuilder.getPendingIntent(
            envelope.signature(),
            PendingIntent.FLAG_IMMUTABLE
        )!!
    }

    @VisibleForTesting
    fun messageThreadIntent(
        envelope: PushNotificationEnvelope,
        messageThread: MessageThread
    ): PendingIntent {
        this.messageThreadIntent = Intent(this.context, MessagesActivity::class.java)
            .putExtra(IntentKey.MESSAGE_THREAD, messageThread)
            .putExtra(IntentKey.MESSAGE_SCREEN_SOURCE_CONTEXT, MessagePreviousScreenType.PUSH)

        val taskStackBuilder = TaskStackBuilder.create(this.context)
            .addNextIntentWithParentStack(messageThreadIntent!!)

        return taskStackBuilder.getPendingIntent(
            envelope.signature(),
            PendingIntent.FLAG_IMMUTABLE
        )!!
    }

    private fun notificationBuilder(
        title: String,
        text: String,
        channelId: String
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(this.context, channelId)
            .setSmallIcon(R.drawable.ic_kickstarter_micro_k)
            .setColor(ContextCompat.getColor(this.context, R.color.kds_create_700))
            .setContentText(text)
            .setContentTitle(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
    }

    private fun projectContentIntent(
        envelope: PushNotificationEnvelope,
        projectIntent: Intent
    ): PendingIntent {
        val taskStackBuilder = TaskStackBuilder.create(this.context)
            .addNextIntentWithParentStack(projectIntent)

        return taskStackBuilder.getPendingIntent(
            envelope.signature(),
            PendingIntent.FLAG_IMMUTABLE
        )!!
    }

    private fun projectUpdateContentIntent(
        envelope: PushNotificationEnvelope,
        update: Update,
        projectParam: String
    ): PendingIntent {
        val projectIntent = Intent().getProjectIntent(this.context)
            .putExtra(IntentKey.PROJECT_PARAM, projectParam)
            .putExtra(IntentKey.REF_TAG, push())

        val updateIntent = Intent(this.context, UpdateActivity::class.java)
            .putExtra(IntentKey.PROJECT_PARAM, projectParam)
            .putExtra(IntentKey.UPDATE, update)
            .putExtra(IntentKey.PUSH_NOTIFICATION_ENVELOPE, envelope)

        val taskStackBuilder = TaskStackBuilder.create(this.context)
            .addNextIntentWithParentStack(projectIntent)
            .addNextIntent(updateIntent)

        return taskStackBuilder.getPendingIntent(
            envelope.signature(),
            PendingIntent.FLAG_IMMUTABLE
        )!!
    }

    private fun surveyResponseContentIntent(
        envelope: PushNotificationEnvelope,
        urlPath: String,
        intentKey: String
    ): PendingIntent {
        val activityFeedIntent = Intent(this.context, ActivityFeedActivity::class.java)

        val surveyResponseIntent = Intent(this.context, SurveyResponseActivity::class.java)
            .putExtra(intentKey, urlPath)

        val taskStackBuilder = TaskStackBuilder.create(this.context)
            .addNextIntentWithParentStack(activityFeedIntent)
            .addNextIntent(surveyResponseIntent)

        return taskStackBuilder.getPendingIntent(
            envelope.signature(),
            PendingIntent.FLAG_IMMUTABLE
        )!!
    }

    private fun fetchBitmap(url: String?, transformIntoCircle: Boolean): Bitmap? {
        if (url == null) {
            return null
        }

        try {
            if (transformIntoCircle) {
                val circleCrop = Glide.with(this.context)
                    .asBitmap()
                    .load(url)
                    .error(R.drawable.logo)
                    .apply(RequestOptions.circleCropTransform())
                    .submit()
                return circleCrop.get()
            } else {
                val SquareRoundCorners = Glide.with(this.context)
                    .asBitmap()
                    .load(url)
                    .error(R.drawable.logo)
                    .transform(MultiTransformation(CenterCrop(), RoundedCorners(10)))
                    .submit()
                return SquareRoundCorners.get()
            }
        } catch (e: ExecutionException) {
            val error = Throwable(url, e)
            FirebaseCrashlytics.getInstance().recordException(error)
            return BitmapFactory.decodeResource(context.resources, R.drawable.logo)
        } catch (e: InterruptedException) {
            val error = Throwable(url, e)
            FirebaseCrashlytics.getInstance().recordException(error)
            return BitmapFactory.decodeResource(context.resources, R.drawable.logo)
        }
    }

    private fun notificationManager(): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun fetchMessageThreadWithEnvelope(
        envelope: PushNotificationEnvelope
    ): Observable<Pair<PushNotificationEnvelope, MessageThread?>>? {
        val message = envelope.message()?.messageThreadId()

        val messageThread = message?.let {
            client.fetchMessagesForThread(it)
                .compose(Transformers.neverErrorV2())
                .map { obj: MessageThreadEnvelope -> obj.messageThread() }
        } ?: Observable.empty()

        val envelopeAndThread = Observable.just(envelope)
            .compose(Transformers.combineLatestPair(messageThread))

        return envelopeAndThread
    }

    private fun fetchSurveyResponseWithEnvelope(
        envelope: PushNotificationEnvelope
    ): Observable<Pair<PushNotificationEnvelope, SurveyResponse>>? {
        val survey = envelope.survey() ?: return null

        val surveyResponse = client.fetchSurveyResponse(survey.id())
            .map {
                it
            }
            .compose(Transformers.neverErrorV2())

        return Observable.just(envelope)
            .map {
                it
            }
            .compose(Transformers.combineLatestPair(surveyResponse))
    }

    private fun fetchUpdateWithEnvelope(
        envelope: PushNotificationEnvelope
    ): Observable<Pair<PushNotificationEnvelope, Update>>? {
        val activity = envelope.activity() ?: return null

        val updateId = activity.updateId() ?: return null

        val projectId = activity.projectId() ?: return null

        val projectParam = projectId.toString()
        val updateParam = updateId.toString()

        val update = client.fetchUpdate(projectParam, updateParam)
            .compose(Transformers.neverErrorV2())

        return Observable.just(envelope)
            .compose(Transformers.combineLatestPair(update))
    }

    private fun projectIntent(envelope: PushNotificationEnvelope, projectParam: String): Intent {
        val intent = Intent(this.context, ProjectPageActivity::class.java)
        return intent
            .putExtra(IntentKey.PROJECT_PARAM, projectParam)
            .putExtra(IntentKey.PUSH_NOTIFICATION_ENVELOPE, envelope)
            .putExtra(IntentKey.REF_TAG, push())
    }

    companion object {
        private const val CHANNEL_ERRORED_PLEDGES = "ERRORED_PLEDGES"
        private const val CHANNEL_FOLLOWING = "FOLLOWING"
        private const val CHANNEL_MESSAGES = "MESSAGES"
        private const val CHANNEL_PROJECT_ACTIVITY = "PROJECT_ACTIVITY"
        private const val CHANNEL_PROJECT_REMINDER = "PROJECT_REMINDER"
        private const val CHANNEL_PROJECT_UPDATES = "PROJECT_UPDATES"
        private const val CHANNEL_SURVEY = "SURVEY"
        private const val CHANNEL_PLEDGE_REDEMPTION = "PLEDGE_REDEMPTION"

        private val NOTIFICATION_CHANNELS = arrayOf(
            CHANNEL_ERRORED_PLEDGES,
            CHANNEL_FOLLOWING,
            CHANNEL_MESSAGES,
            CHANNEL_PROJECT_ACTIVITY,
            CHANNEL_PROJECT_REMINDER,
            CHANNEL_PROJECT_UPDATES,
            CHANNEL_SURVEY,
            CHANNEL_PLEDGE_REDEMPTION
        )
    }
}
