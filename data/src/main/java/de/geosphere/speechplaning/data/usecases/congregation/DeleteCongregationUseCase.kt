package de.geosphere.speechplaning.data.usecases.congregation

import de.geosphere.speechplaning.data.repository.CongregationRepositoryImpl

class DeleteCongregationUseCase(private val repository: CongregationRepositoryImpl) {
    suspend operator fun invoke(congregationId: String, district: String): Result<Unit> {
        if (congregationId.isBlank()) {
            return Result.failure(IllegalArgumentException("Congregation ID cannot be blank."))
        }
        return try {
            repository.delete(congregationId, district)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
