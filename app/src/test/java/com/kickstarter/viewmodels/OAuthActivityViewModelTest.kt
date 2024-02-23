package com.kickstarter.viewmodels

import android.content.Intent
import android.net.Uri
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.utils.CodeVerifier
import com.kickstarter.libs.utils.PKCE
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.models.User
import com.kickstarter.services.apiresponses.OAuthTokenEnvelope
import io.reactivex.Observable
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class OAuthActivityViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: OAuthViewModel

    private fun setUpEnvironment(environment: Environment, mockCodeVerifier: PKCE) {
        this.vm = OAuthViewModelFactory(environment, mockCodeVerifier).create(OAuthViewModel::class.java)
    }

    @Test
    fun testProduceState_isAuthorizationStep() = runTest {

        val testEndpoint = "testEndpoint"
        val testCodeVerifier = "testCodeVerifier"
        val testCodeChallenge = "testCodeChallenge"
        val environment = environment().toBuilder().webEndpoint(testEndpoint).build()

        val mockCodeVerifier = object : PKCE {
            override fun generateCodeChallenge(codeVerifier: String): String {
                return testCodeChallenge
            }

            override fun generateRandomCodeVerifier(entropy: Int): String {
                return testCodeVerifier
            }
        }

        setUpEnvironment(environment, mockCodeVerifier)

        val state = mutableListOf<OAuthUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.produceState(Intent())
            vm.uiState.toList(state)
        }

        val testAuthorizationUrl = "$testEndpoint/oauth/authorizations/new?redirect_uri=ksrauth2&scope=1&client_id=2QEKDK20F5LO2CEOIDZZOW8QGOM6P68AB4A5OQ44XK3N0CUW5T&response_type=1&code_challenge=$testCodeChallenge&code_challenge_method=S256"
        // - First empty emission due the initialization
        assertEquals(
            listOf(
                OAuthUiState(authorizationUrl = "", isAuthorizationStep = false, user = null),
                OAuthUiState(authorizationUrl = testAuthorizationUrl, isAuthorizationStep = true, user = null)
            ),
            state
        )
    }

    fun testProduceState_NoCode_afterRedirection() = runTest {
        setUpEnvironment(environment(), CodeVerifier())

        val state = mutableListOf<OAuthUiState>()

        val redirectionUrl = "ksrauth2://authenticate?&redirect_uri=ksrauth2&response_type=1&scope=1"

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.produceState(Intent().setData(Uri.parse(redirectionUrl)))
            vm.uiState.toList(state)
        }

        // - First empty emission due the initialization
        assertEquals(
            listOf(
                OAuthUiState(authorizationUrl = "", isAuthorizationStep = false, user = null, error = ""),
                OAuthUiState(authorizationUrl = "", isAuthorizationStep = false, user = null, error = "No code after redirection"),
            ),
            state
        )
    }

    @Test
    fun testProduceState_getTokeAndUser() = runTest {

        val user = UserFactory.user()
        val apiClient = object : MockApiClientV2() {
            override fun loginWithCodes(
                codeVerifier: String,
                code: String,
                clientId: String
            ): Observable<OAuthTokenEnvelope> {
                return Observable.just(OAuthTokenEnvelope.builder().accessToken("token").build())
            }

            override fun fetchCurrentUser(token: String): Observable<User> {
                return Observable.just(user)
            }
        }

        val currentUserV2 = MockCurrentUserV2()
        val environment = environment()
            .toBuilder()
            .apiClientV2(apiClient)
            .currentUserV2(currentUserV2)
            .build()

        setUpEnvironment(environment, CodeVerifier())

        val testCode = "1235462834129834"
        val state = mutableListOf<OAuthUiState>()
        val redirectionUrl = "ksrauth2://authenticate?code=$testCode&redirect_uri=ksrauth2&response_type=1&scope=1"

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.produceState(Intent().setData(Uri.parse(redirectionUrl)))
            vm.uiState.toList(state)
        }

        // - First empty emission due the initialization
        assertEquals(
            listOf(
                OAuthUiState(authorizationUrl = "", isAuthorizationStep = false, user = null, error = ""),
                OAuthUiState(authorizationUrl = "", isAuthorizationStep = false, user = user, error = ""),
            ),
            state
        )

        assertEquals(currentUserV2.accessToken, "token")
    }

    @Test
    fun testProduceState_getTokeAndUser_ErrorWhileFetchUser() = runTest {

        val user = UserFactory.user()
        val apiClient = object : MockApiClientV2() {
            override fun loginWithCodes(
                codeVerifier: String,
                code: String,
                clientId: String
            ): Observable<OAuthTokenEnvelope> {
                return Observable.just(OAuthTokenEnvelope.builder().accessToken("tokensito").build())
            }

            override fun fetchCurrentUser(token: String): Observable<User> {
                return Observable.error(ApiExceptionFactory.badRequestException())
            }
        }

        val currentUserV2 = MockCurrentUserV2()
        val environment = environment()
            .toBuilder()
            .apiClientV2(apiClient)
            .currentUserV2(currentUserV2)
            .build()

        setUpEnvironment(environment, CodeVerifier())

        val testCode = "1235462834129834"
        val state = mutableListOf<OAuthUiState>()
        val redirectionUrl = "ksrauth2://authenticate?code=$testCode&redirect_uri=ksrauth2&response_type=1&scope=1"

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.produceState(Intent().setData(Uri.parse(redirectionUrl)))
            vm.uiState.toList(state)
        }

        val testErrorMessage = ApiExceptionFactory.badRequestException().errorEnvelope().errorMessages().toString()

        // - First empty emission due the initialization
        assertEquals(
            listOf(
                OAuthUiState(authorizationUrl = "", isAuthorizationStep = false, user = null, error = ""),
                OAuthUiState(authorizationUrl = "", isAuthorizationStep = false, user = null, error = testErrorMessage),
            ),
            state
        )

        assertEquals(currentUserV2.accessToken, null)
    }

    fun testProduceState_getTokeAndUser_ErrorWhileToken() = runTest {

        val user = UserFactory.user()
        val apiClient = object : MockApiClientV2() {
            override fun loginWithCodes(
                codeVerifier: String,
                code: String,
                clientId: String
            ): Observable<OAuthTokenEnvelope> {
                return Observable.error(ApiExceptionFactory.badRequestException())
            }

            override fun fetchCurrentUser(token: String): Observable<User> {
                return Observable.just(user)
            }
        }

        val currentUserV2 = MockCurrentUserV2()
        val environment = environment()
            .toBuilder()
            .apiClientV2(apiClient)
            .currentUserV2(currentUserV2)
            .build()

        setUpEnvironment(environment, CodeVerifier())

        val testCode = "1235462834129834"
        val state = mutableListOf<OAuthUiState>()
        val redirectionUrl = "ksrauth2://authenticate?code=$testCode&redirect_uri=ksrauth2&response_type=1&scope=1"

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.produceState(Intent().setData(Uri.parse(redirectionUrl)))
            vm.uiState.toList(state)
        }

        val testErrorMessage = ApiExceptionFactory.badRequestException().errorEnvelope().errorMessages().toString()

        // - First empty emission due the initialization
        assertEquals(
            listOf(
                OAuthUiState(authorizationUrl = "", isAuthorizationStep = false, user = null, error = ""),
                OAuthUiState(authorizationUrl = "", isAuthorizationStep = false, user = null, error = testErrorMessage),
            ),
            state
        )

        assertEquals(currentUserV2.accessToken, null)
    }
}
