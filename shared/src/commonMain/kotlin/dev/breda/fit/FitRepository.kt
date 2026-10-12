package dev.breda.fit

import kotlinx.coroutines.flow.Flow

interface FitRepository {
    val data: Flow<FitData>
    suspend fun update(transform: (FitData) -> FitData)
    suspend fun reset()
}

object CsvExporter {
    fun measurements(d: FitData, dateLabel: String = "day_epoch", formatDate: (Long) -> String = { it.toString() }): String = buildString {
        append("$dateLabel,peso_kg,cintura_cm,gordura_percentual,massa_muscular_kg\n")
        d.measurements.sortedBy { it.day }.forEach { append("${formatDate(it.day)},${it.kg},${it.waistCm ?: ""},${it.fatPercent ?: ""},${it.muscleKg ?: ""}\n") }
    }
}
