package com.fintech.domain.usecase.bank

import com.fintech.data.local.db.dao.UserBankAccountDao
import com.fintech.data.local.db.entity.UserBankAccountEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class GetLinkedBanksUseCase @Inject constructor(
    private val userBankAccountDao: UserBankAccountDao
) {
    operator fun invoke(userId: String): Flow<List<UserBankAccountEntity>> {
        return userBankAccountDao.getLinkedBanks(userId)
    }
}

class GetLinkedBankByIdUseCase @Inject constructor(
    private val userBankAccountDao: UserBankAccountDao
) {
    suspend operator fun invoke(id: String): UserBankAccountEntity? {
        return userBankAccountDao.getById(id)
    }
}

class LinkBankAccountUseCase @Inject constructor(
    private val userBankAccountDao: UserBankAccountDao
) {
    sealed class Result {
        data class Success(val bankAccount: UserBankAccountEntity) : Result()
        data class AlreadyLinked(val existingBank: UserBankAccountEntity) : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(
        userId: String,
        bankId: String,
        bankName: String,
        bankCode: String,
        bankLogoUrl: String?,
        accountNumber: String,
        accountHolderName: String,
        initialBalance: Double = 0.0
    ): Result {
        return try {
            val existing = userBankAccountDao.findByAccountAndBank(userId, accountNumber, bankId)
            if (existing != null) {
                return Result.AlreadyLinked(existing)
            }

            val now = System.currentTimeMillis()
            val bankAccount = UserBankAccountEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                bankId = bankId,
                bankName = bankName,
                bankCode = bankCode,
                bankLogoUrl = bankLogoUrl,
                accountNumber = accountNumber,
                accountHolderName = accountHolderName,
                balance = initialBalance,
                isActive = true,
                linkedAt = now,
                updatedAt = now,
                isSynced = false
            )

            userBankAccountDao.insert(bankAccount)
            Result.Success(bankAccount)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi khi liên kết ngân hàng")
        }
    }
}

class UnlinkBankAccountUseCase @Inject constructor(
    private val userBankAccountDao: UserBankAccountDao
) {
    sealed class Result {
        object Success : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(bankAccountId: String): Result {
        return try {
            userBankAccountDao.deactivate(bankAccountId, System.currentTimeMillis())
            Result.Success
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi khi hủy liên kết")
        }
    }
}

class UpdateBankBalanceUseCase @Inject constructor(
    private val userBankAccountDao: UserBankAccountDao
) {
    sealed class Result {
        object Success : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(bankAccountId: String, newBalance: Double): Result {
        return try {
            userBankAccountDao.updateBalance(bankAccountId, newBalance, System.currentTimeMillis())
            Result.Success
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi khi cập nhật số dư")
        }
    }
}

class GetTotalLinkedBankBalanceUseCase @Inject constructor(
    private val userBankAccountDao: UserBankAccountDao
) {
    operator fun invoke(userId: String): Flow<Double> {
        return userBankAccountDao.getTotalBalance(userId).map { it ?: 0.0 }
    }
}
