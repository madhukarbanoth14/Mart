package com.mart.distribution.demo.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mart.distribution.demo.data.api.MartApi
import com.mart.distribution.demo.data.api.dto.RegistrationAreaDto
import com.mart.distribution.demo.data.api.dto.RegistrationStateDto
import com.mart.distribution.demo.data.api.dto.SelfRegisterRequest
import com.mart.distribution.demo.data.api.dto.SendOtpRequest
import com.mart.distribution.demo.data.api.dto.VerifyOtpRequest
import com.mart.distribution.demo.data.session.SessionManager
import com.mart.distribution.demo.data.session.SessionUser
import com.mart.distribution.demo.util.ApiErrorMessages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RegisterAuthMethod {
    MOBILE,
    EMAIL,
}

data class RegisterUiState(
    val step: Int = 0,
    val authMethod: RegisterAuthMethod = RegisterAuthMethod.MOBILE,
    val role: String = "SHOPKEEPER",
    val phone: String = "",
    val otp: String = "",
    val verificationToken: String? = null,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val shopName: String = "",
    val state: String = "",
    val district: String = "",
    val areaId: String = "",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val referralCode: String = "",
    val geo: List<RegistrationStateDto> = emptyList(),
    val areas: List<RegistrationAreaDto> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val otpSent: Boolean = false,
    val devOtp: String? = null,
) {
    val authComplete: Boolean
        get() =
            when (authMethod) {
                RegisterAuthMethod.MOBILE -> !verificationToken.isNullOrBlank()
                RegisterAuthMethod.EMAIL ->
                    step >= 2 && email.contains('@') && password.length >= 8
            }
}

