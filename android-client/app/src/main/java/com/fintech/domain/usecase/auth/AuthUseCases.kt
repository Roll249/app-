package com.fintech.domain.usecase.auth

import com.fintech.data.local.datastore.PreferencesManager
import com.fintech.data.remote.api.services.AuthApi
import com.fintech.data.remote.model.request.LoginRequest
import com.fintech.data.remote.model.request.RefreshTokenRequest
import com.fintech.data.remote.model.request.RegisterRequest
import com.fintech.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for user login
 */
class LoginUseCase @Inject constructor(
    private val authApi: AuthApi,
    private val preferencesManager: PreferencesManager
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return try {
            val response = authApi.login(
                LoginRequest(
                    email = email,
                    password = password
                )
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val authData = response.body()?.data!!
                val userData = authData.user

                // Save tokens
                preferencesManager.saveAuthTokens(authData.accessToken, authData.refreshToken)
                preferencesManager.saveUserInfo(
                    userId = userData.id,
                    email = userData.email,
                    name = userData.fullName
                )

                Result.success(
                    User(
                        id = userData.id,
                        email = userData.email,
                        fullName = userData.fullName,
                        avatarUrl = userData.avatarUrl,
                        phone = userData.phone,
                        isVerified = userData.isVerified,
                        createdAt = System.currentTimeMillis()
                    )
                )
            } else {
                Result.failure(Exception(response.body()?.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for user registration
 */
class RegisterUseCase @Inject constructor(
    private val authApi: AuthApi,
    private val preferencesManager: PreferencesManager
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        fullName: String?,
        phone: String?
    ): Result<User> {
        return try {
            val response = authApi.register(
                RegisterRequest(
                    email = email,
                    password = password,
                    fullName = fullName,
                    phone = phone
                )
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val authData = response.body()?.data!!
                val userData = authData.user

                // Save tokens
                preferencesManager.saveAuthTokens(authData.accessToken, authData.refreshToken)
                preferencesManager.saveUserInfo(
                    userId = userData.id,
                    email = userData.email,
                    name = userData.fullName
                )

                Result.success(
                    User(
                        id = userData.id,
                        email = userData.email,
                        fullName = userData.fullName,
                        avatarUrl = userData.avatarUrl,
                        phone = userData.phone,
                        isVerified = userData.isVerified,
                        createdAt = System.currentTimeMillis()
                    )
                )
            } else {
                Result.failure(Exception(response.body()?.message ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for refreshing token
 */
class RefreshTokenUseCase @Inject constructor(
    private val authApi: AuthApi,
    private val preferencesManager: PreferencesManager
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            val refreshToken = preferencesManager.refreshToken.first()
            if (refreshToken == null) {
                return Result.failure(Exception("No refresh token"))
            }

            val response = authApi.refreshToken(
                RefreshTokenRequest(refreshToken = refreshToken)
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val authData = response.body()?.data!!
                preferencesManager.saveAuthTokens(authData.accessToken, authData.refreshToken)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Token refresh failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for logout
 */
class LogoutUseCase @Inject constructor(
    private val authApi: AuthApi,
    private val preferencesManager: PreferencesManager
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            val refreshToken = preferencesManager.refreshToken.first()
            if (refreshToken != null) {
                try {
                    authApi.logout(RefreshTokenRequest(refreshToken = refreshToken))
                } catch (e: Exception) {
                    // Ignore API errors on logout
                }
            }
            preferencesManager.clearAuthData()
            Result.success(Unit)
        } catch (e: Exception) {
            preferencesManager.clearAuthData()
            Result.success(Unit)
        }
    }
}

/**
 * Use case for checking if user is logged in
 */
class IsLoggedInUseCase @Inject constructor(
    private val preferencesManager: PreferencesManager
) {
    operator fun invoke(): Flow<Boolean> = preferencesManager.isLoggedIn
}
