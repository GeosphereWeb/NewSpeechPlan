package de.geosphere.speechplaning.data.usecases.speaker

import de.geosphere.speechplaning.data.repository.SpeakerRepository

class DeleteSpeakerUseCase(private val repository: SpeakerRepository) {
    suspend operator fun invoke(districtId: String, congregationId: String, speakerId: String): Result<Unit> {
        if (speakerId.isBlank()) {
            return Result.failure(IllegalArgumentException("Speaker ID cannot be blank."))
        }
        return try {
            repository.delete(speakerId, districtId, congregationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
