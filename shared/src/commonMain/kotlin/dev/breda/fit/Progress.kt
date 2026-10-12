package dev.breda.fit

object Validation {
    fun profile(p: Profile) {
        require(p.name.trim().length in 1..40) { "Informe seu nome (até 40 caracteres)." }
        require(p.age in 20..100) { "Este app é destinado a adultos a partir de 20 anos." }
        require(p.heightCm.isFinite() && p.heightCm in 80.0..250.0) { "Altura deve estar entre 80 e 250 cm." }
        require(p.initialKg.isFinite() && p.initialKg in 20.0..400.0) { "Informe um peso válido, em kg." }
        require(p.targetKg.isFinite() && p.targetKg in 20.0..p.initialKg) { "Meta deve ser positiva e não maior que o peso inicial." }
        require(p.targetKg / ((p.heightCm / 100) * (p.heightCm / 100)) >= 18.5) { "Essa meta pode resultar em baixo peso pelo IMC. Defina uma meta com orientação profissional." }
        require(p.createdDay in 0..3652059)
        require(p.weeklySessions in 1..7) { "Escolha entre 1 e 7 sessões por semana." }
    }
    fun measurement(m: Measurement) {
        require(m.day in 0..3652059)
        require(m.kg.isFinite() && m.kg in 20.0..400.0) { "Informe um peso válido, em kg." }
        require(m.waistCm == null || (m.waistCm.isFinite() && m.waistCm in 30.0..300.0)) { "Cintura deve estar entre 30 e 300 cm." }
        require(m.fatPercent == null || (m.fatPercent.isFinite() && m.fatPercent in 1.0..75.0)) { "Percentual de gordura deve estar entre 1 e 75%." }
        require(m.muscleKg == null || (m.muscleKg.isFinite() && m.muscleKg > 0 && m.muscleKg <= m.kg)) { "Massa muscular deve ser positiva e menor que o peso." }
        if (m.muscleKg != null && m.fatPercent != null) require(m.muscleKg <= m.kg * (1 - m.fatPercent / 100)) { "Revise os valores: a massa muscular não pode exceder a massa livre de gordura." }
    }
    fun data(d: FitData) {
        require(d.schema == 1) { "Versão de dados incompatível. Nenhum dado foi apagado." }
        d.profile?.let(::profile)
        require(d.measurements.size <= 2000 && d.workouts.size <= 2000) { "Limite de histórico atingido. Exporte seus dados antes de iniciar um novo histórico." }
        d.measurements.forEach(::measurement)
        require(d.measurements.map { it.day }.distinct().size == d.measurements.size)
        require(d.workouts.map { it.id }.distinct().size == d.workouts.size)
        d.workouts.forEach { require(it.id.isNotBlank() && it.day in 0..3652059 && it.activeSeconds in 0..86400 && it.totalIntervals in 1..1000 && it.completedIntervals in 0..it.totalIntervals && it.effort in 1..10) }
    }
}

data class ProgressStats(val currentKg: Double?, val bmi: Double?, val changeKg: Double?, val goalFraction: Double, val fatKg: Double?, val leanKg: Double?, val weeklyCount: Int, val activeMinutes: Long, val streakDays: Int)
object ProgressCalculator {
    fun calculate(d: FitData, today: Long): ProgressStats {
        val p = d.profile
        val latest = d.measurements.filter { it.day <= today }.maxByOrNull { it.day }
        val kg = latest?.kg ?: p?.initialKg
        val bmi = if (kg != null && p != null) kg / ((p.heightCm / 100) * (p.heightCm / 100)) else null
        val goal = if (p == null || kg == null) 0.0 else if (p.initialKg == p.targetKg) if (kg <= p.targetKg) 1.0 else 0.0 else ((p.initialKg - kg) / (p.initialKg - p.targetKg)).coerceIn(0.0, 1.0)
        val fat = latest?.fatPercent?.let { latest.kg * it / 100 }
        val completed = d.workouts.filter { it.completedIntervals == it.totalIntervals && it.totalIntervals > 0 && it.day <= today }
        val days = completed.map { it.day }.toSet()
        var cursor = if (today in days) today else today - 1
        var streak = 0
        while (cursor in days) { streak++; cursor-- }
        return ProgressStats(kg, bmi, if (p != null && kg != null) kg - p.initialKg else null, goal, fat, fat?.let { latest.kg - it }, completed.count { it.day >= today - 6 }, completed.sumOf { it.activeSeconds } / 60, streak)
    }
    fun withMeasurement(d: FitData, m: Measurement): FitData {
        Validation.measurement(m)
        return d.copy(measurements = (d.measurements.filterNot { it.day == m.day } + m).sortedBy { it.day }).also(Validation::data)
    }
    fun withRecord(d: FitData, r: WorkoutRecord): FitData = if (d.workouts.any { it.id == r.id }) d else d.copy(workouts = d.workouts + r).also(Validation::data)
}