class RegisterViewModel(
    private val martApi: MartApi,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _ui = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _ui.asStateFlow()

    fun setRole(role: String) {
        _ui.update { it.copy(role = role) }
    }

    fun setAuthMethod(method: RegisterAuthMethod) {
        _ui.update {
            it.copy(
                authMethod = method,
                phone = if (method == RegisterAuthMethod.EMAIL) "" else it.phone,
                otp = "",
                otpSent = false,
                devOtp = null,
                verificationToken = null,
                email = if (method == RegisterAuthMethod.MOBILE) "" else it.email,
                password = if (method == RegisterAuthMethod.MOBILE) "" else it.password,
                error = null,
            )
        }
    }

    fun update(block: (RegisterUiState) -> RegisterUiState) {
        _ui.update(block)
    }

    fun loadGeo() {
        viewModelScope.launch {
            try {
                val geo = martApi.registrationGeo().states
                _ui.update {
                    it.copy(
                        geo = geo,
                        state = geo.firstOrNull()?.name ?: it.state,
                        district = geo.firstOrNull()?.districts?.firstOrNull() ?: it.district,
                    )
                }
                loadAreas()
            } catch (e: Exception) {
                _ui.update { it.copy(error = ApiErrorMessages.fromThrowable(e, "Could not load locations")) }
            }
        }
    }

    fun loadAreas() {
        val s = _ui.value
        viewModelScope.launch {
            try {
                val areas = martApi.registrationAreas(s.state.ifBlank { null }, s.district.ifBlank { null })
                _ui.update {
                    it.copy(
                        areas = areas,
                        areaId = areas.firstOrNull()?.id.orEmpty(),
                    )
                }
            } catch (e: Exception) {
                _ui.update { it.copy(error = ApiErrorMessages.fromThrowable(e, "Could not load areas")) }
            }
        }
    }

    fun sendOtp() {
        val phone = _ui.value.phone.trim()
        if (phone.length < 10) {
            _ui.update { it.copy(error = "Enter a valid mobile number") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            try {
                val res = martApi.sendOtp(SendOtpRequest(phone))
                _ui.update {
                    it.copy(
                        loading = false,
                        otpSent = true,
                        devOtp = res.devOtp,
                        error = null,
                    )
                }
            } catch (e: Exception) {
                _ui.update {
                    it.copy(loading = false, error = ApiErrorMessages.fromThrowable(e, "Could not send OTP"))
                }
            }
        }
    }

    fun verifyOtp(onVerified: () -> Unit) {
        val s = _ui.value
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            try {
                val res = martApi.verifyOtp(VerifyOtpRequest(s.phone.trim(), s.otp.trim()))
                _ui.update {
                    it.copy(
                        loading = false,
                        verificationToken = res.verificationToken,
                        step = 2,
                    )
                }
                onVerified()
            } catch (e: Exception) {
                _ui.update {
                    it.copy(loading = false, error = ApiErrorMessages.fromThrowable(e, "Invalid OTP"))
                }
            }
        }
    }

    fun continueWithEmailAuth() {
        val s = _ui.value
        val email = s.email.trim()
        when {
            !email.contains('@') ->
                _ui.update { it.copy(error = "Enter a valid email address") }
            s.password.length < 8 ->
                _ui.update { it.copy(error = "Password must be at least 8 characters") }
            else ->
                _ui.update { it.copy(step = 2, error = null) }
        }
    }

    fun submitRegistration(onSuccess: () -> Unit) {
        val s = _ui.value
        val isMobile = s.authMethod == RegisterAuthMethod.MOBILE
        val token = s.verificationToken
        if (isMobile && token.isNullOrBlank()) {
            _ui.update { it.copy(error = "Verify mobile OTP first") }
            return
        }
        if (!isMobile && (!s.email.contains('@') || s.password.length < 8)) {
            _ui.update { it.copy(error = "Enter email and password (min 8 characters)") }
            return
        }
        validateRegistrationFields(s)?.let { message ->
            _ui.update { it.copy(error = message) }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            try {
                val body =
                    SelfRegisterRequest(
                        verificationToken = if (isMobile) token else null,
                        phone = if (isMobile) s.phone.trim() else null,
                        name = s.name.trim(),
                        email = if (isMobile) s.email.trim().ifBlank { null } else s.email.trim(),
                        password = if (isMobile) s.password.ifBlank { null } else s.password,
                        areaId = s.areaId,
                        state = s.state.trim(),
                        district = s.district.trim(),
                        address = s.address.trim(),
                        shopName = s.shopName.trim(),
                        latitude = s.latitude,
                        longitude = s.longitude,
                        referralCode = s.referralCode.trim().ifBlank { null },
                    )
                val res =
                    if (s.role.equals("DEALER", true)) {
                        martApi.registerDealer(body)
                    } else {
                        martApi.registerShopkeeper(body)
                    }
                sessionManager.completeApiLogin(
                    res.accessToken,
                    SessionUser(
                        id = res.user.id,
                        name = res.user.name,
                        email = res.user.email,
                        role = res.user.role,
                        companyId = res.user.companyId,
                        canPlaceOrders = false,
                        documentStatus = "NOT_UPLOADED",
                    ),
                )
                _ui.update { it.copy(loading = false) }
                onSuccess()
            } catch (e: Exception) {
                _ui.update {
                    it.copy(loading = false, error = ApiErrorMessages.fromThrowable(e, "Registration failed"))
                }
            }
        }
    }

    fun validateWizardStep(wizardStep: Int): String? {
        val s = _ui.value
        return when (wizardStep) {
            0 ->
                when {
                    s.name.isBlank() -> "Enter owner name"
                    s.shopName.isBlank() -> "Enter shop / business name"
                    else -> null
                }
            1 ->
                when {
                    s.areas.isEmpty() -> "No service areas available. Try another district or contact support."
                    s.areaId.isBlank() -> "Select a service area / route"
                    else -> null
                }
            2 ->
                if (s.address.isBlank()) "Enter your shop address" else null
            else -> null
        }
    }

    private fun validateRegistrationFields(s: RegisterUiState): String? {
        if (s.name.isBlank() || s.shopName.isBlank()) {
            return "Complete business details"
        }
        if (s.areaId.isBlank()) {
            return "Select a service area / route"
        }
        if (s.address.isBlank()) {
            return "Enter your shop address"
        }
        return null
    }
}
