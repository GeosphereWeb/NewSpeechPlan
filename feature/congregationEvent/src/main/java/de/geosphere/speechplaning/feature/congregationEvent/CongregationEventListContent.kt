package de.geosphere.speechplaning.feature.congregationEvent

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.core.model.data.Event
import de.geosphere.speechplaning.core.ui.provider.AppEventStringProvider
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews
import java.time.Month

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CongregationEventListContent(
    congregationEvents: List<CongregationEvent>,
    onSelectCongregationEvent: (CongregationEvent) -> Unit,
    stringProvider: AppEventStringProvider,
    isWhatsAppInstalled: Boolean
) {
    val groupedEvents = remember(congregationEvents) {
        congregationEvents
            .sortedBy { it.date }
            .groupBy { it.date?.year ?: 0 }
            .mapValues { entry ->
                entry.value.groupBy { it.date?.month ?: Month.JANUARY }
            }
    }
    val context = LocalContext.current

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
                                            contentColor = de.geosphere.speechplaning.theme.surfaceVariantLightHighContrast
                                        ),
                                        onClick = {
                                            val intent =
                                                Intent(Intent.ACTION_DIAL, Uri.parse("tel:${event.speakerPhone}"))
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
                                        //    - Entferne alle Zeichen, die keine Ziffern sind (Leerzeichen, +, -, /, etc.)
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
                                            "Hallo ${event.speakerName}, es geht um den Vortrag '${event.speechSubject}' am ${event.dateString}."
                                        intent.putExtra("sms_body", message)

                                        // 5. Binde den Intent explizit an das WhatsApp-Paket
                                        intent.setPackage("com.whatsapp")

                                        // 6. Starte die Activity und fange mögliche Fehler ab
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // --- HIER IST DIE ÄNDERUNG ---
                                            // Zeige einen Toast, wenn WhatsApp nicht geöffnet werden konnte.
                                            // Das passiert z.B., wenn WhatsApp doch nicht installiert ist oder ein anderer Fehler auftritt.
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
                                        painter = painterResource(de.geosphere.speechplaning.theme.R.drawable.whatsapp_icon),
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
