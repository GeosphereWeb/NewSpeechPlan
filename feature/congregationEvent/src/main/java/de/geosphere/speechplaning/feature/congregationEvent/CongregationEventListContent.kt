@file:Suppress("MatchingDeclarationName", "TooManyFunctions")

package de.geosphere.speechplaning.feature.congregationEvent

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.core.model.data.Event
import de.geosphere.speechplaning.core.ui.provider.AppEventStringProvider
import de.geosphere.speechplaning.data.util.isInCurrentWeek
import de.geosphere.speechplaning.theme.R
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews
import de.geosphere.speechplaning.theme.surfaceVariantLightHighContrast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Month

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CongregationEventListContent(
    congregationEvents: List<CongregationEvent>,
    onSelectCongregationEvent: (CongregationEvent) -> Unit,
    stringProvider: AppEventStringProvider,
    isWhatsAppInstalled: Boolean,
    onToggleShowPlanedItems: () -> Unit,
    selectedShowPlanedItems: Boolean
) {
    var initialScrollDone by rememberSaveable { mutableStateOf(false) }

    val filteredEvents = congregationEvents

    val groupedEvents = remember(filteredEvents) { groupEventsByYearAndMonth(filteredEvents) }

    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var yearHeaderHeightPx by remember { mutableIntStateOf(0) }

    LaunchedEffect(filteredEvents, yearHeaderHeightPx) {
        if (!initialScrollDone && filteredEvents.isNotEmpty() && yearHeaderHeightPx > 0) {
            scrollToCurrentWeek(
                sortedEvents = filteredEvents,
                groupedEvents = groupedEvents,
                listState = listState,
                coroutineScope = coroutineScope,
                yearHeaderHeightPx = yearHeaderHeightPx,
                animated = false
            )
            initialScrollDone = true
        }
    }

    LaunchedEffect(selectedShowPlanedItems) {
        if (selectedShowPlanedItems && filteredEvents.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column {
        ScrollToCurrentWeekButton(
            onToggleShowPlanedItems = onToggleShowPlanedItems,
            selectedShowPlanedItems = selectedShowPlanedItems,
            onClick = {
                scrollToCurrentWeek(
                    sortedEvents = filteredEvents,
                    groupedEvents = groupedEvents,
                    listState = listState,
                    coroutineScope = coroutineScope,
                    yearHeaderHeightPx = yearHeaderHeightPx,
                    animated = true
                )
            }
        )
        EventsList(
            groupedEvents,
            yearHeaderHeightPx,
            { yearHeaderHeightPx = it },
            context,
            stringProvider,
            isWhatsAppInstalled,
            onSelectCongregationEvent,
            listState
        )
    }
}

@Composable
private fun ScrollToCurrentWeekButton(
    onToggleShowPlanedItems: () -> Unit,
    selectedShowPlanedItems: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            modifier = Modifier,
            onClick = { onToggleShowPlanedItems() },
            label = { Text("Nur ungeplante") },
            selected = selectedShowPlanedItems,
            leadingIcon = if (selectedShowPlanedItems) {
                {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.check_small),
                        contentDescription = "Done icon",
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else {
                null
            },
        )
        Button(onClick = onClick) {
            Text(text = "Heute")
            Icon(imageVector = ImageVector.vectorResource(R.drawable.today), contentDescription = null)
        }
    }
}

@Suppress("UnusedParameter")
@Composable
private fun EventsList(
    groupedEvents: Map<Int, Map<Month, List<CongregationEvent>>>,
    yearHeaderHeightPx: Int,
    onYearHeaderHeightChanged: (Int) -> Unit,
    context: Context,
    stringProvider: AppEventStringProvider,
    isWhatsAppInstalled: Boolean,
    onSelectCongregationEvent: (CongregationEvent) -> Unit,
    listState: LazyListState
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
        groupedEvents.forEach { (year, eventsByMonth) ->
            stickyHeader {
                YearHeader(
                    year = year,
                    modifier = Modifier.onSizeChanged { size -> onYearHeaderHeightChanged(size.height) }
                )
            }
            eventsByMonth.forEach { (month, eventsInMonth) ->
                stickyHeader {
                    MonthHeader(month = month, year = year, modifier = Modifier)
                }
                items(eventsInMonth, key = { it.id.ifBlank { it.hashCode() } }) { event ->
                    EventRow(event, context, stringProvider, isWhatsAppInstalled, onSelectCongregationEvent)
                }
            }
        }
    }
}

