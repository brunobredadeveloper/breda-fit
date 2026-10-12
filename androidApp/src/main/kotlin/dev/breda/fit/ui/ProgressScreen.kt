package dev.breda.fit.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.breda.fit.*
import dev.breda.fit.presentation.*
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ProgressScreen(ui: FitUiState, vm: FitViewModel) {
    var showForm by rememberSaveable { mutableStateOf(false) }; var metric by rememberSaveable { mutableStateOf("Peso") }
    val stats = ProgressCalculator.calculate(ui.data, vm.today)
    val latest = ui.data.measurements.filter { it.day <= vm.today }.maxByOrNull { it.day }
    PageBody("Evolução de verdade.", "Meu histórico") {
        Primary("Registrar medidas de hoje") { showForm = true }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Peso", "Cintura", "Gordura", "Músculo").forEach { m ->
                FilterChip(selected = metric == m, onClick = { metric = m }, label = { Text(m) })
            }
        }
        val values = ui.data.measurements.filter { it.day <= vm.today }.sortedBy { it.day }.takeLast(30).mapNotNull { m -> (when (metric) { "Peso" -> m.kg; "Cintura" -> m.waistCm; "Gordura" -> m.fatPercent; else -> m.muscleKg })?.let { m.day to it } }
        FitCard {
            Text("Últimos ${values.size} registros • ${when (metric) { "Cintura" -> "cm"; "Gordura" -> "%"; else -> "kg" }}", style = MaterialTheme.typography.titleLarge)
            WeightChart(values)
            if (values.size >= 2) Text("${date(values.first().first)}: ${decimal(values.first().second)} → ${date(values.last().first)}: ${decimal(values.last().second)}", color = Mist)
            else Text("Registre em dias diferentes para ver a tendência.", color = Mist)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Metric("Peso atual", decimal(stats.currentKg), "kg", Modifier.weight(1f)); Metric("IMC", decimal(stats.bmi), "indicador de triagem", Modifier.weight(1f)) }
        Text("IMC = peso ÷ altura². Não mede gordura nem massa muscular e pode ser inadequado em situações individuais. Não é um diagnóstico.", color = Mist, style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Metric("Cintura", decimal(latest?.waistCm), "cm informados", Modifier.weight(1f)); Metric("Gordura", decimal(latest?.fatPercent), "% informado", Modifier.weight(1f)) }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Metric("Massa de gordura", decimal(stats.fatKg), "kg calculados", Modifier.weight(1f)); Metric("Massa livre de gordura", decimal(stats.leanKg), "kg calculados", Modifier.weight(1f)) }
        FitCard { Text("Massa muscular: ${decimal(latest?.muscleKg)} kg"); Text("Informe o valor medido, por exemplo em uma avaliação. Massa livre de gordura também inclui água, ossos e órgãos: não equivale a músculo.", color = Mist, style = MaterialTheme.typography.bodySmall) }
        Text("Treinos registrados", style = MaterialTheme.typography.titleLarge)
        if (ui.data.workouts.isEmpty()) FitCard { Text("Seu histórico começa no primeiro treino."); Text("Escolha uma rotina e registre ao concluir.", color = Mist) }
        ui.data.workouts.sortedByDescending { it.day }.take(20).forEach { r -> FitCard {
            Text(Catalog.plans.find { it.id == r.planId }?.title ?: "Treino", style = MaterialTheme.typography.titleMedium)
            Text("${date(r.day)} • ${r.activeSeconds / 60} min ativos • esforço ${r.effort}/10", color = Mist)
        } }
        Text("Medidas recentes", style = MaterialTheme.typography.titleLarge)
        ui.data.measurements.sortedByDescending { it.day }.take(15).forEach { m -> Text("${date(m.day)}  ·  ${decimal(m.kg)} kg  ·  cintura ${decimal(m.waistCm)} cm", color = Mist) }
    }
    if (showForm) MeasurementDialog(ui, vm) { showForm = false }
}
@Composable fun WeightChart(values: List<Pair<Long, Double>>) {
    val description = if (values.isEmpty()) "Nenhum registro disponível" else "Gráfico com ${values.size} registros. Primeiro: ${decimal(values.first().second)}, último: ${decimal(values.last().second)}."
    Canvas(Modifier.fillMaxWidth().height(180.dp).semantics { contentDescription = description }) {
        if (values.isEmpty()) return@Canvas
        val min = values.minOf { it.second } - 0.5; val range = max(1.0, values.maxOf { it.second } - min + 0.5)
        val dayMin = values.first().first; val dayRange = max(1L, values.last().first - dayMin)
        repeat(4) { i -> val y = 12f + (size.height - 24f) * i / 3; drawLine(Mist.copy(alpha = 0.16f), Offset(8f, y), Offset(size.width - 8f, y)) }
        val points = values.map { (day, v) -> Offset(12f + (day - dayMin).toFloat() / dayRange * (size.width - 24f), size.height - 12f - ((v - min) / range).toFloat() * (size.height - 24f)) }
        val path = Path(); points.forEachIndexed { i, p -> if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y) }
        drawPath(path, Lime, style = Stroke(width = 4f)); points.forEach { drawCircle(Lime, 5f, it) }
    }
}
@Composable private fun MeasurementDialog(ui: FitUiState, vm: FitViewModel, onDismiss: () -> Unit) {
    val existing = ui.data.measurements.find { it.day == vm.today }
    var kg by rememberSaveable { mutableStateOf(existing?.kg?.toString() ?: "") }; var waist by rememberSaveable { mutableStateOf(existing?.waistCm?.toString() ?: "") }; var fat by rememberSaveable { mutableStateOf(existing?.fatPercent?.toString() ?: "") }; var muscle by rememberSaveable { mutableStateOf(existing?.muscleKg?.toString() ?: "") }; var error by remember { mutableStateOf<String?>(null) }
    fun optional(s: String): Double? { if (s.isBlank()) return null; return requireNotNull(number(s)) { "Revise os valores opcionais." } }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (existing == null) "Medidas de hoje" else "Atualizar medidas de hoje") }, text = {
        Column(Modifier.heightIn(max = 440.dp).verticalScroll(androidx.compose.foundation.rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Um registro por dia. Valores opcionais podem ficar em branco.", color = Mist)
            Field("Peso (kg)", kg) { kg = it }; Field("Cintura (cm, opcional)", waist) { waist = it }; Field("Gordura (%, opcional)", fat) { fat = it }; Field("Massa muscular (kg, opcional)", muscle) { muscle = it }
            error?.let { Text(it, color = Coral) }
        }
    }, confirmButton = { TextButton(enabled = !ui.saving, onClick = {
        try { val m = Measurement(vm.today, number(kg) ?: 0.0, optional(waist), optional(fat), optional(muscle)); Validation.measurement(m); vm.measurement(m); onDismiss() } catch (e: IllegalArgumentException) { error = e.message }
    }) { Text("Salvar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}
