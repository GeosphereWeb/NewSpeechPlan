package de.geosphere.speechplaning.data.usecases.speeches

import de.geosphere.speechplaning.core.model.data.SpeechUsageDetail
import de.geosphere.speechplaning.core.model.data.SpeechWithUsageHistory
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
    operator fun invoke(): Flow<Result<List<SpeechWithUsageHistory>>> {
        return combine(
            getSpeechesUseCase(),
            getAllCongregationEventUseCase()
        ) { speechesResult, eventsResult ->
            try {
                val speeches = speechesResult.getOrThrow()
                val events = eventsResult.getOrThrow()

                val speechWithUsageHistory = speeches.map { speech ->
                    val usageHistory = events
                        .filter { it.speechId == speech.id }
                        .mapNotNull { event ->
                            // Nur Events mit beiden Daten hinzufügen
                            if (!event.dateString.isNullOrBlank() && !event.speakerName.isNullOrBlank()) {
                                SpeechUsageDetail(
                                    dateString = event.dateString ?: "",
                                    speakerName = event.speakerName ?: ""
                                )
                            } else {
                                null
                            }
                        }
                        .sortedByDescending { it.dateString }

                    SpeechWithUsageHistory(
                        speech = speech,
                        timesUsed = usageHistory.size,
                        usageHistory = usageHistory
                    )
                }

                Result.success(speechWithUsageHistory.sortedBy { it.speech.number.toIntOrNull() ?: 0 })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }.catch { exception ->
            emit(Result.failure(exception))
        }
    }
}
