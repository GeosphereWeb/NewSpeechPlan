package de.geosphere.speechplaning.data.usecases.congregationEvent

import android.util.Log
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.data.repository.CongregationEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

private const val SAMPLE_IDS_LIMIT = 5

class GetAllCongregationEventUseCase(
    private val congregationEventRepository: CongregationEventRepository
) {
    private val tag = "GetAllCongregationEventUseCase"

    /**
     * @return Ein Flow, der eine Liste von Versammlungen emittiert, verpackt in ein Result.
     */
    operator fun invoke(districtId: String, congregationId: String): Flow<Result<List<CongregationEvent>>> {
        return congregationEventRepository.getAllFlow(districtId, congregationId)
            .onEach { list ->
                try {
                    val ids = list.mapNotNull { it.id }.take(SAMPLE_IDS_LIMIT)
                    Log.d(tag, "collectionGroup emitted size=${'$'}{list.size}, sampleIds=${'$'}ids")
                } catch (e: Exception) {
                    Log.w(tag, "Failed to log collectionGroup list", e)
                }
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