@Composable
private fun EventRow(
    event: CongregationEvent,
    context: Context,
    stringProvider: AppEventStringProvider,
    isWhatsAppInstalled: Boolean,
    onSelectCongregationEvent: (CongregationEvent) -> Unit
) {
    SwipeableItemWithActions(
        isRevealed = false,
        actionsLeft = { LeftActionButtons(event, context) },
        actionsRight = { RightActionButtons(event, context, isWhatsAppInstalled) },
        modifier = Modifier,
        onExpanded = { },
        onCollapsed = { }
    ) {
        CongregationEventListItem(
            congregationEvent = event,
            onClick = { onSelectCongregationEvent(event) },
            onLongClick = null,
            stringProvider = stringProvider
        )
    }
    HorizontalDivider()
}

private fun groupEventsByYearAndMonth(sortedEvents: List<CongregationEvent>): Map<
    Int,
    Map<Month, List<CongregationEvent>>
    > {
    return sortedEvents
        .groupBy { it.date?.year ?: 0 }
        .mapValues { entry ->
            entry.value.groupBy { it.date?.month ?: Month.JANUARY }
        }
}

private fun scrollToCurrentWeek(
    sortedEvents: List<CongregationEvent>,
    groupedEvents: Map<Int, Map<Month, List<CongregationEvent>>>,
    listState: LazyListState,
    coroutineScope: CoroutineScope,
    yearHeaderHeightPx: Int,
    animated: Boolean = true
) {
    val targetEvent = sortedEvents.find { it.date?.isInCurrentWeek() ?: false } ?: return

    var index = 0
    var found = false

    groupedEvents.forEach { (_, eventsByMonth) ->
        if (found) return@forEach
        index++ // Year header

        eventsByMonth.forEach { (_, eventsInMonth) ->
            if (found) return@forEach
            index++ // Month header

            val indexInMonth = eventsInMonth.indexOf(targetEvent)
            if (indexInMonth != -1) {
                index += indexInMonth
                found = true
            } else {
                index += eventsInMonth.size
            }
        }
    }

    if (found) {
        coroutineScope.launch {
            if (animated) {
                listState.animateScrollToItem(index = index, scrollOffset = -yearHeaderHeightPx)
            } else {
                listState.scrollToItem(index = index, scrollOffset = -yearHeaderHeightPx)
            }
        }
    }
}

@Composable
private fun LeftActionButtons(event: CongregationEvent, context: Context) {
    Row {
        if (!event.speakerMobile.isNullOrEmpty()) {
            SimpleCallButton(event.speakerMobile!!, R.drawable.send_to_mobile, context)
        }
        if (!event.speakerPhone.isNullOrEmpty()) {
            SimpleCallButton(event.speakerPhone!!, R.drawable.phone_forwarded, context)
        }
    }
}

@Composable
private fun RightActionButtons(event: CongregationEvent, context: Context, isWhatsAppInstalled: Boolean) {
    if (isWhatsAppInstalled && !event.speakerMobile.isNullOrBlank()) {
        SimpleWhatsAppButton(event, context)
    }
}

private const val CONTAINER_COLOR = 0xFF01C040

@Composable
private fun RowScope.SimpleCallButton(phoneNumber: String, iconRes: Int, context: Context) {
    FilledIconButton(
        modifier = Modifier.size(50.dp),
        shape = RoundedCornerShape(10.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = Color(CONTAINER_COLOR),
            contentColor = surfaceVariantLightHighContrast
        ),
        onClick = {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            context.startActivity(intent)
        }
    ) {
        Icon(
            modifier = Modifier.size(40.dp),
            imageVector = ImageVector.vectorResource(iconRes),
            contentDescription = null
        )
    }
}

@Suppress("SwallowedException")
@Composable
private fun SimpleWhatsAppButton(event: CongregationEvent, context: Context) {
    FilledIconButton(
        modifier = Modifier.size(50.dp),
        shape = RoundedCornerShape(10.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = Color(CONTAINER_COLOR),
            contentColor = surfaceVariantLightHighContrast
        ),
        onClick = {
            try {
                val normalizedNumber = event.speakerMobile
                    ?.replace(Regex("[^0-9]"), "")
                    ?.removePrefix("00") ?: return@FilledIconButton

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("smsto:$normalizedNumber"))
                val message =
                    "Hallo ${event.speakerName}, es geht um den Vortrag '${event.speechSubject}' " +
                        "am ${event.dateString}."
                intent.putExtra("sms_body", message)
                intent.setPackage("com.whatsapp")
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "WhatsApp konnte nicht geöffnet werden.", Toast.LENGTH_SHORT).show()
            }
        }
    ) {
        Icon(
            modifier = Modifier.size(40.dp),
            painter = painterResource(R.drawable.whatsapp_icon),
            contentDescription = null
        )
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
        onSelectCongregationEvent = { },
        stringProvider = AppEventStringProvider(context = LocalContext.current),
        isWhatsAppInstalled = true,
        onToggleShowPlanedItems = { },
        selectedShowPlanedItems = true
    )
}
