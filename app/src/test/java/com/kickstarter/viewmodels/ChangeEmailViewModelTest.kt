package com.kickstarter.viewmodels

import SendEmailVerificationMutation
import UpdateUserEmailMutation
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.UserPrivacy
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.Test

class ChangeEmailViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ChangeEmailViewModel.ChangeEmailViewModel

    private val currentEmail = TestSubscriber<String>()
    private val emailErrorIsVisible = TestSubscriber<Boolean>()
    private val error = TestSubscriber<String>()
    private val sendVerificationIsHidden = TestSubscriber<Boolean>()
    private val progressBarIsVisible = TestSubscriber<Boolean>()
    private val saveButtonIsEnabled = TestSubscriber<Boolean>()
    private val success = TestSubscriber<Unit>()
    private val warningText = TestSubscriber<Int>()
    private val warningTextColor = TestSubscriber<Int>()
    private val verificationButtonText = TestSubscriber<Int>()

    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ChangeEmailViewModel.ChangeEmailViewModel(environment)

        this.vm.outputs.currentEmail().subscribe { this.currentEmail.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.emailErrorIsVisible().subscribe { this.emailErrorIsVisible.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.error().subscribe { this.error.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.sendVerificationIsHidden()
            .subscribe { this.sendVerificationIsHidden.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.progressBarIsVisible().subscribe { this.progressBarIsVisible.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.saveButtonIsEnabled().subscribe { this.saveButtonIsEnabled.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.success().subscribe { this.success.onNext(Unit) }
            .addToDisposable(disposables)
        this.vm.outputs.warningText().subscribe { this.warningText.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.warningTextColor().subscribe { this.warningTextColor.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.verificationEmailButtonText()
            .subscribe { this.verificationButtonText.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun testCurrentEmail() {
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun userPrivacy(): Observable<UserPrivacy> {
                    return Observable.just(
                        UserPrivacy("","rashad@test.com", true, true, true, true, "")
                    )
                }
            }).build()
        )

        this.currentEmail.assertValue("rashad@test.com")
        this.sendVerificationIsHidden.assertValue(true)
    }

    @Test
    fun testCurrentEmailError() {
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun userPrivacy(): Observable<UserPrivacy> {
                    return Observable.error(Throwable("error"))
                }
            }).build()
        )

        this.currentEmail.assertNoValues()
    }

    @Test
    fun testEmailErrorIsVisible() {
        setUpEnvironment(environment())

        this.vm.inputs.emailFocus(true)
        this.vm.inputs.email("izzy@")
        this.emailErrorIsVisible.assertValue(false)
        this.vm.inputs.emailFocus(false)
        this.emailErrorIsVisible.assertValues(false, true)
    }

    @Test
    fun testError() {
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun updateUserEmail(
                    email: String,
                    currentPassword: String
                ): Observable<UpdateUserEmailMutation.Data> {
                    return Observable.error(Throwable("boop"))
                }
            }).build()
        )

        this.vm.inputs.email("test@email.com")
        this.vm.inputs.password("password")
        this.vm.inputs.updateEmailClicked()
        this.error.assertValue("boop")
    }

    @Test
    fun testIsEmailUnverified() {
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun userPrivacy(): Observable<UserPrivacy> {
                    return Observable.just(
                        UserPrivacy("", "rashad@test.com", true, true, true, false, "")
                    )
                }
            }).build()
        )

        this.currentEmail.assertValue("rashad@test.com")
        this.sendVerificationIsHidden.assertValue(false)

        this.warningText.assertValue(R.string.Email_unverified)
        this.warningTextColor.assertValue(R.color.kds_support_400)
    }

    @Test
    fun testIsEmailUndeliverable() {
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun userPrivacy(): Observable<UserPrivacy> {
                    return Observable.just(
                        UserPrivacy( "", "rashad@test.com", true, true, false, false, "")
                    )
                }
            }).build()
        )

        this.currentEmail.assertValue("rashad@test.com")
        this.sendVerificationIsHidden.assertValue(false)

        this.warningText.assertValue(R.string.We_ve_been_unable_to_send_email)
        this.warningTextColor.assertValue(R.color.kds_alert)
    }

    @Test
    fun testIsUserABacker() {
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun userPrivacy(): Observable<UserPrivacy> {
                    return Observable.just(
                        UserPrivacy("", "rashad@test.com", true, false, true, true, "")
                    )
                }
            }).build()
        )

        this.currentEmail.assertValue("rashad@test.com")
        this.sendVerificationIsHidden.assertValue(true)

        this.warningText.assertValue(0)
        this.warningTextColor.assertValue(R.color.kds_support_400)
        this.verificationButtonText.assertValue(R.string.Send_verfication_email)
    }

    @Test
    fun testIsUserACreator() {
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun userPrivacy(): Observable<UserPrivacy> {
                    return Observable.just(
                        UserPrivacy("", "rashad@test.com", true, true, true, true, "")
                    )
                }
            }).build()
        )

        this.currentEmail.assertValue("rashad@test.com")
        this.sendVerificationIsHidden.assertValue(true)

        this.warningText.assertValue(0)
        this.warningTextColor.assertValue(R.color.kds_support_400)
        this.verificationButtonText.assertValue(R.string.Resend_verification_email)
    }

    @Test
    fun testProgressBarIsVisible() {
        setUpEnvironment(environment())

        this.vm.inputs.email("izzy@email.com")
        this.vm.inputs.password("password")
        this.vm.inputs.updateEmailClicked()
        this.progressBarIsVisible.assertValues(true, false)
    }

    @Test
    fun testSaveButtonIsEnabled() {
        setUpEnvironment(environment())

        this.vm.inputs.email("izzy@email.com")
        this.vm.inputs.password("password")
        this.saveButtonIsEnabled.assertValue(true)
        this.vm.inputs.email("izzy@emailcom")
        this.saveButtonIsEnabled.assertValues(true, false)
        this.vm.inputs.email("izzy@email.com")
        this.saveButtonIsEnabled.assertValues(true, false, true)
        this.vm.inputs.password("passw")
        this.saveButtonIsEnabled.assertValues(true, false, true, false)
    }

    @Test
    fun testSendVerificationEmail() {
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data> {
                    return Observable.just(
                        SendEmailVerificationMutation.Data(
                            SendEmailVerificationMutation
                                .UserSendEmailVerification("", "1234")
                        )
                    )
                }
            }).build()
        )

        this.vm.inputs.sendVerificationEmail()
        this.success.assertValueCount(1)
    }

    @Test
    fun testSendVerificationEmailError() {
        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data> {
                    return Observable.error(Throwable("error"))
                }
            }).build()
        )

        this.vm.inputs.sendVerificationEmail()
        this.error.assertValue("error")
    }

    @Test
    fun testSuccess() {

        setUpEnvironment(
            environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
                override fun userPrivacy(): Observable<UserPrivacy> {
                    return Observable.just(
                        UserPrivacy("","some@email.com", true, true, true, true, "")
                    )
                }

                override fun updateUserEmail(
                    email: String,
                    currentPassword: String
                ): Observable<UpdateUserEmailMutation.Data> {
                    return Observable.just(
                        UpdateUserEmailMutation.Data(
                            UpdateUserEmailMutation
                                .UpdateUserAccount(
                                    "",
                                    UpdateUserEmailMutation.User("", "", email)
                                )
                        )
                    )
                }
            }).build()
        )

        this.currentEmail.assertValue("some@email.com")
        this.vm.inputs.email("rashad@gmail.com")
        this.vm.inputs.password("password")
        this.vm.inputs.updateEmailClicked()
        this.currentEmail.assertValues("some@email.com", "rashad@gmail.com")
        this.success.assertValueCount(1)
    }
}
