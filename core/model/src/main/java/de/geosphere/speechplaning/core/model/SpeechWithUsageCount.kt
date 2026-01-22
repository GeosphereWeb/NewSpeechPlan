package de.geosphere.speechplaning.core.model

/**
 * Wrapper für eine Speech mit ihrem Verwendungszähler.
 * Zeigt an, wie oft die Rede in CongregationEvents bereits gehalten wurde.
 *
 * @property speech Die eigentliche Rede
 * @property timesUsed Die Anzahl der Male, die diese Rede in CongregationEvents verwendet wurde
 */
data class SpeechWithUsageCount(
    val speech: Speech,
    val timesUsed: Int = 0
)
