package de.geosphere.speechplaning.data.usecases.districts

import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.data.repository.DistrictRepositoryImpl

class SaveDistrictUseCase(private val repository: DistrictRepositoryImpl) {
    suspend operator fun invoke(district: District): Result<Unit> {
        // Basic validation
        if (district.name.isBlank()) {
            return Result.failure(IllegalArgumentException("District name cannot be blank."))
        }

        return try {
            repository.saveDistrict(district)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
