package de.geosphere.speechplaning.feature.districts.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews

@Composable
fun DistrictListItem(district: District, onLongClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(district.name) },
        modifier = Modifier.combinedClickable(
            onClick = {},
            onLongClick = onLongClick
        )
    )
}

@ThemePreviews
@Composable
private fun DistrictListItem_Preview() = SpeechPlaningTheme {
    DistrictListItem(
        district = District(id = "123", name = "Test District"),
        onLongClick = {}
    )
}
