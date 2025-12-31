package de.geosphere.speechplaning.data.usecases.congregation

import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.data.repository.CongregationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class GetCongregationUseCase(private val repository: CongregationRepository) {
    operator fun invoke(vararg parentIds: String): Flow<Result<List<Congregation>>> {
        return repository.getAllFlow(*parentIds)
            .map { list -> // Sortierung und Success-Wrapper
                Result.success(list.sortedBy { it.id.toInt() })
            }
            .catch { e ->
                // Fehler abfangen und als Result.failure senden
                emit(Result.failure(e))
            }
    }
}
