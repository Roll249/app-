package com.fintech.domain.usecase.transaction

import com.fintech.data.local.db.dao.TransactionDao
import com.fintech.data.local.db.entity.TransactionEntity
import com.fintech.data.remote.api.services.TransactionApi
import com.fintech.data.remote.model.request.CreateTransactionRequest
import com.fintech.data.remote.model.response.TransactionDto
import com.fintech.domain.model.Transaction
import com.fintech.domain.model.TransactionType
import com.fintech.data.local.datastore.PreferencesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

/**
 * Use case for getting transactions
 */
class GetTransactionsUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val transactionApi: TransactionApi,
    private val preferencesManager: PreferencesManager
) {
    operator fun invoke(
        accountId: String? = null,
        categoryId: String? = null,
        type: String? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByUser("").map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getTransactionsForUser(
        accountId: String? = null,
        categoryId: String? = null,
        type: String? = null,
        startDate: Long? = null,
        endDate: Long? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): Flow<List<Transaction>> {
        val userId = preferencesManager.userId.first() ?: return transactionDao.getTransactionsByUser("").map { emptyList() }
        return if (startDate != null && endDate != null) {
            transactionDao.getTransactionsByDateRange(userId, startDate, endDate)
        } else {
            transactionDao.getTransactionsByUser(userId)
        }.map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun refresh() {
        try {
            val response = transactionApi.getTransactions()
            if (response.isSuccessful && response.body()?.success == true) {
                val transactions: List<TransactionDto>? = response.body()?.data
                if (transactions != null) {
                    transactionDao.insertAll(transactions.map { dto ->
                        TransactionEntity(
                            id = dto.id,
                            userId = dto.userId,
                            accountId = dto.accountId,
                            accountName = dto.accountName,
                            categoryId = dto.categoryId,
                            categoryName = dto.categoryName,
                            categoryIcon = dto.categoryIcon,
                            categoryColor = dto.categoryColor,
                            type = dto.type,
                            amount = dto.amount.toDoubleOrNull() ?: 0.0,
                            currency = dto.currency,
                            description = dto.description,
                            note = dto.note,
                            date = dto.date.toLongOrNull() ?: System.currentTimeMillis(),
                            sourceType = dto.sourceType,
                            referenceId = dto.referenceId,
                            relatedTransactionId = dto.relatedTransactionId,
                            createdAt = System.currentTimeMillis()
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
 * Use case for creating a transaction
 */
class CreateTransactionUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val transactionApi: TransactionApi
) {
    suspend operator fun invoke(
        accountId: String,
        categoryId: String?,
        type: String,
        amount: Double,
        currency: String = "VND",
        description: String? = null,
        note: String? = null,
        date: Long? = null,
        sourceType: String? = null
    ): Result<Transaction> {
        return try {
            val response = transactionApi.createTransaction(
                CreateTransactionRequest(
                    accountId = accountId,
                    categoryId = categoryId,
                    type = type,
                    amount = amount.toString(),
                    currency = currency,
                    description = description,
                    note = note,
                    date = date?.toString(),
                    sourceType = sourceType
                )
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val dto = response.body()?.data!!
                val entity = TransactionEntity(
                    id = dto.id,
                    userId = dto.userId,
                    accountId = dto.accountId,
                    accountName = dto.accountName,
                    categoryId = dto.categoryId,
                    categoryName = dto.categoryName,
                    categoryIcon = dto.categoryIcon,
                    categoryColor = dto.categoryColor,
                    type = dto.type,
                    amount = dto.amount.toDoubleOrNull() ?: 0.0,
                    currency = dto.currency,
                    description = dto.description,
                    note = dto.note,
                    date = dto.date.toLongOrNull() ?: System.currentTimeMillis(),
                    sourceType = dto.sourceType,
                    referenceId = dto.referenceId,
                    relatedTransactionId = dto.relatedTransactionId,
                    createdAt = System.currentTimeMillis()
                )
                transactionDao.insert(entity)
                Result.success(entity.toDomain())
            } else {
                Result.failure(Exception(response.body()?.message ?: "Create transaction failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for getting recent transactions
 */
class GetRecentTransactionsUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val preferencesManager: PreferencesManager
) {
    suspend operator fun invoke(limit: Int = 10): Flow<List<Transaction>> {
        val userId = preferencesManager.userId.first() ?: return transactionDao.getRecentTransactions("", limit).map { emptyList() }
        return transactionDao.getRecentTransactions(userId, limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}

// Extension function to convert Entity to Domain
private fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        userId = userId,
        accountId = accountId,
        accountName = accountName,
        categoryId = categoryId,
        categoryName = categoryName,
        categoryIcon = categoryIcon,
        categoryColor = categoryColor,
        type = TransactionType.valueOf(type),
        amount = amount,
        currency = currency,
        description = description,
        note = note,
        date = date,
        sourceType = sourceType,
        referenceId = referenceId,
        relatedTransactionId = relatedTransactionId,
        createdAt = createdAt
    )
}

/**
 * Use case for getting monthly income and expense statistics
 */
class GetMonthlyStatsUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val preferencesManager: PreferencesManager
) {
    suspend operator fun invoke(): Flow<Pair<Double, Double>> {
        val userId = preferencesManager.userId.first() ?: return flowOf(Pair(0.0, 0.0))

        // Get start and end of current month
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endOfMonth = calendar.timeInMillis

        return combine(
            transactionDao.getTotalByTypeAndDateRange(userId, "INCOME", startOfMonth, endOfMonth),
            transactionDao.getTotalByTypeAndDateRange(userId, "EXPENSE", startOfMonth, endOfMonth)
        ) { income, expense ->
            Pair(income ?: 0.0, expense ?: 0.0)
        }
    }
}
