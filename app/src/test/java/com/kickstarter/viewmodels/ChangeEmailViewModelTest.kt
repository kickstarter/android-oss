package com.kickstarter.viewmodels

import SendEmailVerificationMutation
import UpdateUserEmailMutation
import UserPrivacyQuery
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KoalaEvent
import com.kickstarter.mock.services.MockApolloClient
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class ChangeEmailViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ChangeEmailViewModel.ViewModel

    private val currentEmail = TestSubscriber<String>()
    private val emailErrorIsVisible = TestSubscriber<Boolean>()
    private val error = TestSubscriber<String>()
    private val sendVerificationIsHidden = TestSubscriber<Boolean>()
    private val progressBarIsVisible = TestSubscriber<Boolean>()
    private val saveButtonIsEnabled = TestSubscriber<Boolean>()
    private val success = TestSubscriber<Void>()
    private val warningText = TestSubscriber<Int>()
    private val warningTextColor = TestSubscriber<Int>()
    private val verificationButtonText = TestSubscriber<Int>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ChangeEmailViewModel.ViewModel(environment)

        this.vm.outputs.currentEmail().subscribe(this.currentEmail)
        this.vm.outputs.emailErrorIsVisible().subscribe(this.emailErrorIsVisible)
        this.vm.outputs.error().subscribe(this.error)
        this.vm.outputs.sendVerificationIsHidden().subscribe(this.sendVerificationIsHidden)
        this.vm.outputs.progressBarIsVisible().subscribe(this.progressBarIsVisible)
        this.vm.outputs.saveButtonIsEnabled().subscribe(this.saveButtonIsEnabled)
        this.vm.outputs.success().subscribe(this.success)
        this.vm.outputs.warningText().subscribe(this.warningText)
        this.vm.outputs.warningTextColor().subscribe(this.warningTextColor)
        this.vm.outputs.verificationEmailButtonText().subscribe(this.verificationButtonText)
    }

    @Test
    fun testCurrentEmail() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "",
                        "rashad@test.com", true, true, true, ""
                )))
            }
        }).build())

        this.currentEmail.assertValue("rashad@test.com")
        this.sendVerificationIsHidden.assertValue(true)
        this.koalaTest.assertValue(KoalaEvent.VIEWED_CHANGE_EMAIL)
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
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data> {
                return Observable.error(Throwable("boop"))
            }
        }).build())

        this.vm.inputs.email("test@email.com")
        this.vm.inputs.password("password")
        this.vm.inputs.updateEmailClicked()
        this.error.assertValue("boop")
    }

    @Test
    fun testIsEmailUnverified() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "",
                        "rashad@test.com", true, true, false, ""
                )))
            }
        }).build())

        this.currentEmail.assertValue("rashad@test.com")
        this.sendVerificationIsHidden.assertValue(false)

        this.warningText.assertValue(R.string.Email_unverified)
        this.warningTextColor.assertValue(R.color.ksr_dark_grey_400)
    }

    @Test
    fun testIsEmailUndeliverable() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "",
                        "rashad@test.com", true, false, false, ""
                )))
            }
        }).build())

        this.currentEmail.assertValue("rashad@test.com")
        this.sendVerificationIsHidden.assertValue(false)

        this.warningText.assertValue(R.string.We_ve_been_unable_to_send_email)
        this.warningTextColor.assertValue(R.color.ksr_red_400)
    }

    @Test
    fun testIsUserABacker() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "",
                        "rashad@test.com", false, true, true, ""
                )))
            }
        }).build())

        this.currentEmail.assertValue("rashad@test.com")
        this.sendVerificationIsHidden.assertValue(true)

        this.warningText.assertValue(null)
        this.warningTextColor.assertValue(R.color.ksr_dark_grey_400)
        this.verificationButtonText.assertValue(R.string.Send_verfication_email)
    }

    @Test
    fun testIsUserACreator() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.just(UserPrivacyQuery.Data(UserPrivacyQuery.Me("", "",
                        "rashad@test.com", true, true, true, ""
                )))
            }
        }).build())

        this.currentEmail.assertValue("rashad@test.com")
        this.sendVerificationIsHidden.assertValue(true)

        this.warningText.assertValue(null)
        this.warningTextColor.assertValue(R.color.ksr_dark_grey_400)
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
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data> {
                return Observable.just(SendEmailVerificationMutation.Data(SendEmailVerificationMutation
                        .UserSendEmailVerification("", "1234")))
            }
        }).build())

        this.vm.inputs.sendVerificationEmail()
        this.success.assertValueCount(1)
        this.koalaTest.assertValues(KoalaEvent.VIEWED_CHANGE_EMAIL, KoalaEvent.RESENT_VERIFICATION_EMAIL)
    }

    @Test
    fun testSendVerificationEmailError() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun sendVerificationEmail(): Observable<SendEmailVerificationMutation.Data> {
                return Observable.error(Throwable("error"))
            }
        }).build())

        this.vm.inputs.sendVerificationEmail()
        this.error.assertValue("error")
    }

    @Test
    fun testSuccess() {
        setUpEnvironment(environment().toBuilder().apolloClient(object : MockApolloClient() {
            override fun updateUserEmail(email: String, currentPassword: String): Observable<UpdateUserEmailMutation.Data> {
                return Observable.just(UpdateUserEmailMutation.Data(UpdateUserEmailMutation
                        .UpdateUserAccount("", UpdateUserEmailMutation.User("", "", email))))
            }
        }).build())

        this.currentEmail.assertValue("some@email.com")
        this.vm.inputs.email("rashad@gmail.com")
        this.vm.inputs.password("password")
        this.vm.inputs.updateEmailClicked()
        this.currentEmail.assertValues("some@email.com", "rashad@gmail.com")
        this.success.assertValueCount(1)
        this.koalaTest.assertValues(KoalaEvent.VIEWED_CHANGE_EMAIL, KoalaEvent.CHANGED_EMAIL)
    }

}
