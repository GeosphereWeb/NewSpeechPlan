package de.geosphere.speechplaning.data.usecases.congregationEvent

import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.data.repository.CongregationEventRepositoryImpl

@Suppress("TooGenericExceptionCaught")
class SaveCongregationEventUseCase(private val repository: CongregationEventRepositoryImpl) {
    suspend operator fun invoke(congregationEvent: CongregationEvent): Result<Unit> {
        // Wichtige Validierung: Ein Event muss immer einer Versammlung zugeordnet sein.
        // Ohne diese ID können die Sicherheitsregeln nicht funktionieren.
        if (congregationEvent.congregationId.isBlank()) {
            return Result.failure(IllegalArgumentException("CongregationEvent must have a valid congregationId to be saved."))
        }

        return try {
            // Aufruf der korrekten 'save'-Methode aus dem Basis-Repository.
            // Diese Methode kann zwischen "neu erstellen" und "aktualisieren" unterscheiden.
            repository.save(congregationEvent)
            Result.success(Unit)
        } catch (e: Exception) {
            // Das Repository wirft bei Fehlern bereits eine detaillierte RuntimeException
            Result.failure(e)
        }
    }
}
