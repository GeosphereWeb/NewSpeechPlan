package de.geosphere.speechplaning.data.usecases.congregation

import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.data.repository.CongregationRepositoryImpl

class SaveCongregationUseCase(private val repository: CongregationRepositoryImpl) {
    suspend operator fun invoke(congregation: Congregation): Result<Unit> {
        // Basic validation
        if (congregation.id.isBlank()) {
            return Result.failure(IllegalArgumentException("Congregation id cannot be blank."))
        }
        return try {
            repository.save(congregation)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
