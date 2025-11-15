package de.geosphere.speechplaning.data.usecases.speaker

import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.data.repository.SpeakerRepository

class SaveSpeakerUseCase(private val repository: SpeakerRepository) {
    suspend operator fun invoke(districtId: String, congregationId: String, speaker: Speaker): Result<String> {
        return try {
            val speakerId = repository.saveSpeaker(districtId, congregationId, speaker)
            Result.success(speakerId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
