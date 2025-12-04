package de.geosphere.speechplaning.data.usecases.districts

import de.geosphere.speechplaning.data.repository.DistrictRepositoryImpl

class DeleteDistrictUseCase(private val repository: DistrictRepositoryImpl) {
    suspend operator fun invoke(speechId: String): Result<Unit> {
        if (speechId.isBlank()) {
            return Result.failure(IllegalArgumentException("District ID cannot be blank."))
        }
        return try {
            repository.deleteDistrict(speechId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
