package com.mart.distribution.demo.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mart.distribution.demo.data.api.MartApi
import com.mart.distribution.demo.data.api.dto.LoginRequest
import com.mart.distribution.demo.data.session.SessionRepository
import com.mart.distribution.demo.data.session.SessionUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
)

class LoginViewModel(
    private val sessionRepository: SessionRepository,
    private val martApi: MartApi,
) : ViewModel() {
    private val _ui = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _ui.asStateFlow()

    fun setEmail(v: String) {
        _ui.value = _ui.value.copy(email = v, error = null)
    }

    fun setPassword(v: String) {
        _ui.value = _ui.value.copy(password = v, error = null)
    }

    fun login(onSuccess: () -> Unit) {
        val s = _ui.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _ui.value = s.copy(error = "Enter email and password")
            return
        }
        viewModelScope.launch {
            _ui.value = s.copy(loading = true, error = null)
            try {
                val res = martApi.login(LoginRequest(s.email.trim(), s.password))
                val user =
                    SessionUser(
                        id = res.user.id,
                        name = res.user.name,
                        email = res.user.email,
                        role = res.user.role,
                    )
                sessionRepository.saveSession(res.accessToken, user)
                _ui.value = _ui.value.copy(loading = false)
                onSuccess()
            } catch (e: Exception) {
                _ui.value =
                    _ui.value.copy(
                        loading = false,
                        error = e.message ?: "Login failed",
                    )
            }
        }
    }
}
