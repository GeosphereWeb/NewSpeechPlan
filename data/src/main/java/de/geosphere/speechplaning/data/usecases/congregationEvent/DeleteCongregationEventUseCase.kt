package de.geosphere.speechplaning.data.usecases.congregationEvent

import de.geosphere.speechplaning.data.repository.CongregationEventRepository

@Suppress("TooGenericExceptionCaught")
class DeleteCongregationEventUseCase(private val repository: CongregationEventRepository) {
    suspend operator fun invoke(eventId: String): Result<Unit> {
        if (eventId.isBlank()) {
            return Result.failure(IllegalArgumentException("CongregationEventRepository ID cannot be blank."))
        }
        return try {
            repository.deleteEvent(eventId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
