package de.geosphere.speechplaning.data.usecases.congregationEvent

import android.util.Log
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.data.repository.CongregationEventRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class GetAllCongregationEventUseCase(
    private val congregationEventRepository: CongregationEventRepositoryImpl
) {
    private val TAG = "GetAllCongregationEventUseCase"
    /**
     * @return Ein Flow, der eine Liste von Versammlungen emittiert, verpackt in ein Result.
     */
    operator fun invoke(): Flow<Result<List<CongregationEvent>>> {
        return congregationEventRepository.getAllEventsFlow()
            .onEach { list ->
                try {
                    val ids = list.mapNotNull { it.id }.take(5)
                    Log.d(TAG, "collectionGroup emitted size=${list.size}, sampleIds=$ids")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to log collectionGroup list", e)
                }
            }
            .map { list ->
                // Erfolgreiche Daten in Result.success verpacken
                Result.success(list)
            }
            .catch { exception ->
                // Fehler abfangen und als Result.failure emittieren
                Log.e(TAG, "Flow error", exception)
                emit(Result.failure(exception))
            }
    }
}
