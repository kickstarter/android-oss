package com.kickstarter.viewmodels

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.CodeVerifier
import com.kickstarter.libs.utils.PKCE
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.Secrets.WebEndpoint
import com.kickstarter.models.User
import com.kickstarter.services.ApiException
import com.kickstarter.viewmodels.usecases.LoginUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow
import timber.log.Timber

/**
 * UiState for the OAuthScreen.
 *  @param authorizationUrl = Url to be loaded withing the ChromeTabs with all PKCE params
 *  @param user = User object retrieve after obtaining the token
 *  @param isAuthorizationStep indicates whether or not the ChromeTabs have been loaded with authorizationUrl
 *  @param error if any error happens at any step
 */
data class OAuthUiState(
    val authorizationUrl: String = "",
    val user: User? = null,
    val isAuthorizationStep: Boolean = false,
    val error: String = ""
)
class OAuthViewModel(
    private val environment: Environment,
    private val verifier: PKCE
) : ViewModel() {

    private val logcat = "Oauth :"
    private val hostEndpoint = environment.webEndpoint()
    private val sharedPreferences = environment.sharedPreferences()
    private val loginUseCase = LoginUseCase(environment)
    private val analyticEvents = requireNotNull(environment.analytics())
    private val apiClient = requireNotNull(environment.apiClientV2())
    private val clientID = when (hostEndpoint) {
        WebEndpoint.PRODUCTION -> Secrets.Api.Client.PRODUCTION
        WebEndpoint.STAGING -> Secrets.Api.Client.STAGING
        else -> ""
    }

    private var mutableUIState = MutableStateFlow(OAuthUiState())
    private var codeVerifier: String?
        get() {
            return sharedPreferences?.getString("KSCodeVerifier", "")
        }
        set(value) {
            if (value == null) sharedPreferences?.edit()?.remove("KSCodeVerifier")?.apply()
            else sharedPreferences?.edit()
                ?.putString("KSCodeVerifier", value)
                ?.apply()
        }
    val uiState: StateFlow<OAuthUiState>
        get() = mutableUIState.asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = OAuthUiState()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    fun produceState(intent: Intent, uri: Uri? = null) {
        viewModelScope.launch {
            uri?.let {
                val code = uri.getQueryParameter("code")
                if (isAfterRedirectionStep(uri)) {
                    Timber.d("$logcat retrieve token after redirectionDeeplink: $code")
                    apiClient.loginWithCodes(
                        requireNotNull(codeVerifier),
                        requireNotNull(code),
                        clientID
                    )
                        .asFlow()
                        .flatMapLatest { token ->
                            Timber.d("$logcat About to persist token to currentUser: $token")
                            loginUseCase.setToken(token.accessToken())
                            apiClient.fetchCurrentUser()
                                .asFlow()
                                .map {
                                    it
                                }
                        }
                        .catch {
                            Timber.e(
                                "$logcat error while getting the token or user: ${
                                processThrowable(
                                    it
                                )
                                }"
                            )
                            mutableUIState.emit(
                                OAuthUiState(
                                    error = processThrowable(it),
                                    user = null
                                )
                            )
                            loginUseCase.logout()
                            codeVerifier = null
                        }
                        .collect { user ->
                            Timber.d("$logcat About to persist user to currentUser: $user")
                            loginUseCase.setUser(user)
                            mutableUIState.emit(
                                OAuthUiState(
                                    user = user,
                                )
                            )
                            analyticEvents.trackLogInButtonCtaClicked()
                            codeVerifier = null
                        }
                } else {
                    mutableUIState.emit(
                        OAuthUiState(
                            error = "$code / $codeVerifier empty or null or wrong redirection",
                            user = null
                        )
                    )
                    codeVerifier = null
                }
            }

            if (intent.data == null && uri == null) {
                codeVerifier = null
                codeVerifier = verifier.generateRandomCodeVerifier(entropy = CodeVerifier.MIN_CODE_VERIFIER_ENTROPY)
                codeVerifier?.let {
                    val url = generateAuthorizationUrlWithParams(it)
                    Timber.d("$logcat isAuthorizationStep $url and codeVerifier: $codeVerifier")
                    mutableUIState.emit(
                        OAuthUiState(
                            authorizationUrl = url,
                            isAuthorizationStep = true,
                        )
                    )
                }
            }
        }
    }

    private fun processThrowable(throwable: Throwable): String {
        if (!throwable.message.isNullOrBlank()) return throwable.message ?: ""

        if (throwable is ApiException) {
            val apiError = throwable.errorEnvelope()?.errorMessages()?.toString() ?: ""
            val genericError = throwable.response().message()
            return "$genericError / $apiError"
        }

        return "error while getting the token or user"
    }

    private fun generateAuthorizationUrlWithParams(verifier: String): String {
        val authParams = mapOf(
            "redirect_uri" to REDIRECT_URI_SCHEMA,
            "scope" to "1", // profile/email
            "client_id" to clientID,
            "response_type" to "1", // code
            "code_challenge" to this.verifier.generateCodeChallenge(verifier),
            "code_challenge_method" to "S256"
        ).map { (k, v) -> "${(k)}=$v" }.joinToString("&")
        return "$hostEndpoint/oauth/authorizations/new?$authParams"
    }

    companion object {
        const val REDIRECT_URI_SCHEMA = "ksrauth2"
        const val REDIRECT_URI_HOST = "authenticate"
        fun isAfterRedirectionStep(uri: Uri): Boolean {
            val scheme = uri.scheme
            val host = uri.host
            val code = uri.getQueryParameter("code")
            return scheme == REDIRECT_URI_SCHEMA && host == REDIRECT_URI_HOST && !code.isNullOrBlank()
        }
    }
}

class OAuthViewModelFactory(
    private val environment: Environment,
    private val verifier: PKCE = CodeVerifier()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return OAuthViewModel(environment, verifier = verifier) as T
    }
}
