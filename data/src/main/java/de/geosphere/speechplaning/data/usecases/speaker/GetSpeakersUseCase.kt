package de.geosphere.speechplaning.data.usecases.speaker

import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.data.repository.SpeakerRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class GetSpeakersUseCase(private val repository: SpeakerRepositoryImpl) {
    /**
     * Ruft Redner ab.
     * @param parentIds Wenn angegeben (districtId, congregationId), werden Redner dieser Versammlung geladen.
     *                  Wenn LEER, werden ALLE Redner global (Collection Group Query) geladen.
     */
    operator fun invoke(vararg parentIds: String): Flow<Result<List<Speaker>>> {
        val flow = if (parentIds.isEmpty()) {
            // Keine IDs -> Globale Suche (Collection Group)
            repository.getAllSpeakersGlobalFlow()
        } else {
            // IDs vorhanden -> Spezifische Suche in einer Versammlung
            repository.getAllFlow(*parentIds)
        }

        return flow
            .map { list ->
                // Sortieren (z.B. nach Nachname)
                Result.success(list.sortedBy { it.lastName })
            }
            .catch { exception ->
                emit(Result.failure(exception))
            }
    }
}
