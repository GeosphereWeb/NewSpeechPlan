package de.geosphere.speechplaning.data.usecases.planning

import de.geosphere.speechplaning.data.repository.CongregationEventRepositoryImpl

class DeleteCongregationEventUseCase(private val repository: CongregationEventRepositoryImpl) {
    suspend operator fun invoke(districtId: String, congregationId: String, eventId: String): Result<Unit> {
        return try {
            repository.deleteEvent(districtId, congregationId, eventId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
