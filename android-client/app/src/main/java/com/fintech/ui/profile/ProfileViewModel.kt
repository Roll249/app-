package com.fintech.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintech.data.local.datastore.PreferencesManager
import com.fintech.domain.usecase.auth.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val userName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val isLoading: Boolean = false,
    val updateSuccess: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            preferencesManager.userInfo.collect { userInfo ->
                _state.update {
                    it.copy(
                        userName = userInfo?.name ?: "Nguyễn Văn A",
                        email = userInfo?.email ?: "khang@fintech.app",
                        phone = "0912345678",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateProfile(name: String, email: String, phone: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                kotlinx.coroutines.delay(500)
                _state.update {
                    it.copy(
                        userName = name,
                        email = email,
                        phone = phone,
                        isLoading = false,
                        updateSuccess = true
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }

    fun clearUpdateSuccess() {
        _state.update { it.copy(updateSuccess = false) }
    }
}