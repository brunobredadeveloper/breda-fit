package dev.breda.fit

import kotlin.test.*

class FitTests {
    private val profile = Profile("Ana", 32, 170.0, 80.0, 70.0, 3, 100)
    @Test fun bmiAndGoalUseActualMeasurements() {
        val d = FitData(profile = profile, measurements = listOf(Measurement(101, 75.0, fatPercent = 20.0)))
        val s = ProgressCalculator.calculate(d, 101)
        assertEquals(75.0 / 2.89, s.bmi!!, 0.00001); assertEquals(0.5, s.goalFraction); assertEquals(15.0, s.fatKg); assertEquals(60.0, s.leanKg)
    }
    @Test fun noBodyFatIsInvented() { val s = ProgressCalculator.calculate(FitData(profile = profile), 100); assertNull(s.fatKg); assertNull(s.leanKg) }
    @Test fun recordsOnSameDayReplaceInsteadOfDuplicate() { val d = ProgressCalculator.withMeasurement(FitData(measurements = listOf(Measurement(100, 80.0))), Measurement(100, 79.0)); assertEquals(1, d.measurements.size); assertEquals(79.0, d.measurements.single().kg) }
    @Test fun rejectsInvalidNumbers() { assertFailsWith<IllegalArgumentException> { Validation.measurement(Measurement(100, Double.NaN)) }; assertFailsWith<IllegalArgumentException> { Validation.measurement(Measurement(100, 70.0, fatPercent = 110.0)) }; assertFailsWith<IllegalArgumentException> { Validation.profile(profile.copy(heightCm = 0.0)) } }
    @Test fun pausedTimeIsNeverCounted() { val s = SessionEngine.start(Plan("test", "Test", "", listOf(Interval("march", 10, 5)))).copy(paused = true); assertEquals(s, SessionEngine.tick(s, 99999)) }
    @Test fun exactTransitionsCountOnlyMovement() { val p = Plan("test", "Test", "", listOf(Interval("march", 10, 5), Interval("calf", 10, 0))); var s = SessionEngine.start(p); s = SessionEngine.tick(s, 5000); assertEquals(Phase.WORK, s.phase); s = SessionEngine.tick(s, 15000); assertEquals(Phase.WORK, s.phase); assertEquals(1, s.index); assertEquals(10000, s.activeMs); s = SessionEngine.tick(s, 10000); assertEquals(Phase.FINISHED, s.phase); assertEquals(20000, s.activeMs); assertEquals(2, s.completed) }
    @Test fun largeDeltaSafelyTraversesAllPhases() { val p = Catalog.plans.first(); val s = SessionEngine.tick(SessionEngine.start(p), p.durationSeconds * 1000L + 9999); assertEquals(Phase.FINISHED, s.phase); assertEquals(p.intervals.sumOf { it.workSeconds } * 1000L, s.activeMs) }
    @Test fun noRestOnFinalInterval() { val p = Plan("test", "Test", "", listOf(Interval("march", 10, 500))); assertEquals(15, p.durationSeconds); assertEquals(Phase.FINISHED, SessionEngine.tick(SessionEngine.start(p), 15000).phase) }
    @Test fun duplicateSaveIsIdempotent() { val r = WorkoutRecord("id", "start", 100, 60, 1, 1, 4); val once = ProgressCalculator.withRecord(FitData(), r); assertEquals(once, ProgressCalculator.withRecord(once, r)) }
    @Test fun weeklyAndStreakCountRealDays() { val records = listOf(98L, 99L, 100L).map { WorkoutRecord("$it", "start", it, 120, 1, 1, 4) }; val s = ProgressCalculator.calculate(FitData(workouts = records), 101); assertEquals(3, s.streakDays); assertEquals(3, s.weeklyCount); assertEquals(6, s.activeMinutes) }
    @Test fun aMissedDayBreaksStreak() { val r = WorkoutRecord("id", "start", 98, 120, 1, 1, 4); assertEquals(0, ProgressCalculator.calculate(FitData(workouts = listOf(r)), 100).streakDays) }
    @Test fun progressIsBoundedAndEqualGoalDoesNotDivideByZero() { assertEquals(1.0, ProgressCalculator.calculate(FitData(profile = profile, measurements = listOf(Measurement(100, 60.0))), 100).goalFraction); assertEquals(1.0, ProgressCalculator.calculate(FitData(profile = profile.copy(targetKg = 80.0)), 100).goalFraction) }
    @Test fun futureMeasurementsDoNotAffectToday() { val s = ProgressCalculator.calculate(FitData(profile = profile, measurements = listOf(Measurement(500, 60.0))), 100); assertEquals(80.0, s.currentKg) }
    @Test fun catalogIsConsistent() { assertEquals(12, Catalog.exercises.size); Catalog.plans.forEach { p -> p.intervals.forEach { Catalog.exercise(it.exerciseId) }; assertTrue(p.durationSeconds > 0) }; assertEquals(Catalog.exercises.size, Catalog.exercises.map { it.id }.distinct().size) }
    @Test fun rejectsInconsistentBodyComposition() { assertFailsWith<IllegalArgumentException> { Validation.measurement(Measurement(100, 70.0, fatPercent = 30.0, muscleKg = 60.0)) } }
    @Test fun zeroRestMovesDirectlyToNextWork() { val p = Plan("test", "Test", "", listOf(Interval("march", 1, 0), Interval("calf", 1, 0))); val s = SessionEngine.tick(SessionEngine.start(p), 6000); assertEquals(Phase.WORK, s.phase); assertEquals(1, s.index); assertEquals(1000, s.activeMs) }
    @Test fun exportKeepsOptionalValuesBlank() { assertTrue(CsvExporter.measurements(FitData(measurements = listOf(Measurement(100, 80.0)))).contains("100,80.0,,,")) }
}
