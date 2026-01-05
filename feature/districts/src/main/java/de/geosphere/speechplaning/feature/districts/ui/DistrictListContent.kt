package de.geosphere.speechplaning.feature.districts.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews

@Composable
fun DistrictListContent(
    districts: List<District>,
    onSelectDistrict: (District) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(districts, key = { it.id }) { district ->
            DistrictListItem(
                district = district,
                onLongClick = { onSelectDistrict(district) }
            )
        }
    }
}

@ThemePreviews
@Composable
private fun DistrictListContent_Preview() = SpeechPlaningTheme {
    DistrictListContent(
        districts = listOf(
            District(id = "1", name = "District 1"),
            District(id = "2", name = "District 2"),
            District(id = "3", name = "District 3")
        ),
        onSelectDistrict = {}
    )
}
