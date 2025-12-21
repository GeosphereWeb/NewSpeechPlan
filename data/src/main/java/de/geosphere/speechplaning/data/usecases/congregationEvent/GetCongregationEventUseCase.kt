package de.geosphere.speechplaning.data.usecases.congregationEvent

import android.util.Log
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.data.repository.CongregationEventRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

@Suppress("TooGenericExceptionCaught")
class GetCongregationEventUseCase(private val repository: CongregationEventRepositoryImpl) {
    private val TAG = "GetCongregationEventUseCase"
    // WICHTIG: Kein 'suspend' mehr, da ein Flow direkt zurückgegeben wird
    operator fun invoke(): Flow<Result<List<CongregationEvent>>> {
        return repository.getAllEventsFlow()
            .onEach { list ->
                try {
                    val ids = list.mapNotNull { it.id }.take(5)
                    Log.d(TAG, "received ${list.size} congregationEvents, sampleIds=$ids")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to log incoming list", e)
                }
            }
            .map { list -> // Sortierung und Success-Wrapper
                // Sortieren nach ID
                Result.success(list.sortedBy { it.id })
            }
            .catch { e ->
                // Fehler abfangen und als Result.failure senden
                Log.e(TAG, "Flow error", e)
                emit(Result.failure(e))
            }
    }
}
