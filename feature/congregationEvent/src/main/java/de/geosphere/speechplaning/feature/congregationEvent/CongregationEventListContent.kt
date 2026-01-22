package de.geosphere.speechplaning.feature.congregationEvent

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
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
import kotlinx.coroutines.launch
import java.time.Month

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CongregationEventListContent(
    congregationEvents: List<CongregationEvent>,
    onSelectCongregationEvent: (CongregationEvent) -> Unit,
    stringProvider: AppEventStringProvider,
    isWhatsAppInstalled: Boolean
) {
    var initialScrollDone by rememberSaveable { mutableStateOf(false) }

    val sortedEvents = remember(congregationEvents) {
        congregationEvents.sortedBy { it.date }
    }

    val groupedEvents = remember(sortedEvents) {
        sortedEvents
            .groupBy { it.date?.year ?: 0 }
            .mapValues { entry ->
                entry.value.groupBy { it.date?.month ?: Month.JANUARY }
            }
    }

    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var yearHeaderHeightPx by remember { mutableIntStateOf(0) }
    var monthHeaderHeightPx by remember { mutableIntStateOf(0) }

    fun scrollToCurrentWeek(animated: Boolean = true) {
        val targetEvent = sortedEvents.find { it.date?.isInCurrentWeek() ?: false }

        if (targetEvent != null) {
            var calculatedIndex = 0
            var targetFound = false

            groupedEvents.forEach { (year, eventsByMonth) ->
                if (targetFound) return@forEach

                calculatedIndex++ // Year header

                eventsByMonth.forEach { (month, eventsInMonth) ->
                    if (targetFound) return@forEach

                    calculatedIndex++ // Month header

                    val indexInMonth = eventsInMonth.indexOf(targetEvent)
                    if (indexInMonth != -1) {
                        calculatedIndex += indexInMonth
                        targetFound = true
                    } else {
                        calculatedIndex += eventsInMonth.size
                    }
                }
            }

            if (targetFound) {
                coroutineScope.launch {
                    if (animated) {
                        listState.animateScrollToItem(
                            index = calculatedIndex,
                            scrollOffset = -yearHeaderHeightPx
                        )
                    } else {
                        listState.scrollToItem(
                            index = calculatedIndex,
                            scrollOffset = -yearHeaderHeightPx
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(sortedEvents, yearHeaderHeightPx, monthHeaderHeightPx) {
        if (!initialScrollDone && sortedEvents.isNotEmpty() && yearHeaderHeightPx > 0 && monthHeaderHeightPx > 0) {
            scrollToCurrentWeek(false)
            initialScrollDone = true
        }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { scrollToCurrentWeek(true) },

                ) {
                Row(modifier = Modifier) {
                    Text(text = "Scroll to...")
                    Icon(imageVector = ImageVector.vectorResource(R.drawable.today), contentDescription = null)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            groupedEvents.forEach { (year, eventsByMonth) ->
                stickyHeader {
                    YearHeader(
                        year = year,
                        modifier = Modifier.onSizeChanged { size ->
                            yearHeaderHeightPx = size.height
                        }
                    )
                }

                eventsByMonth.forEach { (month, eventsInMonth) ->
                    stickyHeader {
                        MonthHeader(
                            month = month,
                            year = year,
                            modifier = Modifier.onSizeChanged { size ->
                                monthHeaderHeightPx = size.height
                            }
                        )
                    }

                    items(eventsInMonth, key = { it.id.ifBlank { it.hashCode() } }) { event ->
                        SwipeableItemWithActions(
                            isRevealed = false,
                            actionsLeft = {
                                if (!event.speakerMobile.isNullOrEmpty()) {
                                    FilledIconButton(
                                        modifier = Modifier.size(50.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = Color(0xFF01C040),
                                            contentColor = de.geosphere.speechplaning.theme.surfaceVariantLightHighContrast
                                        ),
                                        onClick = {
                                            val intent =
                                                Intent(Intent.ACTION_DIAL, Uri.parse("tel:${event.speakerMobile}"))
                                            context.startActivity(intent)
                                        }
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(40.dp),
                                            imageVector = ImageVector.vectorResource(R.drawable.send_to_mobile),
                                            contentDescription = null
                                        )
                                    }
                                }
                                if (!event.speakerPhone.isNullOrEmpty()) {
                                    FilledIconButton(
                                        modifier = Modifier.size(50.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = Color(0xFF01C040),
                                            contentColor = de.geosphere.speechplaning.theme.surfaceVariantLightHighContrast
                                        ),
                                        onClick = {
                                            val intent =
                                                Intent(Intent.ACTION_DIAL, Uri.parse("tel:${event.speakerPhone}"))
                                            context.startActivity(intent)
                                        }
                                    ) {
                                        FilledIconButton(
                                            modifier = Modifier.size(50.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = IconButtonDefaults.filledIconButtonColors(
                                                containerColor = Color(0xFF01C040),
                                                contentColor = surfaceVariantLightHighContrast
                                            ),
                                            onClick = {
                                                val intent =
                                                    Intent(
                                                        Intent.ACTION_DIAL,
                                                        Uri.parse("tel:${event.speakerPhone}")
                                                    )
                                                context.startActivity(intent)
                                            }
                                        ) {
                                            Icon(
                                                modifier = Modifier.size(40.dp),
                                                imageVector = ImageVector.vectorResource(R.drawable.phone_forwarded),
                                                contentDescription = null
                                            )
                                        }
                                    }
                                }
                            },
                            actionsRight = {
                                val rawPhoneNumber = event.speakerMobile?.takeIf { it.isNotBlank() }
                                if (isWhatsAppInstalled && !rawPhoneNumber.isNullOrBlank()) {
                                    FilledIconButton(
                                        modifier = Modifier.size(50.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = Color(0xFF01C040),
                                            contentColor = de.geosphere.speechplaning.theme.surfaceVariantLightHighContrast
                                        ),
                                        onClick = {
                                            val normalizedNumber = rawPhoneNumber
                                                .replace(Regex("[^0-9]"), "")
                                                .removePrefix("00")

                                            val intent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("smsto:$normalizedNumber")
                                            )

                                            val message =
                                                "Hallo ${event.speakerName}, es geht um den Vortrag " +
                                                    "'${event.speechSubject}' am ${event.dateString}."
                                            intent.putExtra("sms_body", message)

                                            intent.setPackage("com.whatsapp")

                                            try {
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(
                                                    context,
                                                    "WhatsApp konnte nicht geöffnet werden.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
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
                            },
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
        onSelectCongregationEvent = { },
        stringProvider = AppEventStringProvider(context = LocalContext.current),
        isWhatsAppInstalled = true
    )
}
