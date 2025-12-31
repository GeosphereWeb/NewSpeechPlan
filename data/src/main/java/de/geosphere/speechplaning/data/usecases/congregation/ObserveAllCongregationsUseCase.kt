package de.geosphere.speechplaning.data.usecases.congregation

import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.data.repository.CongregationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Ruft alle Versammlungen über alle Bezirke hinweg ab.
 * Nützlich für Auswahl-Listen (Dropdowns).
 */
class ObserveAllCongregationsUseCase(private val repository: CongregationRepository) {
    operator fun invoke(): Flow<Result<List<Congregation>>> {
        return repository.getAllCongregationsGlobalFlow()
            .map { list ->
                // Sortiert nach Name für bessere UX
                Result.success(list.sortedBy { it.name })
            }
            .catch { e ->
                emit(Result.failure(e))
            }
    }
}
