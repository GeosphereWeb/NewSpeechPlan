package de.geosphere.speechplaning.data.usecases.speeches

import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.data.repository.SpeechRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class GetSpeechesUseCase(private val repository: SpeechRepository) {
    // WICHTIG: Kein 'suspend' mehr, da ein Flow direkt zurückgegeben wird
    operator fun invoke(): Flow<Result<List<Speech>>> {
        return repository.getAllSpeechesFlow()
            .map { list -> // Sortierung und Success-Wrapper
                Result.success(list.sortedBy { it.number.toInt() })
            }
            .catch { e ->
                // Fehler abfangen und als Result.failure senden
                emit(Result.failure(e))
            }
    }
}
