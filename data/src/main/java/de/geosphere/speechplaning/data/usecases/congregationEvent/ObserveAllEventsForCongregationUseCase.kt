package de.geosphere.speechplaning.data.usecases.congregationEvent

import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.data.repository.CongregationEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * UseCase to observe a list of all events for a specific congregation as a live stream.
 */
class ObserveAllEventsForCongregationUseCase(private val repository: CongregationEventRepository) {
    operator fun invoke(districtId: String, congregationId: String): Flow<Result<List<CongregationEvent>>> {
        return repository.getAllFlow(districtId, congregationId)
            .map { events -> Result.success(events) }
            .catch { e -> emit(Result.failure(e)) }
    }
}
