package de.geosphere.speechplaning.feature.congregationEvent

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.theme.ThemePreviews
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun MonthHeader(month: java.time.Month, year: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        val monthString = remember(month, year) {
            val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
            LocalDate.of(year, month, 1).format(formatter)
        }

        Text(
            text = monthString,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(
                start = 24.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 8.dp
            )
        )
    }
}

@ThemePreviews
@Composable
fun MonthHeaderPreview() {
    MonthHeader(month = java.time.Month.JANUARY, year = 2026)
}
