package de.geosphere.speechplaning.core.model.data

import de.geosphere.speechplaning.core.model.Speech

/**
 * Wrapper für eine Speech mit ihrer vollständigen Verwendungshistorie.
 * Zeigt an, wie oft die Rede gehalten wurde und wann/von wem sie zuletzt gehalten wurde.
 *
 * @property speech Die eigentliche Rede
 * @property timesUsed Die Anzahl der Male, die diese Rede in CongregationEvents verwendet wurde
 * @property usageHistory Eine Liste der Verwendungsdetails (Datum, Redner), sortiert nach neuesten zuerst
 */
data class SpeechWithUsageHistory(
    val speech: Speech,
    val timesUsed: Int = 0,
    val usageHistory: List<SpeechUsageDetail> = emptyList()
)

data class SpeechUsageDetail(
    val dateString: String = "",
    val speakerName: String = ""
)
