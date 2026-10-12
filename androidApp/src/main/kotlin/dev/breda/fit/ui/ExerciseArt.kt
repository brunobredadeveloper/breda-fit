package dev.breda.fit.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.breda.fit.*

private data class Pose(val head: Offset, val shoulder: Offset, val hip: Offset, val knees: List<Offset>, val feet: List<Offset>, val elbows: List<Offset>, val hands: List<Offset>)
private fun pose(m: Movement, t: Float): Pose {
    var head = Offset(100f, 41f); var shoulder = Offset(100f, 65f); var hip = Offset(100f, 119f)
    var knees = listOf(Offset(83f, 151f), Offset(117f, 151f)); var feet = listOf(Offset(78f, 190f), Offset(122f, 190f))
    var elbows = listOf(Offset(73f, 92f), Offset(127f, 92f)); var hands = listOf(Offset(65f, 121f), Offset(135f, 121f))
    when (m) {
        Movement.SIT_STAND, Movement.SQUAT -> {
            val v = t * if (m == Movement.SIT_STAND) 43 else 24
            head += Offset(-8f * t, v); shoulder += Offset(-7f * t, v); hip += Offset(7f * t, v)
            knees = listOf(Offset(73f, 155f), Offset(127f, 155f)); elbows = listOf(Offset(69f, 79f + v), Offset(128f, 79f + v)); hands = listOf(Offset(51f, 77f + v), Offset(146f, 77f + v))
        }
        Movement.CALF -> { head -= Offset(0f, t * 11); shoulder -= Offset(0f, t * 11); hip -= Offset(0f, t * 11); knees = knees.map { it - Offset(0f, t * 6) }; feet = feet.map { it - Offset(0f, t * 5) } }
        Movement.SIDE_LEG -> { knees = listOf(knees[0], knees[1] + Offset(t * 23, -t * 5)); feet = listOf(feet[0], feet[1] + Offset(t * 37, -t * 13)) }
        Movement.BACK_LEG -> { knees = listOf(knees[0], knees[1] + Offset(t * 23, -t * 3)); feet = listOf(feet[0], feet[1] + Offset(t * 32, -t * 10)); hands = listOf(Offset(63f, 100f), Offset(135f, 100f)) }
        Movement.WALL_PUSH -> {
            head = Offset(123f + t * 19, 50f); shoulder = Offset(120f + t * 16, 75f); hip = Offset(97f + t * 8, 126f)
            knees = listOf(Offset(82f, 154f), Offset(89f, 160f)); feet = listOf(Offset(62f, 190f), Offset(74f, 190f))
            elbows = listOf(Offset(146f - t * 12, 71f + t * 15), Offset(147f - t * 12, 80f + t * 13)); hands = listOf(Offset(174f, 73f), Offset(174f, 84f))
        }
        Movement.MARCH -> { knees = listOf(Offset(83f - t * 15, 151f - t * 27), knees[1]); feet = listOf(Offset(70f, 190f - t * 29), feet[1]); hands = listOf(Offset(58f, 119f - t * 35), Offset(139f, 112f + t * 12)) }
        Movement.SIDE_STEP -> { head += Offset(t * 12, 0f); shoulder += Offset(t * 12, 0f); hip += Offset(t * 12, 0f); knees = listOf(knees[0], knees[1] + Offset(t * 25, 0f)); feet = listOf(feet[0], feet[1] + Offset(t * 34, 0f)) }
        Movement.SEATED_MARCH, Movement.KNEE_EXTEND, Movement.ANKLE -> {
            head = Offset(89f, 54f); shoulder = Offset(89f, 79f); hip = Offset(95f, 129f)
            knees = listOf(Offset(125f, 133f - if (m == Movement.SEATED_MARCH) t * 17 else 0f), Offset(123f, 139f))
            feet = listOf(Offset(129f + if (m != Movement.SEATED_MARCH) t * 36 else 0f, 188f - if (m == Movement.SEATED_MARCH) t * 18 else t * 46), Offset(121f, 190f))
            elbows = listOf(Offset(70f, 107f), Offset(110f, 105f)); hands = listOf(Offset(70f, 134f), Offset(113f, 133f))
        }
        Movement.SHOULDER -> { shoulder += Offset(0f, -t * 6); elbows = elbows.map { it - Offset(0f, t * 4) } }
    }
    if (m == Movement.ANKLE) { knees = listOf(Offset(125f, 133f), Offset(123f, 139f)); feet = listOf(Offset(164f, 141f), Offset(121f, 190f)) }
    return Pose(head, shoulder, hip, knees, feet, elbows, hands)
}
private fun DrawScope.figure(exercise: Exercise, phase: Float) {
    val p = pose(exercise.movement, phase)
    fun limb(a: Offset, b: Offset, color: Color, width: Float = 10f) = drawLine(color, a, b, width, StrokeCap.Round)
    drawLine(Color(0xFF496055), Offset(24f, 199f), Offset(181f, 199f), 2f)
    if (exercise.seated || exercise.movement == Movement.SIT_STAND) {
        val seat = if (exercise.movement == Movement.SIT_STAND) 164f else 130f
        limb(Offset(53f, seat), Offset(112f, seat), Blue, 5f); limb(Offset(56f, seat), Offset(56f, 196f), Blue, 5f); limb(Offset(109f, seat + 2), Offset(109f, 195f), Blue, 5f); limb(Offset(53f, seat - 48), Offset(53f, seat), Blue, 5f)
    }
    if (exercise.movement == Movement.WALL_PUSH) drawLine(Blue, Offset(180f, 26f), Offset(180f, 199f), 7f)
    p.knees.forEachIndexed { i, k -> limb(p.hip, k, Blue, 13f); limb(k, p.feet[i], Blue, 11f); limb(p.feet[i], p.feet[i] + Offset(10f, if (exercise.movement == Movement.ANKLE && i == 0) -phase * 12 else 0f), Color(0xFFF3F6EF), 7f) }
    limb(p.shoulder, p.hip, Lime, 27f)
    p.elbows.forEachIndexed { i, elbow -> limb(p.shoulder + Offset(if (i == 0) -8f else 8f, 0f), elbow, Coral); limb(elbow, p.hands[i], Coral, 8f) }
    limb(p.head + Offset(0f, 13f), p.shoulder, Coral, 9f); drawCircle(Coral, 13f, p.head)
    drawArc(Color(0xFF263C32), 180f, 180f, false, p.head - Offset(13f, 13f), androidx.compose.ui.geometry.Size(26f, 26f), style = androidx.compose.ui.graphics.drawscope.Stroke(7f))
}
@Composable fun ExerciseArt(exercise: Exercise, animate: Boolean, modifier: Modifier = Modifier, fixedPhase: Float = 0f) {
    val phase = if (animate) {
        val transition = rememberInfiniteTransition(label = "Movement demonstration")
        val animated by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(1500, easing = EaseInOut), RepeatMode.Reverse), label = "Movement phase")
        animated
    } else fixedPhase
    Canvas(modifier.background(Panel, RoundedCornerShape(24.dp)).semantics { contentDescription = "Demonstração esquemática: ${exercise.title}. ${exercise.cue}" }) {
        val ratio = minOf(size.width / 210f, size.height / 220f)
        translate((size.width - 210f * ratio) / 2, (size.height - 220f * ratio) / 2) {
            scale(ratio, ratio, pivot = Offset.Zero) { figure(exercise, phase) }
        }
    }
}
@Composable fun PositionPair(exercise: Exercise) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(0f to "1 · Início", 1f to "2 · Movimento").forEach { (phase, label) ->
            Column(Modifier.weight(1f)) { ExerciseArt(exercise, false, Modifier.fillMaxWidth().height(170.dp), phase); Text(label, Modifier.padding(top = 8.dp), color = Mist) }
        }
    }
}
