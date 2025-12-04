package de.geosphere.speechplaning.data.usecases.speeches

import de.geosphere.speechplaning.data.repository.SpeechRepositoryImpl

class DeleteSpeechUseCase(private val repository: SpeechRepositoryImpl) {
    suspend operator fun invoke(speechId: String): Result<Unit> {
        if (speechId.isBlank()) {
            return Result.failure(IllegalArgumentException("Speech ID cannot be blank."))
        }
        return try {
            repository.deleteSpeech(speechId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
