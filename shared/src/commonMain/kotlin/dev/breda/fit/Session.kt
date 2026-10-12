package dev.breda.fit

enum class Phase { READY, WORK, REST, FINISHED }
data class SessionState(val plan: Plan, val index: Int = 0, val phase: Phase = Phase.READY, val remainingMs: Long = 5000, val activeMs: Long = 0, val completed: Int = 0, val paused: Boolean = false) {
    val interval: Interval get() = plan.intervals[index.coerceAtMost(plan.intervals.lastIndex)]
    val seconds: Int get() = ((remainingMs + 999) / 1000).toInt()
}
/** Pure reducer. Elapsed monotonic time is supplied by the platform; paused time is never counted. */
object SessionEngine {
    fun start(plan: Plan): SessionState { require(plan.intervals.isNotEmpty()); require(plan.intervals.all { it.workSeconds > 0 && it.restSeconds >= 0 }); return SessionState(plan) }
    fun tick(initial: SessionState, elapsedMs: Long): SessionState {
        require(elapsedMs >= 0)
        if (initial.paused || initial.phase == Phase.FINISHED) return initial
        var state = initial; var delta = elapsedMs
        while (delta > 0 && state.phase != Phase.FINISHED) {
            val used = minOf(delta, state.remainingMs)
            state = state.copy(remainingMs = state.remainingMs - used, activeMs = state.activeMs + if (state.phase == Phase.WORK) used else 0)
            delta -= used
            if (state.remainingMs == 0L) state = next(state)
        }
        return state
    }
    private fun next(s: SessionState): SessionState = when (s.phase) {
        Phase.READY -> s.copy(phase = Phase.WORK, remainingMs = s.interval.workSeconds * 1000L)
        Phase.WORK -> if (s.index == s.plan.intervals.lastIndex) s.copy(phase = Phase.FINISHED, remainingMs = 0, completed = s.completed + 1) else if (s.interval.restSeconds == 0) s.copy(index = s.index + 1, remainingMs = s.plan.intervals[s.index + 1].workSeconds * 1000L, completed = s.completed + 1) else s.copy(phase = Phase.REST, remainingMs = s.interval.restSeconds * 1000L, completed = s.completed + 1)
        Phase.REST -> s.copy(index = s.index + 1, phase = Phase.WORK, remainingMs = s.plan.intervals[s.index + 1].workSeconds * 1000L)
        Phase.FINISHED -> s
    }
}
