package com.fintech.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintech.data.local.datastore.PreferencesManager
import com.fintech.data.remote.api.services.AuthApi
import com.fintech.data.remote.model.request.LoginRequest
import com.fintech.data.remote.model.request.RegisterRequest
import com.fintech.domain.usecase.auth.LoginUseCase
import com.fintech.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val authApi: AuthApi,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            loginUseCase(email, password)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isLoggedIn = true) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Login failed"
                        )
                    }
                }
        }
    }

    fun register(email: String, password: String, fullName: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            registerUseCase(email, password, fullName, null)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isLoggedIn = true) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Registration failed"
                        )
                    }
                }
        }
    }

    fun demoLogin(scenario: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val response = authApi.register(
                    RegisterRequest(
                        email = "demo_${scenario}_${System.currentTimeMillis()}@fintech.demo",
                        password = "Demo123!@#",
                        fullName = when (scenario) {
                            "office_worker" -> "Nguyễn Văn Minh"
                            "freelancer" -> "Trần Thị Lan"
                            "tech_lead" -> "Lê Hoàng Nam"
                            else -> "Demo User"
                        }
                    )
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    data?.accessToken?.let { token ->
                        preferencesManager.saveAuthTokens(token, data.refreshToken ?: token)
                    }
                    _state.update { it.copy(isLoading = false, isLoggedIn = true) }
                } else {
                    val loginRes = authApi.login(
                        LoginRequest(
                            email = "demo_${scenario}@fintech.demo",
                            password = "Demo123!@#"
                        )
                    )
                    if (loginRes.isSuccessful && loginRes.body()?.success == true) {
                        val data = loginRes.body()?.data
                        data?.accessToken?.let { token ->
                            preferencesManager.saveAuthTokens(token, data.refreshToken ?: token)
                        }
                        _state.update { it.copy(isLoading = false, isLoggedIn = true) }
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = response.body()?.message ?: "Demo login failed"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Không thể kết nối server: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
