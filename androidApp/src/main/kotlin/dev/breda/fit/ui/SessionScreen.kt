package dev.breda.fit.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.breda.fit.*
import dev.breda.fit.presentation.*
import kotlin.math.roundToInt

@Composable fun SessionScreen(ui: FitUiState, vm: FitViewModel) {
    val s = requireNotNull(ui.session); val ex = Catalog.exercise(s.interval.exerciseId)
    var exit by rememberSaveable { mutableStateOf(false) }; var effort by rememberSaveable { mutableFloatStateOf(4f) }
    BackHandler { vm.pause(); exit = true }
    if (s.phase == Phase.FINISHED) {
        PageBody("Você se movimentou.\nIsso conta.", "Treino concluído") {
            Icon(FitIcons.Check, "Concluído", tint = Lime, modifier = Modifier.size(88.dp))
            FitCard { Text(s.plan.title, style = MaterialTheme.typography.titleLarge); Text("${s.completed} intervalos • ${s.activeMs / 60000} min em movimento", color = Mist) }
            Text("Como foi o esforço? ${effort.roundToInt()}/10", fontWeight = FontWeight.Bold)
            Slider(value = effort, onValueChange = { effort = it }, valueRange = 1f..10f, steps = 8)
            Text("1 = muito leve · 10 = máximo. Use sua percepção para adaptar o próximo treino.", color = Mist)
            Primary(if (ui.saving) "Registrando…" else "Salvar meu treino", !ui.saving) { vm.saveSession(effort.roundToInt()) }
            Text("O tempo registrado inclui só os intervalos ativos; preparação, pausas e descanso ficam de fora.", color = Mist, style = MaterialTheme.typography.bodySmall)
        }
    } else {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(s.plan.title, color = Mist)
            LinearProgressIndicator(progress = { s.completed.toFloat() / s.plan.intervals.size }, modifier = Modifier.fillMaxWidth(), color = Lime)
            Text(when { s.paused -> "PAUSADO"; s.phase == Phase.READY -> "PREPARE-SE"; s.phase == Phase.REST -> "RECUPERE O FÔLEGO"; else -> "MOVIMENTE-SE" }, color = if (s.phase == Phase.REST) Blue else Lime, letterSpacing = 2.sp)
            Text(if (s.phase == Phase.REST) "Respire e descanse" else ex.title, style = MaterialTheme.typography.headlineLarge)
            ExerciseArt(ex, !ui.data.preferences.reducedMotion && !s.paused && s.phase == Phase.WORK, Modifier.fillMaxWidth().height(260.dp))
            Text("${s.seconds}", fontSize = 76.sp, fontWeight = FontWeight.Bold, color = Lime)
            Text(if (s.phase == Phase.REST) "Próximo: ${Catalog.exercise(s.plan.intervals[s.index + 1].exerciseId).title}" else ex.cue, color = Mist)
            Text("Intervalo ${s.index + 1} de ${s.plan.intervals.size} · repetições no seu ritmo", color = Mist, style = MaterialTheme.typography.bodySmall)
            Button(onClick = { if (s.paused) vm.resume() else vm.pause() }, modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp)) {
                Icon(if (s.paused) FitIcons.Play else FitIcons.Pause, null); Spacer(Modifier.width(8.dp)); Text(if (s.paused) "Retomar" else "Pausar")
            }
            OutlinedButton(onClick = { vm.pause(); exit = true }, modifier = Modifier.fillMaxWidth()) { Text("Encerrar treino") }
            Text("Pare se sentir dor, tontura ou mal-estar. O cronômetro não deve guiar sua amplitude ou velocidade.", color = Mist, style = MaterialTheme.typography.bodySmall)
        }
    }
    if (exit) AlertDialog(onDismissRequest = { exit = false }, title = { Text("Encerrar esta sessão?") }, text = { Text("O treino fica pausado. Se encerrar agora, esta sessão não será registrada no histórico.") }, confirmButton = { TextButton(onClick = { exit = false; vm.discard() }) { Text("Encerrar sem registrar") } }, dismissButton = { TextButton(onClick = { exit = false }) { Text("Voltar ao treino") } })
}
