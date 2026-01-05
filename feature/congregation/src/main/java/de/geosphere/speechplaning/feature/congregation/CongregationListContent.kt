package de.geosphere.speechplaning.feature.congregation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews

@Composable
fun CongregationListContent(
    congregations: List<Congregation>,
    allDistricts: List<District>,
    onSelectCongregation: (Congregation) -> Unit
) {
    // State für die Filter
    var districtFilter by remember { mutableStateOf("") }
    var congregationNameFilter by remember { mutableStateOf("") }
    var visibleFilter by remember { mutableStateOf(false) }

    // Gefilterte Liste
    val filteredCongregations = remember(congregations, allDistricts, districtFilter, congregationNameFilter) {
        congregations.filter { cong ->
            // Filter nach Versammlung Name
            val matchesCongName = if (congregationNameFilter.isBlank()) {
                true
            } else {
                cong.name.contains(congregationNameFilter, ignoreCase = true)
            }

            // Filter nach District Name
            val matchesDistrict = if (districtFilter.isBlank()) {
                true
            } else {
                val distName = allDistricts.find { it.id == cong.districtId }?.name ?: ""
                distName.contains(districtFilter, ignoreCase = true)
            }

            matchesCongName && matchesDistrict
        }
    }

    // Gruppieren
    val grouped = remember(filteredCongregations, allDistricts) {
        filteredCongregations
            .groupBy { it.districtId }
            .toList()
            .sortedBy { (districtId, _) ->
                allDistricts.find { it.id == districtId }?.name ?: districtId
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- Filter Bereich ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Button(onClick = { visibleFilter = visibleFilter.not() }) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Filter")
            }
            AnimatedVisibility(visibleFilter) {
                Column(modifier = Modifier.padding(8.dp)) {
                    OutlinedTextField(
                        value = districtFilter,
                        onValueChange = { districtFilter = it },
                        label = { Text("Filter: Kreis") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = congregationNameFilter,
                        onValueChange = { congregationNameFilter = it },
                        label = { Text("Filter: Versammlung") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            grouped.forEach { (districtId, congregationsInDistrict) ->
                val districtName = allDistricts.find { it.id == districtId }?.name ?: districtId

                stickyHeader { CongregationDistrictHeader(districtName = districtName) }

                items(congregationsInDistrict, key = { it.id.ifBlank { it.hashCode() } }) { congregation ->
                    CongregationListItem(
                        congregation = congregation,
                        districtName = districtName,
                        onClick = {
                            // Optional: Klick Verhalten definieren
                        },
                        onLongClick = { onSelectCongregation(congregation) }
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun CongregationListContentPreview() {
    SpeechPlaningTheme {
        CongregationListContent(
            congregations = listOf(
                Congregation(id = "1", name = "Congregation A", districtId = "1"),
                Congregation(id = "2", name = "Congregation B", districtId = "2"),
                Congregation(id = "3", name = "Congregation C", districtId = "1"),
            ),
            allDistricts = listOf(
                District(id = "1", name = "District 1"),
                District(id = "2", name = "District 2"),
            ),
            onSelectCongregation = {}
        )
    }
}
