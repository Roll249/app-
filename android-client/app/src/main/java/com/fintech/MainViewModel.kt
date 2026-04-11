package com.fintech

import androidx.lifecycle.ViewModel
import com.fintech.domain.usecase.auth.IsLoggedInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    isLoggedInUseCase: IsLoggedInUseCase
) : ViewModel() {
    val isLoggedIn: Flow<Boolean> = isLoggedInUseCase()
}
