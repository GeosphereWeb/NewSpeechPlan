package de.geosphere.speechplaning.data.usecases.planning

import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.data.repository.CongregationEventRepository

class SaveCongregationEventUseCase(private val repository: CongregationEventRepository) {
    suspend operator fun invoke(districtId: String, congregationId: String, event: CongregationEvent): Result<String> {
        return try {
            val eventId = repository.saveEvent(districtId, congregationId, event)
            Result.success(eventId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
