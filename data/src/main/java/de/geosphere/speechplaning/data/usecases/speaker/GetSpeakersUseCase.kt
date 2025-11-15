package de.geosphere.speechplaning.data.usecases.speaker

import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.data.repository.SpeakerRepository

class GetSpeakersUseCase(private val repository: SpeakerRepository) {
    suspend operator fun invoke(districtId: String, congregationId: String): Result<List<Speaker>> {
        return try {
            val speakers = repository.getSpeakersForCongregation(districtId, congregationId)
            Result.success(speakers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
