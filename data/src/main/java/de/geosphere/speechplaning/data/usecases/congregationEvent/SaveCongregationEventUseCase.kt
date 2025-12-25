package de.geosphere.speechplaning.data.usecases.congregationEvent

import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.data.repository.CongregationEventRepositoryImpl

@Suppress("TooGenericExceptionCaught")
class SaveCongregationEventUseCase(private val repository: CongregationEventRepositoryImpl) {
    suspend operator fun invoke(congregationEvent: CongregationEvent): Result<Unit> {
        // Optional: validate minimal fields
        return try {
            repository.saveEvent(congregationEvent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
