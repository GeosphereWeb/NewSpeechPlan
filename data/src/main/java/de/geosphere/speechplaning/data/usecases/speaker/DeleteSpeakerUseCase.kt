package de.geosphere.speechplaning.data.usecases.speaker

import de.geosphere.speechplaning.data.repository.SpeakerRepository

class DeleteSpeakerUseCase(private val repository: SpeakerRepository) {
    suspend operator fun invoke(districtId: String, congregationId: String, speakerId: String): Result<Unit> {
        return try {
            repository.deleteSpeaker(districtId, congregationId, speakerId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
