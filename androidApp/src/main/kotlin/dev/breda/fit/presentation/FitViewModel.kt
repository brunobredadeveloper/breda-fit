package dev.breda.fit.presentation

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import dev.breda.fit.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

enum class Page { HOME, PLANS, PROGRESS, SETTINGS }
data class FitUiState(val loading: Boolean = true, val data: FitData = FitData(), val page: Page = Page.HOME, val session: SessionState? = null, val sessionId: String? = null, val sessionDay: Long? = null, val message: String? = null, val saving: Boolean = false, val storageFailure: Boolean = false)
class FitViewModel(private val repository: FitRepository) : ViewModel() {
    private val mutable = MutableStateFlow(FitUiState())
    val state = mutable.asStateFlow()
    private var timer: Job? = null
    private var lastTick = 0L
    private var observer: Job? = null
    val today: Long get() = LocalDate.now().toEpochDay()
    init { observe() }
    fun observe() {
        observer?.cancel()
        observer = viewModelScope.launch {
            try { repository.data.collect { d -> mutable.update { it.copy(loading = false, data = d, storageFailure = false) } } }
            catch (e: Exception) { if (e is CancellationException) throw e; mutable.update { it.copy(loading = false, storageFailure = true, message = "Não foi possível abrir seu histórico. Os dados foram preservados. Tente novamente.") } }
        }
    }
    fun page(page: Page) { mutable.update { it.copy(page = page) } }
    fun clearMessage() { mutable.update { it.copy(message = null) } }
    private fun write(block: suspend () -> Unit, success: String) {
        if (state.value.saving || state.value.storageFailure) return
        mutable.update { it.copy(saving = true) }
        viewModelScope.launch {
            try { block(); mutable.update { it.copy(saving = false, message = success) } }
            catch (e: Exception) { if (e is CancellationException) throw e; mutable.update { it.copy(saving = false, message = if (e is IllegalArgumentException) e.message ?: "Revise os valores informados." else "Não foi possível salvar. Verifique o espaço disponível e tente novamente.") } }
        }
    }
    fun setup(profile: Profile) {
        mutable.update { it.copy(page = Page.HOME) }
        write({ Validation.profile(profile); repository.update { it.copy(profile = profile, measurements = listOf(Measurement(today, profile.initialKg))) } }, "Seu plano está pronto.")
    }
    fun measurement(m: Measurement) { write({ repository.update { ProgressCalculator.withMeasurement(it, m) } }, "Medidas registradas.") }
    fun editProfile(profile: Profile) { write({ Validation.profile(profile); repository.update { it.copy(profile = profile) } }, "Perfil atualizado.") }
    fun preferences(p: Preferences) { write({ repository.update { it.copy(preferences = p) } }, "Preferências atualizadas.") }
    fun start(plan: Plan) {
        if (state.value.session != null) return
        mutable.update { it.copy(session = SessionEngine.start(plan), sessionId = UUID.randomUUID().toString(), sessionDay = today) }
        runTimer()
    }
    private fun runTimer() {
        timer?.cancel(); lastTick = SystemClock.elapsedRealtime()
        timer = viewModelScope.launch {
            while (true) {
                delay(100)
                val now = SystemClock.elapsedRealtime(); val delta = now - lastTick; lastTick = now
                mutable.update { s -> s.copy(session = s.session?.let { SessionEngine.tick(it, delta) }) }
                if (state.value.session?.phase == Phase.FINISHED) break
            }
        }
    }
    fun pause() { timer?.cancel(); mutable.update { it.copy(session = it.session?.copy(paused = true)) } }
    fun resume() {
        val current = state.value.session ?: return
        if (!current.paused || current.phase == Phase.FINISHED) return
        mutable.update { it.copy(session = it.session?.copy(paused = false)) }; runTimer()
    }
    fun discard() { timer?.cancel(); mutable.update { it.copy(session = null, sessionId = null, sessionDay = null) } }
    fun saveSession(effort: Int) {
        val ui = state.value; val s = ui.session ?: return
        if (s.phase != Phase.FINISHED) return
        write({
            repository.update { ProgressCalculator.withRecord(it, WorkoutRecord(requireNotNull(ui.sessionId), s.plan.id, requireNotNull(ui.sessionDay), s.activeMs / 1000, s.completed, s.plan.intervals.size, effort)) }
            discard()
        }, "Treino registrado. Muito bem!")
    }
    fun reset() {
        if (state.value.saving) return
        mutable.update { it.copy(saving = true) }
        viewModelScope.launch {
            try { repository.reset(); discard(); observe(); mutable.update { it.copy(saving = false, message = "Histórico apagado.") } }
            catch (e: Exception) { if (e is CancellationException) throw e; mutable.update { it.copy(saving = false, message = "Não foi possível apagar. Tente novamente.") } }
        }
    }
    companion object {
        fun factory(repository: FitRepository) = viewModelFactory {
            initializer { FitViewModel(repository) }
        }
    }
}
