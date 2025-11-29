package de.geosphere.speechplaning.data.usecases.speeches

import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.data.repository.SpeechRepositoryImpl

class SaveSpeechUseCase(private val repository: SpeechRepositoryImpl) {
    suspend operator fun invoke(speech: Speech): Result<Unit> {
        // Basic validation
        if (speech.number.isBlank() || speech.subject.isBlank()) {
            return Result.failure(IllegalArgumentException("Speech number and subject cannot be blank."))
        }
        return try {
            repository.saveSpeech(speech)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
