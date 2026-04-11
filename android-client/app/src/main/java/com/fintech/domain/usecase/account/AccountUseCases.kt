package com.fintech.domain.usecase.account

import com.fintech.data.local.db.dao.AccountDao
import com.fintech.data.local.db.entity.AccountEntity
import com.fintech.data.remote.api.services.AccountApi
import com.fintech.data.remote.model.request.CreateAccountRequest
import com.fintech.data.remote.model.response.AccountDto
import com.fintech.domain.model.Account
import com.fintech.domain.model.AccountType
import com.fintech.data.local.datastore.PreferencesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for getting all accounts
 */
class GetAccountsUseCase @Inject constructor(
    private val accountDao: AccountDao,
    private val accountApi: AccountApi,
    private val preferencesManager: PreferencesManager
) {
    operator fun invoke(): Flow<List<Account>> {
        return accountDao.getAccountsByUser("").map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getAccountsForUser(): Flow<List<Account>> {
        val userId = preferencesManager.userId.first() ?: return accountDao.getAccountsByUser("").map { emptyList() }
        return accountDao.getAccountsByUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun refresh() {
        val userId = preferencesManager.userId.first() ?: return
        try {
            val response = accountApi.getAccounts()
            if (response.isSuccessful && response.body()?.success == true) {
                val accounts: List<AccountDto>? = response.body()?.data
                if (accounts != null) {
                    accountDao.insertAll(accounts.map { dto ->
                        AccountEntity(
                            id = dto.id,
                            userId = dto.userId,
                            name = dto.name,
                            type = dto.type,
                            icon = dto.icon,
                            color = dto.color,
                            initialBalance = dto.initialBalance.toDoubleOrNull() ?: 0.0,
                            currentBalance = dto.currentBalance.toDoubleOrNull() ?: 0.0,
                            currency = dto.currency,
                            includeInTotal = dto.includeInTotal,
                            isActive = dto.isActive,
                            bankAccountId = dto.bankAccountId,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                    })
                }
            }
        } catch (e: Exception) {
            // Offline mode - use cached data
        }
    }
}

/**
 * Use case for creating an account
 */
class CreateAccountUseCase @Inject constructor(
    private val accountDao: AccountDao,
    private val accountApi: AccountApi
) {
    suspend operator fun invoke(
        name: String,
        type: String,
        icon: String? = null,
        color: String? = null,
        initialBalance: Double = 0.0,
        currency: String = "VND",
        includeInTotal: Boolean = true
    ): Result<Account> {
        return try {
            val response = accountApi.createAccount(
                CreateAccountRequest(
                    name = name,
                    type = type,
                    icon = icon,
                    color = color,
                    initialBalance = initialBalance.toString(),
                    currency = currency,
                    includeInTotal = includeInTotal
                )
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val dto = response.body()?.data!!
                val entity = AccountEntity(
                    id = dto.id,
                    userId = dto.userId,
                    name = dto.name,
                    type = dto.type,
                    icon = dto.icon,
                    color = dto.color,
                    initialBalance = dto.initialBalance.toDoubleOrNull() ?: 0.0,
                    currentBalance = dto.currentBalance.toDoubleOrNull() ?: 0.0,
                    currency = dto.currency,
                    includeInTotal = dto.includeInTotal,
                    isActive = dto.isActive,
                    bankAccountId = dto.bankAccountId,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                accountDao.insert(entity)
                Result.success(entity.toDomain())
            } else {
                Result.failure(Exception(response.body()?.message ?: "Create account failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for calculating total balance
 */
class CalculateBalanceUseCase @Inject constructor(
    private val accountDao: AccountDao,
    private val preferencesManager: PreferencesManager
) {
    suspend operator fun invoke(): Flow<Double> {
        val userId = preferencesManager.userId.first() ?: return accountDao.getTotalBalance("").map { it ?: 0.0 }
        return accountDao.getTotalBalance(userId).map { it ?: 0.0 }
    }
}

// Extension function to convert Entity to Domain
private fun AccountEntity.toDomain(): Account {
    return Account(
        id = id,
        userId = userId,
        name = name,
        type = AccountType.valueOf(type),
        icon = icon,
        color = color,
        initialBalance = initialBalance,
        currentBalance = currentBalance,
        currency = currency,
        includeInTotal = includeInTotal,
        isActive = isActive,
        bankAccountId = bankAccountId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
