package de.geosphere.speechplaning.feature.congregation

import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CongregationListItem(
    congregation: Congregation,
    districtName: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?
) {
    ListItem(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
        headlineContent = { Text(congregation.name) },
        supportingContent = {
            val displayDistrict = districtName.ifBlank { congregation.districtId }
            if (displayDistrict.isNotBlank() || congregation.address.isNotBlank()) {
                val sb = StringBuilder()
                if (displayDistrict.isNotBlank()) sb.append(displayDistrict)
                if (displayDistrict.isNotBlank() && congregation.address.isNotBlank()) sb.append(" - ")
                if (congregation.address.isNotBlank()) sb.append(congregation.address)
                Text(sb.toString())
            }
        },
        trailingContent = {
            if (!congregation.active) {
                Text("Inaktiv", color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@ThemePreviews
@Composable
private fun CongregationListItemPreview() {
    SpeechPlaningTheme {
        CongregationListItem(
            congregation = Congregation(
                id = "1",
                name = "Test Congregation",
                address = "123 Main St",
                active = true
            ),
            districtName = "Test District",
            onClick = {},
            onLongClick = {}
        )
    }
}

@ThemePreviews
@Composable
private fun CongregationListItemInactivePreview() {
    SpeechPlaningTheme {
        CongregationListItem(
            congregation = Congregation(
                id = "2",
                name = "Inactive Congregation",
                address = "456 Side St",
                active = false
            ),
            districtName = "Test District 2",
            onClick = {},
            onLongClick = {}
        )
    }
}
