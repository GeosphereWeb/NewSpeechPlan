package de.geosphere.speechplaning.data.usecases.congregationEvent

import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.data.repository.CongregationEventRepositoryImpl

@Suppress("TooGenericExceptionCaught")
class GetCongregationEventsUseCase(private val repository: CongregationEventRepositoryImpl) {
    suspend operator fun invoke(districtId: String, congregationId: String): Result<List<CongregationEvent>> {
        return try {
            val events = repository.getAll()
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
