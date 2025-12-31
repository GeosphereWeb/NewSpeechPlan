package de.geosphere.speechplaning.data.usecases.congregationEvent

import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.data.repository.CongregationEventRepository

/**
 * UseCase to get a single, specific event by its unique ID.
 * This is a one-shot operation.
 */
class GetEventByIdUseCase(private val repository: CongregationEventRepository) {
    @Suppress("TooGenericExceptionCaught")
    suspend operator fun invoke(eventId: String): Result<CongregationEvent?> {
        if (eventId.isBlank()) {
            return Result.failure(IllegalArgumentException("Event ID cannot be blank."))
        }
        return try {
            val event = repository.getEventById(eventId)
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
