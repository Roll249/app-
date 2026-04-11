package com.fintech.domain.usecase.category

import com.fintech.data.local.db.dao.CategoryDao
import com.fintech.data.local.db.entity.CategoryEntity
import com.fintech.data.remote.api.services.CategoryApi
import com.fintech.data.remote.model.request.CreateCategoryRequest
import com.fintech.data.remote.model.response.CategoryDto
import com.fintech.domain.model.Category
import com.fintech.domain.model.CategoryType
import com.fintech.data.local.datastore.PreferencesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for getting categories
 */
class GetCategoriesUseCase @Inject constructor(
    private val categoryDao: CategoryDao,
    private val categoryApi: CategoryApi,
    private val preferencesManager: PreferencesManager
) {
    operator fun invoke(type: String? = null): Flow<List<Category>> {
        return categoryDao.getCategories("").map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getCategoriesForUser(type: String? = null): Flow<List<Category>> {
        val userId = preferencesManager.userId.first() ?: return categoryDao.getCategories("").map { emptyList() }
        return if (type != null) {
            categoryDao.getCategoriesByType(userId, type)
        } else {
            categoryDao.getCategories(userId)
        }.map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun refresh() {
        try {
            val response = categoryApi.getCategories()
            if (response.isSuccessful && response.body()?.success == true) {
                val categories: List<CategoryDto>? = response.body()?.data
                if (categories != null) {
                    categoryDao.insertAll(categories.map { dto ->
                        CategoryEntity(
                            id = dto.id,
                            userId = dto.userId,
                            name = dto.name,
                            icon = dto.icon,
                            color = dto.color,
                            type = dto.type,
                            parentId = dto.parentId,
                            isSystem = dto.isSystem,
                            isActive = dto.isActive,
                            sortOrder = dto.sortOrder,
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
 * Use case for creating a category
 */
class CreateCategoryUseCase @Inject constructor(
    private val categoryDao: CategoryDao,
    private val categoryApi: CategoryApi
) {
    suspend operator fun invoke(
        name: String,
        type: String,
        icon: String? = null,
        color: String? = null,
        parentId: String? = null
    ): Result<Category> {
        return try {
            val response = categoryApi.createCategory(
                CreateCategoryRequest(
                    name = name,
                    type = type,
                    icon = icon,
                    color = color,
                    parentId = parentId
                )
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val dto = response.body()?.data!!
                val entity = CategoryEntity(
                    id = dto.id,
                    userId = dto.userId,
                    name = dto.name,
                    icon = dto.icon,
                    color = dto.color,
                    type = dto.type,
                    parentId = dto.parentId,
                    isSystem = dto.isSystem,
                    isActive = dto.isActive,
                    sortOrder = dto.sortOrder,
                    createdAt = System.currentTimeMillis()
                )
                categoryDao.insert(entity)
                Result.success(entity.toDomain())
            } else {
                Result.failure(Exception(response.body()?.message ?: "Create category failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Extension function to convert Entity to Domain
private fun CategoryEntity.toDomain(): Category {
    return Category(
        id = id,
        userId = userId,
        name = name,
        icon = icon,
        color = color,
        type = CategoryType.valueOf(type),
        parentId = parentId,
        isSystem = isSystem,
        isActive = isActive,
        sortOrder = sortOrder
    )
}
