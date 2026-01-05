package de.geosphere.speechplaning.feature.congregation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews

@Composable
fun CongregationDistrictHeader(districtName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant) // Hintergrundfarbe zur Abhebung
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Text(
            text = if (districtName.isBlank()) "Ohne Kreiszuordnung" else "Kreis $districtName",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
    }
}

@ThemePreviews
@Composable
private fun CongregationDistrictHeaderPreview() {
    SpeechPlaningTheme {
        CongregationDistrictHeader(districtName = "Test-Kreis")
    }
}

@ThemePreviews
@Composable
private fun CongregationDistrictHeaderEmptyPreview() {
    SpeechPlaningTheme {
        CongregationDistrictHeader(districtName = "")
    }
}
