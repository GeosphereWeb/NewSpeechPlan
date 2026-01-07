package de.geosphere.speechplaning.feature.congregationEvent

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.core.model.data.Event
import de.geosphere.speechplaning.core.ui.provider.AppEventStringProvider
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CongregationEventListContent(
    congregationEvents: List<CongregationEvent>,
    onSelectCongregationEvent: (CongregationEvent) -> Unit,
    stringProvider: AppEventStringProvider
) {
    val groupedEvents = remember(congregationEvents) {
        congregationEvents
            .sortedBy { it.date }
            .groupBy { it.date?.year ?: 0 }
            .mapValues { entry ->
                entry.value.groupBy { it.date?.month ?: java.time.Month.JANUARY }
            }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        groupedEvents.forEach { (year, eventsByMonth) ->
            stickyHeader {
                YearHeader(year = year)
            }

            eventsByMonth.forEach { (month, eventsInMonth) ->
                stickyHeader {
                    MonthHeader(month = month, year = year)
                }

                items(eventsInMonth, key = { it.id.ifBlank { it.hashCode() } }) { event ->
                    CongregationEventListItem(
                        congregationEvent = event,
                        onClick = { onSelectCongregationEvent(event) },
                        onLongClick = null,
                        stringProvider = stringProvider
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
fun CongregationEventListContentPreview() = SpeechPlaningTheme {
    val mockEvents = listOf(
        CongregationEvent(
            id = "1",
            dateString = "2026-01-15",
            speechNumber = "123",
            speechSubject = "Vortrag über Glauben",
            speakerName = "Müller, Max",
            speakerCongregationName = "Berlin-Mitte",
            eventType = Event.MISCELLANEOUS
        ),
        CongregationEvent(
            id = "2",
            dateString = "2026-01-22",
            speechNumber = "456",
            speechSubject = "Hoffnung für die Zukunft",
            speakerName = "Schmidt, Lisa",
            speakerCongregationName = "Hamburg-Nord",
            eventType = Event.MEMORIAL
        ),
        CongregationEvent(
            id = "3",
            dateString = "2026-02-05",
            speechNumber = "789",
            speechSubject = "Gottes Königreich",
            speakerName = "Weber, Thomas",
            speakerCongregationName = "München-Süd",
            eventType = Event.CONVENTION
        )
    )
    CongregationEventListContent(
        congregationEvents = mockEvents,
        onSelectCongregationEvent = {},
        stringProvider = AppEventStringProvider(context = LocalContext.current)
    )
}
