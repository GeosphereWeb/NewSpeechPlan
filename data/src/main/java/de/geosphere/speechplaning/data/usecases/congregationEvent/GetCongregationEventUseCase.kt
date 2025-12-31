package de.geosphere.speechplaning.data.usecases.congregationEvent

import android.util.Log
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.data.repository.CongregationEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

private const val SAMPLE_IDS_LIMIT = 5

@Suppress("TooGenericExceptionCaught")
class GetCongregationEventUseCase(private val repository: CongregationEventRepository) {
    private val tag = "GetCongregationEventUseCase"

    // Akzeptiert optional parentIds: wenn leer -> collection-group-flow (global), ansonsten Subcollection-Flow
    operator fun invoke(vararg parentIds: String): Flow<Result<List<CongregationEvent>>> {
        val flow = if (parentIds.isEmpty()) {
            repository.getAllEventsFlow()
        } else {
            repository.getAllFlow(*parentIds)
        }

        return flow
            .onEach { list ->
                try {
                    val ids = list.mapNotNull { it.id }.take(SAMPLE_IDS_LIMIT)
                    Log.d(tag, "received ${'$'}{list.size} congregationEvents, sampleIds=${'$'}ids")
                } catch (e: Exception) {
                    Log.w(tag, "Failed to log incoming list", e)
                }
            }
            .map { list -> // Sortierung und Success-Wrapper
                // Sortieren nach ID
                Result.success(list.sortedBy { it.id })
            }
            .catch { e ->
                // Fehler abfangen und als Result.failure senden
                Log.e(tag, "Flow error", e)
                emit(Result.failure(e))
            }
    }
}
