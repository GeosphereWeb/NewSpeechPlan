package de.geosphere.speechplaning.data.usecases.congregationEvent

import android.util.Log
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.data.repository.CongregationEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class GetAllCongregationEventUseCase(
    private val congregationEventRepository: CongregationEventRepository
) {
    private val tag = "GetAllCongregationEventUseCase"

    /**
     * @return Ein Flow, der eine Liste von Versammlungen emittiert, verpackt in ein Result.
     */
    operator fun invoke(): Flow<Result<List<CongregationEvent>>> {
        return congregationEventRepository.getAllFlow()
            .map { list ->
                // Daten sortieren
                list.sortedBy { it.date }
            }
            .map { list ->
                // Erfolgreiche Daten in Result.success verpacken
                Result.success(list)
            }
            .catch { exception ->
                // Fehler abfangen und als Result.failure emittieren
                Log.e(tag, "Flow error", exception)
                emit(Result.failure(exception))
            }
    }
}
