package dev.breda.fit.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.breda.fit.*
import dev.breda.fit.presentation.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

fun decimal(v: Double?): String = v?.let { String.format(Locale.forLanguageTag("pt-BR"), "%.1f", it) } ?: "—"
fun number(s: String): Double? = s.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }
fun date(day: Long): String = LocalDate.ofEpochDay(day).format(DateTimeFormatter.ofPattern("dd/MM/yy"))

@Composable fun FitApp(repository: FitRepository) {
    val vm: FitViewModel = viewModel(factory = FitViewModel.factory(repository))
    val ui by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val lifecycle = LocalLifecycleOwner.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showIntro by rememberSaveable { mutableStateOf(true) }
    var exportText by remember { mutableStateOf("") }
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) scope.launch {
            try {
                withContext(Dispatchers.IO) { requireNotNull(context.contentResolver.openOutputStream(uri)).use { it.write(exportText.toByteArray()) } }
                snackbar.showSnackbar("Medidas exportadas.")
            } catch (_: Exception) { snackbar.showSnackbar("Não foi possível exportar. Tente outro local.") }
        }
    }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) vm.pause() }
        lifecycle.lifecycle.addObserver(observer)
        onDispose { lifecycle.lifecycle.removeObserver(observer) }
    }
    val keepScreenAwake = ui.session?.let { !it.paused && it.phase != Phase.FINISHED } == true
    DisposableEffect(keepScreenAwake) {
        val window = (context as? ComponentActivity)?.window
        if (keepScreenAwake) window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
    LaunchedEffect(ui.message) { ui.message?.let { snackbar.showSnackbar(it); vm.clearMessage() } }
    BackHandler(ui.session == null && ui.data.profile != null && ui.page != Page.HOME) { vm.page(Page.HOME) }
    VoiceCoach(context, ui) { scope.launch { snackbar.showSnackbar("Voz não disponível. Verifique o mecanismo e o idioma de voz nas configurações do Android.") } }
    FitTheme {
        Scaffold(containerColor = Ink, snackbarHost = { SnackbarHost(snackbar) }, bottomBar = {
            if (!ui.loading && ui.data.profile != null && ui.session == null && !ui.storageFailure) {
                NavigationBar(containerColor = Panel) {
                    val icons = listOf(FitIcons.Home, FitIcons.Fitness, FitIcons.Chart, FitIcons.Settings)
                    val titles = listOf("Hoje", "Treinos", "Evolução", "Perfil")
                    Page.entries.forEachIndexed { i, page -> NavigationBarItem(selected = ui.page == page, onClick = { vm.page(page) }, icon = { Icon(icons[i], titles[i]) }, label = { Text(titles[i]) }) }
                }
            }
        }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when {
                    ui.loading -> LoadingScreen()
                    ui.storageFailure -> FailureScreen(vm)
                    ui.data.profile == null && showIntro -> Welcome { showIntro = false }
                    ui.data.profile == null -> ProfileForm(vm)
                    ui.session != null -> SessionScreen(ui, vm)
                    else -> AnimatedContent(ui.page, transitionSpec = { fadeIn(tween(if (ui.data.preferences.reducedMotion) 0 else 180)) togetherWith fadeOut(tween(if (ui.data.preferences.reducedMotion) 0 else 120)) }, label = "Page") { page ->
                        when (page) {
                            Page.HOME -> HomeScreen(ui, vm)
                            Page.PLANS -> PlansScreen(ui, vm)
                            Page.PROGRESS -> ProgressScreen(ui, vm)
                            Page.SETTINGS -> SettingsScreen(ui, vm) { exportText = CsvExporter.measurements(ui.data, "data") { LocalDate.ofEpochDay(it).toString() }; exporter.launch("breda-fit-medidas-${LocalDate.now()}.csv") }
                        }
                    }
                }
            }
        }
    }
}
@Composable private fun VoiceCoach(context: Context, ui: FitUiState, onUnavailable: () -> Unit) {
    var speaker by remember { mutableStateOf<TextToSpeech?>(null) }
    var ready by remember { mutableStateOf(false) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, speaker) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) speaker?.stop() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    DisposableEffect(ui.data.preferences.voice) {
        var engine: TextToSpeech? = null
        var disposed = false
        if (ui.data.preferences.voice) {
            engine = TextToSpeech(context) { status ->
                if (!disposed) {
                    ready = status == TextToSpeech.SUCCESS && (engine?.setLanguage(Locale.forLanguageTag("pt-BR")) ?: TextToSpeech.LANG_NOT_SUPPORTED) >= TextToSpeech.LANG_AVAILABLE
                    if (!ready) onUnavailable()
                }
            }
            speaker = engine
        }
        onDispose { disposed = true; engine?.stop(); engine?.shutdown(); speaker = null; ready = false }
    }
    LaunchedEffect(ui.session?.index, ui.session?.phase, ui.session?.paused, ready) {
        val s = ui.session
        if (s == null || s.paused) speaker?.stop()
        else if (ready) {
            val text = when (s.phase) { Phase.READY -> "Prepare-se. Comece no seu ritmo."; Phase.WORK -> Catalog.exercise(s.interval.exerciseId).title; Phase.REST -> "Descanse e respire."; Phase.FINISHED -> "Treino concluído. Muito bem." }
            speaker?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "phase")
        }
    }
}
@Composable fun PageBody(title: String, eyebrow: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(eyebrow.uppercase(), color = Lime, style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp)
        Text(title, style = MaterialTheme.typography.headlineLarge)
        content()
        Spacer(Modifier.height(12.dp))
    }
}
@Composable fun FitCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Panel)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}
@Composable fun Primary(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(16.dp)) { Text(text, fontWeight = FontWeight.Bold) }
}
@Composable private fun LoadingScreen() {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("breda_dev", color = Lime, fontWeight = FontWeight.Bold, fontSize = 26.sp); Spacer(Modifier.height(20.dp)); CircularProgressIndicator(color = Lime); Spacer(Modifier.height(20.dp)); Text("Preparando seu espaço de movimento", color = Mist)
    }
}
@Composable private fun Welcome(onContinue: () -> Unit) {
    PageBody("Mais movimento.\nNo seu ritmo.", "breda_dev / Breda Fit") {
        ExerciseArt(Catalog.exercise("march"), true, Modifier.fillMaxWidth().height(270.dp))
        Text("Seu treino começa em casa.", style = MaterialTheme.typography.titleLarge)
        Text("Treinos ilustrados, metas pessoais e um histórico que mostra sua evolução. Sem comparação com outras pessoas.", color = Mist)
        FitCard { Text("Você está no controle", fontWeight = FontWeight.Bold); Text("Escolha movimentos confortáveis, use apoios firmes e pare em caso de dor ou mal-estar. Se tiver uma condição ou limitação, adapte a rotina com um profissional.", color = Mist) }
        Primary("Criar meu plano", onClick = onContinue)
        Text("Para adultos • funciona offline • dados no aparelho", color = Mist, style = MaterialTheme.typography.bodySmall)
    }
}
@Composable private fun ProfileForm(vm: FitViewModel) {
    var name by rememberSaveable { mutableStateOf("") }; var age by rememberSaveable { mutableStateOf("") }; var height by rememberSaveable { mutableStateOf("") }; var weight by rememberSaveable { mutableStateOf("") }; var goal by rememberSaveable { mutableStateOf("") }; var weekly by rememberSaveable { mutableFloatStateOf(3f) }; var error by remember { mutableStateOf<String?>(null) }
    val ui by vm.state.collectAsStateWithLifecycle()
    PageBody("Um plano com\na sua medida.", "Começar") {
        Text("Sua meta é pessoal. O app acompanha o progresso; não promete prazo para perder peso.", color = Mist)
        Field("Como quer ser chamado?", name, false) { name = it }
        Field("Idade (20 anos ou mais)", age) { age = it }; Field("Altura (cm)", height) { height = it }
        Field("Peso inicial (kg)", weight) { weight = it }; Field("Meta de peso (kg)", goal) { goal = it }
        Text("${weekly.roundToInt()} sessões por semana", fontWeight = FontWeight.Bold)
        Slider(weekly, { weekly = it }, valueRange = 1f..7f, steps = 5)
        error?.let { Text(it, color = Coral) }
        Primary(if (ui.saving) "Salvando…" else "Começar minha jornada", !ui.saving) {
            try {
                val p = Profile(name.trim(), age.toIntOrNull() ?: 0, number(height) ?: 0.0, number(weight) ?: 0.0, number(goal) ?: 0.0, weekly.roundToInt(), vm.today)
                Validation.profile(p)
                vm.setup(p)
            } catch (e: IllegalArgumentException) { error = e.message }
        }
    }
}
@Composable fun Field(label: String, value: String, numeric: Boolean = true, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = { if (it.length <= 60) onChange(it) }, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp), keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text))
}
@Composable private fun HomeScreen(ui: FitUiState, vm: FitViewModel) {
    val stats = ProgressCalculator.calculate(ui.data, vm.today); val p = requireNotNull(ui.data.profile)
    PageBody("Vamos movimentar,\n${p.name}.", "Seu espaço / Hoje") {
        Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF284C37), Panel)), RoundedCornerShape(28.dp)).padding(22.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("UMA ROTINA. UM PASSO.", color = Lime, style = MaterialTheme.typography.labelLarge)
                Text("Comece no\nseu ritmo", style = MaterialTheme.typography.headlineLarge)
                ExerciseArt(Catalog.exercise("chair"), !ui.data.preferences.reducedMotion, Modifier.fillMaxWidth().height(180.dp))
                Text("${Catalog.plans[0].durationSeconds / 60} min • baixo impacto • cadeira", color = Mist)
                Primary("Ver treino de hoje") { vm.page(Page.PLANS) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Metric("Peso atual", decimal(stats.currentKg), "kg", Modifier.weight(1f))
            Metric("Últimos 7 dias", "${stats.weeklyCount}", "/ ${p.weeklySessions} sessões", Modifier.weight(1f))
        }
        FitCard {
            Text("Sua meta: ${decimal(p.targetKg)} kg", style = MaterialTheme.typography.titleLarge)
            LinearProgressIndicator(progress = { stats.goalFraction.toFloat() }, modifier = Modifier.fillMaxWidth(), color = Lime, trackColor = Color(0xFF3B4F43))
            Text("${(stats.goalFraction * 100).roundToInt()}% do caminho • variação ${decimal(stats.changeKg)} kg", color = Mist)
            Text("Peso varia de um dia para outro. Observe a tendência, não um único registro.", color = Mist, style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { vm.page(Page.PROGRESS) }) { Text("Registrar minhas medidas →") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Metric("Movimento", "${stats.activeMinutes}", "min ativos concluídos", Modifier.weight(1f)); Metric("Constância", "${stats.streakDays}", "dias seguidos", Modifier.weight(1f)) }
    }
}
@Composable fun Metric(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    FitCard(modifier) { Text(label, color = Mist, style = MaterialTheme.typography.bodySmall); Text(value, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Lime); Text(unit, color = Mist, style = MaterialTheme.typography.bodySmall) }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun PlansScreen(ui: FitUiState, vm: FitViewModel) {
    var selectedPlanId by rememberSaveable { mutableStateOf<String?>(null) }; var selectedExerciseId by rememberSaveable { mutableStateOf<String?>(null) }; var group by rememberSaveable { mutableStateOf("Todos") }
    val selectedPlan = Catalog.plans.find { it.id == selectedPlanId }
    val selectedExercise = Catalog.exercises.find { it.id == selectedExerciseId }
    PageBody("Treinar em casa.", "Biblioteca") {
        Text("Escolha uma rotina. A duração é um limite de tempo, não uma obrigação de completar repetições.", color = Mist)
        Catalog.plans.forEach { plan ->
            FitCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ExerciseArt(Catalog.exercise(plan.intervals.first().exerciseId), false, Modifier.size(90.dp))
                    Column(Modifier.weight(1f).padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) { Text(plan.title, fontWeight = FontWeight.Bold); Text(plan.subtitle, color = Mist, style = MaterialTheme.typography.bodySmall); Text("${plan.durationSeconds / 60} min • ${plan.intervals.size} intervalos", color = Lime) }
                }
                OutlinedButton(onClick = { selectedPlanId = plan.id }, modifier = Modifier.fillMaxWidth()) { Text("Conhecer o treino") }
            }
        }
        Text("Guia de movimentos", style = MaterialTheme.typography.titleLarge)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Todos", "Pernas", "Cardio", "Mobilidade", "Braços").forEach { g -> FilterChip(selected = group == g, onClick = { group = g }, label = { Text(g) }) }
        }
        Catalog.exercises.filter { group == "Todos" || it.group == group }.forEach { ex ->
            Card(onClick = { selectedExerciseId = ex.id }, colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) { ExerciseArt(ex, false, Modifier.size(85.dp)); Column(Modifier.padding(start = 16.dp).weight(1f)) { Text(ex.title, fontWeight = FontWeight.Bold); Text(ex.equipment, color = Mist); Text("Ver como fazer →", color = Lime, style = MaterialTheme.typography.bodySmall) } }
            }
        }
    }
    selectedPlan?.let { plan -> ModalBottomSheet(onDismissRequest = { selectedPlanId = null }, containerColor = Ink) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(plan.title, style = MaterialTheme.typography.headlineLarge); Text(plan.subtitle, color = Mist)
            Text("Prepare um espaço livre, água e o apoio indicado. Faça os movimentos devagar e pare se sentir dor.", color = Mist)
            plan.intervals.map { it.exerciseId }.distinct().forEach { id -> val ex = Catalog.exercise(id); TextButton(onClick = { selectedExerciseId = ex.id }) { Text("${ex.title} · ${ex.equipment}") } }
            Primary("Começar ${plan.durationSeconds / 60} minutos") { selectedPlanId = null; vm.start(plan) }; Spacer(Modifier.height(20.dp))
        }
    } }
    selectedExercise?.let { ex -> ModalBottomSheet(onDismissRequest = { selectedExerciseId = null }, containerColor = Ink) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(ex.title, style = MaterialTheme.typography.headlineLarge); ExerciseArt(ex, !ui.data.preferences.reducedMotion, Modifier.fillMaxWidth().height(250.dp)); PositionPair(ex)
            ex.instructions.forEachIndexed { i, text -> Text("${i + 1}. $text", color = Mist) }
            Text("Ilustração esquemática de orientação. Amplitude e ritmo devem ser adaptados ao seu conforto.", color = Mist, style = MaterialTheme.typography.bodySmall)
            Primary("Entendi") { selectedExerciseId = null }; Spacer(Modifier.height(20.dp))
        }
    } }
}
