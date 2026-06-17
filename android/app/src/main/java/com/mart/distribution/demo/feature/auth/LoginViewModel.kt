package com.mart.distribution.demo.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mart.distribution.demo.BuildConfig
import com.mart.distribution.demo.data.api.MartApi
import com.mart.distribution.demo.data.api.dto.ForgotPasswordRequest
import com.mart.distribution.demo.data.api.dto.ForgotPasswordResponse
import com.mart.distribution.demo.data.api.dto.LoginRequest
import com.mart.distribution.demo.data.api.dto.ResetPasswordRequest
import com.mart.distribution.demo.data.demo.LocalDemoAuthConfig
import com.mart.distribution.demo.data.demo.LocalDemoMartStore
import com.mart.distribution.demo.data.network.NetworkConfigRepository
import com.mart.distribution.demo.data.session.SessionManager
import com.mart.distribution.demo.data.session.SessionRepository
import com.mart.distribution.demo.data.session.SessionUser
import com.mart.distribution.demo.util.FieldValidators
import com.mart.distribution.demo.util.isProbablyEmulator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    /** Shown on physical devices when using live API (not offline demo). */
    val serverUrl: String = "",
    val loading: Boolean = false,
    val error: String? = null,
)

class LoginViewModel(
    private val sessionRepository: SessionRepository,
    private val sessionManager: SessionManager,
    private val martApi: MartApi,
    private val localDemoMartStore: LocalDemoMartStore,
    private val networkConfigRepository: NetworkConfigRepository,
) : ViewModel() {
    private val _ui = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.hydrate()
            networkConfigRepository.hydrate()
            _ui.value =
                _ui.value.copy(serverUrl = networkConfigRepository.displayUrlForLoginField())
        }
    }

    fun setIdentifier(v: String) {
        _ui.value = _ui.value.copy(identifier = v, error = null)
    }

    fun setPassword(v: String) {
        _ui.value = _ui.value.copy(password = v, error = null)
    }

    fun setServerUrl(v: String) {
        _ui.value = _ui.value.copy(serverUrl = v, error = null)
    }

    private fun isNetworkError(e: Exception): Boolean {
        if (
            e is UnknownHostException ||
            e is ConnectException ||
            e is SocketTimeoutException ||
            e is IOException
        ) {
            return true
        }
        val msg = (e.message ?: "").lowercase()
        return msg.contains("unable to resolve host") ||
            msg.contains("failed to connect") ||
            msg.contains("timeout") ||
            msg.contains("network is unreachable") ||
            msg.contains("no address associated")
    }

    private fun tryOfflineDemoLogin(
        identifier: String,
        password: String,
    ): SessionUser? =
        LocalDemoAuthConfig.resolveDemoUser(identifier.trim(), password)
            ?: localDemoMartStore.tryResolveOnboardedSession(identifier.trim(), password)

    private suspend fun completeOfflineDemoLogin(
        demoUser: SessionUser,
        onSuccess: () -> Unit,
    ) {
        localDemoMartStore.resetForNewSession()
        sessionRepository.saveLocalDemoSession(demoUser)
        _ui.value = _ui.value.copy(loading = false, error = null)
        onSuccess()
    }

    /** Manual fallback when Cloud Run / LAN backend is unreachable. */
    fun loginOfflineDemo(onSuccess: () -> Unit) {
        val s = _ui.value
        if (s.identifier.isBlank() || s.password.isBlank()) {
            _ui.value = s.copy(error = "Enter mobile/email and password")
            return
        }
        viewModelScope.launch {
            _ui.value = s.copy(loading = true, error = null)
            val demoUser = tryOfflineDemoLogin(s.identifier, s.password)
            if (demoUser == null) {
                val statusMsg = localDemoMartStore.loginStatusMessage(s.identifier.trim())
                _ui.value =
                    _ui.value.copy(
                        loading = false,
                        error =
                            statusMsg
                                ?: "Offline demo login failed. Use a seed account and password " +
                                    LocalDemoAuthConfig.DEMO_PASSWORD,
                    )
                return@launch
            }
            completeOfflineDemoLogin(demoUser, onSuccess)
        }
    }

    fun login(onSuccess: () -> Unit) {
        val s = _ui.value
        FieldValidators.loginIdentifier(s.identifier)?.let { msg ->
            _ui.value = s.copy(error = msg)
            return
        }
        if (s.password.isBlank()) {
            _ui.value = s.copy(error = "Enter password")
            return
        }
        viewModelScope.launch {
            _ui.value = s.copy(loading = true, error = null)
            try {
                if (BuildConfig.USE_LOCAL_DEMO_AUTH) {
                    val demoUser =
                        LocalDemoAuthConfig.resolveDemoUser(s.identifier.trim(), s.password)
                            ?: localDemoMartStore.tryResolveOnboardedSession(
                                s.identifier.trim(),
                                s.password,
                            )
                    if (demoUser == null) {
                        val statusMsg = localDemoMartStore.loginStatusMessage(s.identifier.trim())
                        _ui.value =
                            _ui.value.copy(
                                loading = false,
                                error =
                                    statusMsg
                                        ?: "Invalid mobile/email or password. Demo password is ${LocalDemoAuthConfig.DEMO_PASSWORD}.",
                            )
                        return@launch
                    }
                    localDemoMartStore.resetForNewSession()
                    sessionRepository.saveLocalDemoSession(demoUser)
                    _ui.value = _ui.value.copy(loading = false)
                    onSuccess()
                    return@launch
                }

                if (!isProbablyEmulator()) {
                    if (s.serverUrl.isBlank() &&
                        BuildConfig.API_BASE_URL.contains("10.0.2.2", ignoreCase = true)
                    ) {
                        _ui.value =
                            _ui.value.copy(
                                loading = false,
                                error =
                                    "On a real phone, enter your backend URL " +
                                        "(e.g. http://192.168.1.10:3000 on the same Wi-Fi as the server).",
                            )
                        return@launch
                    }
                    if (s.serverUrl.isNotBlank() && s.serverUrl.trim().toHttpUrlOrNull() == null) {
                        _ui.value =
                            _ui.value.copy(
                                loading = false,
                                error = "Invalid backend URL. Example: http://192.168.1.10:3000",
                            )
                        return@launch
                    }
                    networkConfigRepository.setOverrideFromUserInput(s.serverUrl.trim())
                }

                val res = martApi.login(LoginRequest(s.identifier.trim(), s.password))
                val user =
                    SessionUser(
                        id = res.user.id,
                        name = res.user.name,
                        email = res.user.email,
                        role = res.user.role,
                        companyId = res.user.companyId,
                    )
                sessionRepository.saveSession(res.accessToken, user)
                _ui.value = _ui.value.copy(loading = false)
                onSuccess()
                viewModelScope.launch {
                    sessionManager.refreshProfileFromServerIfNeeded()
                }
            } catch (e: Exception) {
                if (BuildConfig.USE_LOCAL_DEMO_AUTH && isNetworkError(e)) {
                    val demoUser = tryOfflineDemoLogin(s.identifier, s.password)
                    if (demoUser != null) {
                        completeOfflineDemoLogin(demoUser, onSuccess)
                        return@launch
                    }
                }
                _ui.value =
                    _ui.value.copy(
                        loading = false,
                        error = e.message ?: "Login failed",
                    )
            }
        }
    }

    fun requestPasswordReset(
        email: String,
        onResult: (ForgotPasswordResponse?) -> Unit,
    ) {
        FieldValidators.email(email)?.let { msg ->
            _ui.value = _ui.value.copy(error = msg)
            onResult(null)
            return
        }
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            try {
                if (BuildConfig.USE_LOCAL_DEMO_AUTH) {
                    _ui.value = _ui.value.copy(loading = false)
                    onResult(
                        ForgotPasswordResponse(
                            message = "Demo mode: use demo password or token from onboard screen.",
                            loginEmail = email.trim(),
                            resetPasswordToken = "demo-reset-token",
                        ),
                    )
                    return@launch
                }
                val resp = martApi.forgotPassword(ForgotPasswordRequest(email.trim()))
                _ui.value = _ui.value.copy(loading = false)
                onResult(resp)
            } catch (e: Exception) {
                _ui.value =
                    _ui.value.copy(
                        loading = false,
                        error = e.message ?: "Could not request reset link",
                    )
                onResult(null)
            }
        }
    }

    fun resetPassword(
        token: String,
        newPassword: String,
        onSuccess: () -> Unit,
    ) {
        FieldValidators.resetToken(token)?.let { msg ->
            _ui.value = _ui.value.copy(error = msg)
            return
        }
        FieldValidators.password(newPassword)?.let { msg ->
            _ui.value = _ui.value.copy(error = msg)
            return
        }
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            try {
                if (BuildConfig.USE_LOCAL_DEMO_AUTH) {
                    _ui.value = _ui.value.copy(loading = false, error = null)
                    onSuccess()
                    return@launch
                }
                martApi.resetPassword(ResetPasswordRequest(token, newPassword))
                _ui.value = _ui.value.copy(loading = false, password = "")
                onSuccess()
            } catch (e: Exception) {
                _ui.value =
                    _ui.value.copy(
                        loading = false,
                        error = e.message ?: "Could not reset password",
                    )
            }
        }
    }
}
