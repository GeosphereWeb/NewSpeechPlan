package de.geosphere.speechplaning.data.usecases.speeches

import de.geosphere.speechplaning.core.model.SpeechWithUsageCount
import de.geosphere.speechplaning.data.usecases.congregationEvent.GetAllCongregationEventUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine

/**
 * Use Case, der Speeches mit ihrer Verwendungshäufigkeit in CongregationEvents kombiniert.
 * Für jede Rede wird gezählt, wie oft sie in den Versammlungsereignissen verwendet wurde.
 */
class GetSpeechesWithUsageCountUseCase(
    private val getSpeechesUseCase: GetSpeechesUseCase,
    private val getAllCongregationEventUseCase: GetAllCongregationEventUseCase
) {
    operator fun invoke(): Flow<Result<List<SpeechWithUsageCount>>> {
        return combine(
            getSpeechesUseCase(),
            getAllCongregationEventUseCase()
        ) { speechesResult, eventsResult ->
            try {
                val speeches = speechesResult.getOrThrow()
                val events = eventsResult.getOrThrow()

                // Für jede Speech zählen, wie oft sie in Events verwendet wurde
                val speechWithUsageCount = speeches.map { speech ->
                    val timesUsed = events.count { event ->
                        event.speechId == speech.id
                    }
                    SpeechWithUsageCount(speech, timesUsed)
                }

                // Nach Sprachnummer sortieren
                Result.success(speechWithUsageCount.sortedBy { it.speech.number.toIntOrNull() ?: 0 })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
            .catch { exception ->
                emit(Result.failure(exception))
            }
    }
}
