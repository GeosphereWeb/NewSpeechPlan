package de.geosphere.speechplaning.data.usecases.congregationEvent

import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.data.repository.CongregationEventRepository
import kotlinx.coroutines.flow.first

@Suppress("TooGenericExceptionCaught")
class GetCongregationEventsUseCase(private val repository: CongregationEventRepository) {
    suspend operator fun invoke(districtId: String, congregationId: String): Result<List<CongregationEvent>> {
        return try {
            val events = repository.getAllEventsFlow().first()
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
