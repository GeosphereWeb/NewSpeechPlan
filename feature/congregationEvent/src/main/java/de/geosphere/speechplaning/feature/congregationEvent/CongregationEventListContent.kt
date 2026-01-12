package de.geosphere.speechplaning.feature.congregationEvent

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PhoneForwarded
import androidx.compose.material.icons.automirrored.filled.SendToMobile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
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
    // 1. Sortiere die Liste einmal aufsteigend. Das ist wichtig für die Index-Suche.
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

    // NEU: State für die Höhe des Headers in Pixeln
    var yearHeaderHeightPx by remember { mutableIntStateOf(0) }
    // NEU: Density wird benötigt, um von Dp in Px umzurechnen, falls nötig
    val density = LocalDensity.current

    LaunchedEffect(sortedEvents, groupedEvents) { // Füge groupedEvents als Key hinzu

        // 1. var calculatedIndex = 0: Wir initialisieren einen Zähler für den LazyColumn-Index.
        // 2.groupedEvents.forEach { ... }: Wir iterieren durch die Datenstruktur in genau der gleichen Reihenfolge,
        // wie sie von der LazyColumn dargestellt wird.
        // 3.calculatedIndex++: Jedes Mal, wenn wir auf einen Header (Jahr oder Monat) stoßen, erhöhen wir unseren
        // Zähler.
        // 4. val indexInMonth = eventsInMonth.indexOf(targetEvent): Wir prüfen, ob unser Zielevent im aktuellen
        // Monatsblock enthalten ist.
        // 5.Wenn gefunden (indexInMonth != -1): Wir addieren den inneren Index (indexInMonth) zu unserem Gesamtzähler.
        // Jetzt haben wir den exakten Index des Items in der LazyColumn. Wir setzen targetFound = true, um die
        // Schleifen zu beenden.
        // 6.Wenn nicht gefunden: Wir addieren die gesamte Anzahl der Events dieses Monats (eventsInMonth.size) zu
        // unserem Zähler, da wir an ihnen "vorbeiscrollen" müssen.
        // 7.listState.animateScrollToItem(calculatedIndex): Zum Schluss rufen wir die Scroll-Funktion mit dem
        // korrekten, manuell berechneten Index auf.
        // 1. Finde das erste Event, das in der aktuellen Kalenderwoche liegt.
        val targetEvent = sortedEvents.find { it.date?.isInCurrentWeek() ?: false }

        if (targetEvent != null) {
            // --- HIER IST DIE KORREKTUR ---
            // 2. Berechne den korrekten Index für die LazyColumn manuell

            var calculatedIndex = 0
            var targetFound = false

            // Iteriere durch die gruppierte Struktur, genau wie die LazyColumn es tut
            groupedEvents.forEach { (year, eventsByMonth) ->
                if (targetFound) return@forEach // Schleife abbrechen, wenn Ziel gefunden

                calculatedIndex++ // Zähle den Jahres-Header

                eventsByMonth.forEach { (month, eventsInMonth) ->
                    if (targetFound) return@forEach // Schleife abbrechen

                    calculatedIndex++ // Zähle den Monats-Header

                    val indexInMonth = eventsInMonth.indexOf(targetEvent)
                    if (indexInMonth != -1) {
                        // Ziel-Event in diesem Monat gefunden!
                        calculatedIndex += indexInMonth
                        targetFound = true
                    } else {
                        // Wenn nicht in diesem Monat, zähle alle Events dieses Monats hinzu
                        calculatedIndex += eventsInMonth.size
                    }
                }
            }

            if (targetFound) {
                // 3. Starte die Coroutine zum Scrollen mit dem KORREKTEN Index und OFFSET.
                coroutineScope.launch {
                    listState.animateScrollToItem(
                        index = calculatedIndex,
                        // Schiebe das Item um die Höhe des Headers nach unten.
                        scrollOffset = -yearHeaderHeightPx
                    )
                }
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
                    MonthHeader(month = month, year = year)
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
                                        containerColor = Color(0xFF01C040), // Hintergrundfarbe
                                        contentColor = de.geosphere.speechplaning.theme.surfaceVariantLightHighContrast
                                        // Iconfarbe
                                    ),
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${event.speakerMobile}"))
                                        context.startActivity(intent)
                                    }
                                ) {
                                    Icon(
                                        modifier = Modifier.size(40.dp),
                                        imageVector = Icons.AutoMirrored.Filled.SendToMobile,
                                        contentDescription = null
                                    )
                                }
                            }
                            if (!event.speakerPhone.isNullOrEmpty()) {
                                FilledIconButton(
                                    modifier = Modifier.size(50.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = Color(0xFF01C040), // Hintergrundfarbe
                                        contentColor = de.geosphere.speechplaning.theme.surfaceVariantLightHighContrast
                                        // Iconfarbe
                                    ),
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${event.speakerPhone}"))
                                        context.startActivity(intent)
                                    }
                                ) {
                                    FilledIconButton(
                                        modifier = Modifier.size(50.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = Color(0xFF01C040), // Hintergrundfarbe
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
                                            imageVector = Icons.AutoMirrored.Filled.PhoneForwarded,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                        },
                        actionsRight = {
                            // 1. Prüfe, ob eine Telefonnummer vorhanden ist (Mobil oder Festnetz)
                            val rawPhoneNumber = event.speakerMobile?.takeIf { it.isNotBlank() }
                            if (isWhatsAppInstalled && !rawPhoneNumber.isNullOrBlank()) {
                                FilledIconButton(
                                    modifier = Modifier.size(50.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = Color(0xFF01C040), // Hintergrundfarbe
                                        contentColor = de.geosphere.speechplaning.theme.surfaceVariantLightHighContrast
                                    ),
                                    onClick = {
                                        // whatsapp soll sich mit mit der telefonnummer öffnen
                                        // --- HIER IST DIE NORMALISIERUNG ---
                                        // 2. Bereinige die Telefonnummer:
                                        //    - Entferne alle Zeichen, die keine Ziffern sind
                                        //    (Leerzeichen, +, -, /, etc.)
                                        //    - Ersetze eine führende "00" durch nichts (z.B. aus 0049 wird 49)
                                        val normalizedNumber = rawPhoneNumber
                                            .replace(Regex("[^0-9]"), "") // Entfernt alles außer Zahlen
                                            .removePrefix("00")              // Entfernt führende "00"

                                        // 3. Erstelle einen Intent mit der "smsto:" URI und der bereinigten Nummer
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("smsto:$normalizedNumber")
                                        )

                                        // 4. Optional: Füge eine vordefinierte Nachricht hinzu
                                        val message =
                                            "Hallo ${event.speakerName}, es geht um den Vortrag " +
                                                "'${event.speechSubject}' am ${event.dateString}."
                                        intent.putExtra("sms_body", message)

                                        // 5. Binde den Intent explizit an das WhatsApp-Paket
                                        intent.setPackage("com.whatsapp")

                                        // 6. Starte die Activity und fange mögliche Fehler ab
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // --- HIER IST DIE ÄNDERUNG ---
                                            // Zeige einen Toast, wenn WhatsApp nicht geöffnet werden konnte.
                                            // Das passiert z.B., wenn WhatsApp doch nicht installiert ist oder
                                            // ein anderer Fehler auftritt.
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
        stringProvider = AppEventStringProvider(context = LocalContext.current),
        isWhatsAppInstalled = true
    )
}
