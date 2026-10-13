package dev.breda.fit

import kotlinx.serialization.Serializable

@Serializable enum class AvatarStyle { CAIO, AURORA, ALEX }
@Serializable enum class GenderIdentity { UNSPECIFIED, WOMAN, MAN, NON_BINARY, SELF_DESCRIBED }
/** name is the social/preferred name; no legal name or sex assigned at birth is collected. */
@Serializable data class Profile(val name: String, val age: Int, val heightCm: Double, val initialKg: Double, val targetKg: Double, val weeklySessions: Int, val createdDay: Long, val avatar: AvatarStyle = AvatarStyle.CAIO, val genderIdentity: GenderIdentity = GenderIdentity.UNSPECIFIED, val genderDescription: String = "")
@Serializable data class Measurement(val day: Long, val kg: Double, val waistCm: Double? = null, val fatPercent: Double? = null, val muscleKg: Double? = null)
@Serializable data class WorkoutRecord(val id: String, val planId: String, val day: Long, val activeSeconds: Long, val completedIntervals: Int, val totalIntervals: Int, val effort: Int)
@Serializable data class Preferences(val reducedMotion: Boolean = false, val voice: Boolean = false)
@Serializable data class FitData(val schema: Int = 1, val profile: Profile? = null, val measurements: List<Measurement> = emptyList(), val workouts: List<WorkoutRecord> = emptyList(), val preferences: Preferences = Preferences())

enum class Movement { SIT_STAND, SQUAT, CALF, SIDE_LEG, BACK_LEG, WALL_PUSH, MARCH, SIDE_STEP, SEATED_MARCH, KNEE_EXTEND, SHOULDER, ANKLE }
data class Exercise(val id: String, val title: String, val group: String, val movement: Movement, val instructions: List<String>, val cue: String, val equipment: String, val hands: Boolean = false, val seated: Boolean = false)
data class Interval(val exerciseId: String, val workSeconds: Int = 40, val restSeconds: Int = 25)
data class Plan(val id: String, val title: String, val subtitle: String, val intervals: List<Interval>) {
    val durationSeconds: Int get() = 5 + intervals.sumOf { it.workSeconds + it.restSeconds } - (intervals.lastOrNull()?.restSeconds ?: 0)
}

object Catalog {
    val exercises = listOf(
        Exercise("chair", "Sentar e levantar", "Pernas", Movement.SIT_STAND, listOf("Use uma cadeira firme, sem rodas, contra a parede.", "Pés na largura dos quadris. Incline o tronco levemente e levante devagar.", "Volte a sentar com controle. Faça poucas repetições confortáveis."), "Suba com controle, sem impulso.", "Cadeira firme"),
        Exercise("squat", "Miniagachamento", "Pernas", Movement.SQUAT, listOf("Fique perto de um apoio firme, com pés afastados na largura dos quadris.", "Flexione pouco os joelhos, alinhados com os pés.", "Retorne sem prender a respiração."), "Amplitude pequena e confortável.", "Apoio opcional"),
        Exercise("calf", "Elevação de calcanhares", "Pernas", Movement.CALF, listOf("Fique ao lado de uma cadeira firme ou parede.", "Eleve os calcanhares devagar, mantendo o corpo ereto.", "Desça suavemente. Use apoio se precisar."), "Suba e desça devagar.", "Apoio firme"),
        Exercise("sideleg", "Elevação lateral da perna", "Pernas", Movement.SIDE_LEG, listOf("Mantenha o tronco ereto e use apoio se necessário.", "Afaste uma perna um pouco, sem inclinar o quadril.", "Retorne e alterne os lados."), "Quadril estável, sem balanço.", "Apoio firme"),
        Exercise("backleg", "Extensão da perna", "Pernas", Movement.BACK_LEG, listOf("Fique de pé com um apoio firme à frente.", "Leve uma perna um pouco para trás, sem arquear a lombar.", "Volte e alterne os lados."), "Movimento curto, costas neutras.", "Apoio firme"),
        Exercise("wall", "Flexão na parede", "Braços", Movement.WALL_PUSH, listOf("Fique de frente para uma parede, à distância dos braços.", "Apoie as mãos na altura do peito e flexione os cotovelos.", "Empurre a parede para voltar, mantendo o corpo alinhado."), "Aproxime o peito com controle.", "Parede", hands = true),
        Exercise("march", "Marcha leve", "Cardio", Movement.MARCH, listOf("Marche no lugar em ritmo confortável.", "Eleve pouco os joelhos e aterrisse suavemente.", "Você deve conseguir falar durante o movimento."), "Ritmo confortável, sem saltos.", "Sem equipamento"),
        Exercise("step", "Passo lateral", "Cardio", Movement.SIDE_STEP, listOf("Dê um passo curto para o lado.", "Aproxime o outro pé sem cruzar as pernas.", "Alterne os lados devagar e sem saltar."), "Passos curtos e suaves.", "Sem equipamento"),
        Exercise("seated", "Marcha sentada", "Mobilidade", Movement.SEATED_MARCH, listOf("Sente em uma cadeira firme, com pés apoiados no chão.", "Eleve um joelho um pouco, mantendo o tronco ereto.", "Abaixe e alterne o lado."), "Eleve uma perna de cada vez.", "Cadeira firme", seated = true),
        Exercise("knee", "Extensão do joelho sentada", "Mobilidade", Movement.KNEE_EXTEND, listOf("Sente com postura confortável e pés no chão.", "Estenda um joelho lentamente, sem travar.", "Retorne e alterne as pernas."), "Devagar, dentro do seu conforto.", "Cadeira firme", seated = true),
        Exercise("shoulder", "Círculos de ombros", "Mobilidade", Movement.SHOULDER, listOf("Sente ou fique de pé, mantendo os braços relaxados.", "Faça círculos pequenos com os ombros para trás.", "Respire normalmente, sem elevar o pescoço."), "Ombros soltos, círculos pequenos.", "Sem equipamento"),
        Exercise("ankle", "Mobilidade de tornozelos", "Mobilidade", Movement.ANKLE, listOf("Sente em cadeira firme com o tronco ereto.", "Estenda um pouco a perna e aponte o pé suavemente.", "Flexione o tornozelo e alterne os lados."), "Movimente o tornozelo suavemente.", "Cadeira firme", seated = true)
    )
    private fun circuit(ids: List<String>, rounds: Int = 1, seconds: Int = 40) = List(rounds) { ids.map { Interval(it, seconds, 25) } }.flatten()
    val plans = listOf(
        Plan("start", "Comece no seu ritmo", "Baixo impacto • primeira rotina", circuit(listOf("march", "chair", "calf", "step", "shoulder", "seated"))),
        Plan("standing", "Em pé, sem apoio nas mãos", "Sem exercícios no chão • ritmo leve", circuit(listOf("march", "step", "squat", "calf", "shoulder"), 2)),
        Plan("chair", "Movimento na cadeira", "Mobilidade • intervalos curtos", circuit(listOf("seated", "knee", "shoulder", "ankle"), 2, 30)),
        Plan("strength", "Força em casa", "Parede + cadeira • movimento controlado", circuit(listOf("chair", "wall", "sideleg", "backleg", "calf"), 2))
    )
    fun exercise(id: String): Exercise = exercises.first { it.id == id }
    fun plan(id: String): Plan = plans.first { it.id == id }
}
