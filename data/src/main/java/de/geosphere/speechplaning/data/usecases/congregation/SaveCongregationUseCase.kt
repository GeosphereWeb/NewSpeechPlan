package de.geosphere.speechplaning.data.usecases.congregation

import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.data.repository.CongregationRepository

class SaveCongregationUseCase(private val repository: CongregationRepository) {
    suspend operator fun invoke(districtId: String, congregation: Congregation): Result<Unit> {
        // Basic validation
        if (congregation.name.isBlank()) {
            // Kleine Korrektur: Die Fehlermeldung sprach von "id", prüfte aber "name"
            return Result.failure(IllegalArgumentException("Congregation name cannot be blank."))
        }
        return try {
            repository.saveCongregation(districtId, congregation)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
