package dev.breda.fit.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.breda.fit.*
import dev.breda.fit.presentation.*
import kotlin.math.roundToInt

@Composable fun SettingsScreen(ui: FitUiState, vm: FitViewModel, export: () -> Unit) {
    val p = requireNotNull(ui.data.profile)
    var edit by rememberSaveable { mutableStateOf(false) }; var reset by rememberSaveable { mutableStateOf(false) }
    PageBody("Seu ritmo.\nSuas escolhas.", "Perfil / breda_dev") {
        FitCard { Text(p.name, style = MaterialTheme.typography.titleLarge); Text("Avatar ${p.avatar.label()} • ${p.genderIdentity.label()}",color=Mist); Text("${p.age} anos • ${decimal(p.heightCm)} cm", color = Mist); Text("Meta ${decimal(p.targetKg)} kg • ${p.weeklySessions} sessões por semana", color = Mist); OutlinedButton(onClick = { edit = true }) { Text("Editar perfil e metas") } }
        FitCard {
            Text("Experiência", style = MaterialTheme.typography.titleLarge)
            PreferenceRow("Reduzir movimento", "Exibe as ilustrações estáticas e remove transições.", ui.data.preferences.reducedMotion, !ui.saving) { vm.preferences(ui.data.preferences.copy(reducedMotion = it)) }
            PreferenceRow("Orientação por voz", "Usa a voz do Android, quando disponível. Pode exigir um pacote de idioma instalado.", ui.data.preferences.voice, !ui.saving) { vm.preferences(ui.data.preferences.copy(voice = it)) }
        }
        FitCard {
            Text("Privacidade e seus dados", style = MaterialTheme.typography.titleLarge)
            Text("Perfil, medidas e treinos ficam no armazenamento privado deste aparelho. Sem conta, anúncios ou envio para um servidor. Desinstalar o app remove o histórico.", color = Mist)
            OutlinedButton(onClick = export, enabled = ui.data.measurements.isNotEmpty(), modifier = Modifier.fillMaxWidth()) { Text("Exportar medidas (CSV)") }
            Text("O arquivo exportado contém medidas corporais. Escolha onde guardá-lo e com quem compartilhar.", color = Mist, style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { reset = true }) { Text("Apagar histórico e recomeçar", color = Coral) }
        }
        FitCard { Text("Feito pela breda_dev", fontWeight = FontWeight.Bold); Text("Breda Fit · 0.5.0", color = Lime); Text("Indicadores informativos, não uma avaliação clínica. As rotinas são genéricas e devem ser adaptadas. Calorias e resultados de emagrecimento não são prometidos.", color = Mist, style = MaterialTheme.typography.bodySmall) }
    }
    if (edit) EditProfileDialog(p, ui.saving, vm) { edit = false }
    if (reset) DeleteDialog(ui.saving, { reset = false }) { vm.reset(); reset = false }
}
@Composable private fun PreferenceRow(title: String, subtitle: String, checked: Boolean, enabled: Boolean, change: (Boolean) -> Unit) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold); Text(subtitle, color = Mist, style = MaterialTheme.typography.bodySmall) }; Switch(checked, change, enabled = enabled) }
}
@Composable private fun EditProfileDialog(p: Profile, saving: Boolean, vm: FitViewModel, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(p.name) }; var height by rememberSaveable { mutableStateOf(p.heightCm.toString()) }; var age by rememberSaveable { mutableStateOf(p.age.toString()) }; var target by rememberSaveable { mutableStateOf(p.targetKg.toString()) }; var weekly by rememberSaveable { mutableFloatStateOf(p.weeklySessions.toFloat()) }; var error by remember { mutableStateOf<String?>(null) }
    var avatar by rememberSaveable { mutableStateOf(p.avatar) }
    var identity by rememberSaveable { mutableStateOf(p.genderIdentity) }
    var description by rememberSaveable { mutableStateOf(p.genderDescription) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Perfil e metas") }, text = {
        Column(Modifier.heightIn(max = 450.dp).verticalScroll(androidx.compose.foundation.rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            IdentityFields(name,avatar,identity,description,{name=it},{avatar=it},{identity=it},{description=it}); Field("Idade", age) { age = it }; Field("Altura (cm)", height) { height = it }; Field("Meta (kg)", target) { target = it }
            Text("${weekly.roundToInt()} sessões/semana"); Slider(weekly, { weekly = it }, valueRange = 1f..7f, steps = 5)
            Text("Peso inicial preservado: ${decimal(p.initialKg)} kg. Atualize seu peso na aba Evolução.", color = Mist, style = MaterialTheme.typography.bodySmall)
            error?.let { Text(it, color = Coral) }
        }
    }, confirmButton = { TextButton(enabled = !saving, onClick = {
        try { val next = p.copy(name = name.trim(), age = age.toIntOrNull() ?: 0, heightCm = number(height) ?: 0.0, targetKg = number(target) ?: 0.0, weeklySessions = weekly.roundToInt(), avatar=avatar, genderIdentity=identity, genderDescription=description.trim()); Validation.profile(next); vm.editProfile(next); onDismiss() } catch (e: IllegalArgumentException) { error = e.message }
    }) { Text("Salvar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}
@Composable fun DeleteDialog(saving: Boolean, onDismiss: () -> Unit, delete: () -> Unit) {
    var confirmation by rememberSaveable { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Apagar todos os dados?") }, text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("Perfil, medidas e treinos serão apagados. Esta ação não pode ser desfeita. Digite APAGAR para confirmar."); Field("Confirmação", confirmation, false) { confirmation = it } } }, confirmButton = { TextButton(enabled = !saving && confirmation == "APAGAR", onClick = delete) { Text("Apagar", color = Coral) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}
@Composable fun FailureScreen(vm: FitViewModel) {
    var reset by rememberSaveable { mutableStateOf(false) }
    PageBody("Seu histórico\nfoi preservado.", "Não foi possível abrir") {
        Text("A leitura do armazenamento falhou. Tente novamente. Se o problema continuar, você pode escolher recomeçar, apagando os dados locais.", color = Mist)
        Primary("Tentar novamente") { vm.observe() }; OutlinedButton(onClick = { reset = true }) { Text("Apagar dados e recomeçar") }
    }
    if (reset) DeleteDialog(false, { reset = false }) { vm.reset(); reset = false }
}
