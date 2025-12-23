package de.geosphere.speechplaning.data.usecases.congregationEvent

import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.data.repository.CongregationEventRepositoryImpl

@Suppress("TooGenericExceptionCaught")
class SaveCongregationEventUseCase(private val repository: CongregationEventRepositoryImpl) {
    suspend operator fun invoke(congregationEvent: CongregationEvent): Result<Unit> {
        // Basic validation
        if (congregationEvent.date == null) {
            return Result.failure(IllegalArgumentException("CongregationEvent date cannot be null."))
        }

        return try {
            repository.saveEvent(congregationEvent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
