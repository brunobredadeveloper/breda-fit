package dev.breda.fit.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.breda.fit.*
import kotlin.math.*

/** Illustration geometry only: independent from workout timing, records and health indicators. */
private data class Pose(
    val head: Offset, val shoulder: Offset, val hip: Offset,
    val knees: List<Offset>, val feet: List<Offset>,
    val elbows: List<Offset>, val hands: List<Offset>, val profile: Boolean = false
)

private fun blend(a: Offset, b: Offset, t: Float) = a + (b - a) * t

/** Two-segment joint keeps the leg connected as the torso moves. */
private fun joint(a: Offset, b: Offset, upper: Float, lower: Float, bend: Float): Offset {
    val delta = b - a
    val distance = delta.getDistance().coerceIn(.001f, upper + lower - .001f)
    val direction = delta / delta.getDistance().coerceAtLeast(.001f)
    val along = ((upper * upper - lower * lower + distance * distance) / (2 * distance)).coerceIn(-upper, upper)
    val height = sqrt((upper * upper - along * along).coerceAtLeast(0f))
    return a + direction * along + Offset(-direction.y, direction.x) * height * bend
}

private fun pose(m: Movement, t: Float): Pose {
    var shoulder = Offset(119f, 79f)
    var hip = Offset(119f, 145f)
    var head = Offset(119f, 48f)
    var knees = listOf(Offset(103f, 190f), Offset(135f, 190f))
    var feet = listOf(Offset(101f, 239f), Offset(137f, 239f))
    var elbows = listOf(Offset(90f, 108f), Offset(148f, 108f))
    var hands = listOf(Offset(88f, 140f), Offset(150f, 140f))
    var profile = false
    when (m) {
        Movement.SIT_STAND -> {
            profile = true
            hip = blend(Offset(112f, 143f), Offset(91f, 182f), t)
            shoulder = hip + Offset(7f + 19f * t, -65f)
            head = shoulder + Offset(1f, -31f)
            feet = listOf(Offset(146f, 239f), Offset(155f, 239f))
            knees = feet.map { joint(hip, it, 57f, 59f, -1f) }
            elbows = listOf(shoulder + Offset(28f, 22f), shoulder + Offset(32f, 20f))
            hands = listOf(elbows[0] + Offset(28f, -5f), elbows[1] + Offset(28f, -5f))
        }
        Movement.SQUAT -> {
            hip += Offset(0f, t * 25f); shoulder += Offset(0f, t * 25f); head += Offset(0f, t * 25f)
            knees = listOf(joint(hip, feet[0], 51f, 50f, 1f), joint(hip, feet[1], 51f, 50f, -1f))
            elbows = listOf(Offset(86f, 98f + t * 25f), Offset(152f, 98f + t * 25f))
            hands = listOf(Offset(111f, 96f + t * 25f), Offset(128f, 96f + t * 25f))
        }
        Movement.CALF -> {
            val lift = Offset(0f, -t * 12f)
            head += lift; shoulder += lift; hip += lift; knees = knees.map { it + lift }; feet = feet.map { it + Offset(0f, -t * 6f) }
        }
        Movement.SIDE_LEG -> {
            knees = listOf(knees[0], knees[1] + Offset(t * 21f, -t * 6f))
            feet = listOf(feet[0], feet[1] + Offset(t * 39f, -t * 16f))
        }
        Movement.BACK_LEG -> {
            profile = true
            shoulder = Offset(129f, 79f); hip = Offset(121f, 145f); head = Offset(131f, 48f)
            feet = listOf(Offset(125f, 239f), Offset(126f - t * 40f, 237f - t * 12f))
            knees = feet.map { joint(hip, it, 51f, 51f, -1f) }
            elbows = listOf(Offset(154f, 100f), Offset(151f, 107f)); hands = listOf(Offset(173f, 127f), Offset(173f, 127f))
        }
        Movement.WALL_PUSH -> {
            profile = true
            hip = Offset(119f + t * 13f, 147f)
            shoulder = Offset(141f + t * 21f, 85f)
            head = shoulder + Offset(6f, -32f)
            feet = listOf(Offset(92f, 239f), Offset(102f, 239f))
            knees = feet.map { joint(hip, it, 50f, 50f, 1f) }
            hands = listOf(Offset(208f, 93f), Offset(208f, 99f))
            elbows = listOf(joint(shoulder, hands[0], 37f, 35f, 1f), joint(shoulder, hands[1], 37f, 35f, 1f))
        }
        Movement.MARCH -> {
            knees = listOf(Offset(99f - t * 10f, 190f - t * 27f), knees[1])
            feet = listOf(Offset(98f, 239f - t * 32f), feet[1])
            elbows = listOf(Offset(91f, 110f - t * 10f), Offset(148f, 109f + t * 7f))
            hands = listOf(Offset(83f, 139f - t * 31f), Offset(154f, 141f + t * 3f))
        }
        Movement.SIDE_STEP -> {
            val shift = Offset(t * 10f, 0f)
            head += shift; shoulder += shift; hip += shift
            knees = listOf(knees[0] + shift, knees[1] + Offset(t * 25f, 0f))
            feet = listOf(feet[0] + shift, feet[1] + Offset(t * 35f, 0f))
        }
        Movement.SEATED_MARCH, Movement.KNEE_EXTEND, Movement.ANKLE -> {
            profile = true
            hip = Offset(97f, 174f); shoulder = Offset(99f, 109f); head = Offset(100f, 78f)
            knees = listOf(Offset(147f, 179f), Offset(141f, 178f))
            feet = listOf(Offset(154f, 238f), Offset(148f, 238f))
            if (m == Movement.SEATED_MARCH) { knees = listOf(knees[0] + Offset(-t * 3f, -t * 15f), knees[1]); feet = listOf(feet[0] + Offset(-t * 3f, -t * 15f), feet[1]) }
            if (m == Movement.KNEE_EXTEND) { val angle = (90f - t * 78f) * PI.toFloat() / 180f; feet = listOf(knees[0] + Offset(cos(angle) * 58f, sin(angle) * 58f), feet[1]) }
            if (m == Movement.ANKLE) feet = listOf(Offset(194f, 197f), feet[1])
            elbows = listOf(Offset(78f, 142f), Offset(123f, 143f)); hands = listOf(Offset(94f, 168f), Offset(143f, 173f))
        }
        Movement.SHOULDER -> {
            shoulder += Offset(sin(t * PI.toFloat()) * 2f, -sin(t * PI.toFloat()) * 5f)
            elbows = listOf(Offset(90f, 109f - t * 3f), Offset(148f, 109f - t * 3f))
        }
    }
    return Pose(head, shoulder, hip, knees, feet, elbows, hands, profile)
}

private val Skin = Color(0xFFE7A080)
private val SkinLight = Color(0xFFF7B99C)
private val Hair = Color(0xFF273D38)
private val Shirt = Color(0xFFB0F36F)
private val ShirtShade = Color(0xFF74B942)
private val Trousers = Color(0xFF7398C5)
private val TrouserShade = Color(0xFF4C709A)
private val Shoe = Color(0xFFF1F4E9)

private fun DrawScope.segment(a: Offset, b: Offset, color: Color, width: Float, light: Color? = null) {
    drawLine(color, a, b, width, StrokeCap.Round)
    light?.let { drawLine(it, a + Offset(-width * .17f, 0f), b + Offset(-width * .17f, 0f), width * .24f, StrokeCap.Round) }
}

private fun DrawScope.chair(seat: Float, x: Float = 70f, width: Float = 63f) {
    val metal = Color(0xFF617F75)
    segment(Offset(x, seat - 50f), Offset(x, seat + 4f), metal, 5f)
    drawRoundRect(Color(0xFF344F45), Offset(x - 5f, seat - 56f), Size(12f, 41f), androidx.compose.ui.geometry.CornerRadius(5f))
    segment(Offset(x + 2f, seat + 4f), Offset(x - 1f, 241f), metal, 5f)
    segment(Offset(x + width - 3f, seat + 4f), Offset(x + width + 3f, 241f), metal, 5f)
    drawRoundRect(Color(0xFF809D8A), Offset(x - 3f, seat - 2f), Size(width + 9f, 9f), androidx.compose.ui.geometry.CornerRadius(4f))
    segment(Offset(x, seat), Offset(x + width + 3f, seat), Color(0xFFA1BAA6), 2f)
}

private fun DrawScope.shoe(foot: Offset, front: Boolean, tilt: Float = 0f) {
    rotate(tilt, foot) {
        val path = Path().apply {
            moveTo(foot.x - 6f, foot.y - 7f); quadraticTo(foot.x + 1f, foot.y - 10f, foot.x + 7f, foot.y - 4f)
            lineTo(foot.x + 17f, foot.y - 2f); quadraticTo(foot.x + 22f, foot.y, foot.x + 19f, foot.y + 5f)
            lineTo(foot.x - 6f, foot.y + 5f); close()
        }
        drawPath(path, if (front) Shoe else Color(0xFFC0CEBE))
        segment(foot + Offset(-5f, 5f), foot + Offset(18f, 5f), Hair, 2.5f)
        segment(foot + Offset(3f, -3f), foot + Offset(8f, -1f), ShirtShade, 2f)
    }
}

private fun DrawScope.arm(p: Pose, index: Int, front: Boolean) {
    val start = p.shoulder + Offset(if (p.profile) 0f else if (index == 0) -16f else 16f, 3f)
    val elbow = p.elbows[index]; val hand = p.hands[index]
    val skin = if (front) Skin else Color(0xFFC48369)
    segment(start, elbow, skin, 11f, if (front) SkinLight else null)
    segment(start, blend(start, elbow, .36f), if (front) Shirt else ShirtShade, 15f)
    segment(elbow, hand, skin, 9f, if (front) SkinLight else null)
    drawOval(if (front) SkinLight else skin, hand - Offset(5f, 6f), Size(10f, 13f))
}

private fun DrawScope.figure(exercise: Exercise, phase: Float) {
    val p = pose(exercise.movement, phase)
    drawOval(Color.Black.copy(alpha=.22f), Offset(60f, 240f), Size(132f, 12f))
    drawLine(Color(0xFF52685B), Offset(32f, 247f), Offset(218f, 247f), 1f)
    if (exercise.seated || exercise.movement == Movement.SIT_STAND) chair(if (exercise.seated) 180f else 187f)
    if (exercise.movement == Movement.BACK_LEG) chair(163f, 180f, 40f)
    if (exercise.movement == Movement.WALL_PUSH) {
        drawRoundRect(Color(0xFF355047), Offset(214f, 41f), Size(10f, 206f), androidx.compose.ui.geometry.CornerRadius(4f))
        drawLine(Color(0xFF709286), Offset(213f, 43f), Offset(213f, 247f), 2f)
    }
    fun leg(i: Int, front: Boolean) {
        val hip = p.hip + Offset(if (p.profile) 0f else if (i == 0) -9f else 9f, 0f)
        val color = if (front) Trousers else TrouserShade
        segment(hip, p.knees[i], color, 20f, if (front) Color(0xFF91B0D6) else null)
        segment(p.knees[i], p.feet[i], color, 15f, if (front) Color(0xFF91B0D6) else null)
        shoe(p.feet[i], front, if (exercise.movement == Movement.ANKLE && i == 0) -15f + phase * 30f else if (exercise.movement == Movement.CALF) phase * 12f else 0f)
    }
    arm(p, 1, false); leg(1, false); leg(0, true)
    segment(p.head + Offset(0f, 14f), p.shoulder, Skin, 11f)
    val body = Path().apply {
        val s = p.shoulder; val h = p.hip
        moveTo(s.x - 17f, s.y - 3f)
        cubicTo(s.x - 25f, s.y + 14f, h.x - 18f, h.y - 14f, h.x - 17f, h.y + 3f)
        quadraticTo(h.x, h.y + 12f, h.x + 18f, h.y + 3f)
        cubicTo(h.x + 19f, h.y - 18f, s.x + 24f, s.y + 11f, s.x + 17f, s.y - 3f)
        quadraticTo(s.x, s.y - 10f, s.x - 17f, s.y - 3f); close()
    }
    drawPath(body, Brush.linearGradient(listOf(Shirt, ShirtShade), p.shoulder - Offset(15f,0f), p.hip + Offset(22f,10f)))
    segment(p.shoulder + Offset(8f, 17f), p.hip + Offset(9f, -8f), ShirtShade.copy(alpha=.5f), 2f)
    drawCircle(Color(0xFFE7FFD4), 3f, p.shoulder + Offset(7f, 16f))
    arm(p, 0, true)
    drawOval(Skin, p.head - Offset(15f, 20f), Size(30f, 38f))
    drawOval(SkinLight, p.head - Offset(11f, 16f), Size(24f, 32f))
    val hair = Path().apply {
        val h=p.head; moveTo(h.x-15f,h.y+1f); cubicTo(h.x-20f,h.y-24f,h.x+14f,h.y-30f,h.x+17f,h.y-6f)
        quadraticTo(h.x+5f,h.y-13f,h.x-6f,h.y-11f); lineTo(h.x-11f,h.y+1f); close()
    }
    drawPath(hair, Hair)
    if (p.profile) {
        drawCircle(Hair, 1.3f, p.head + Offset(10f, -1f))
        val nose=Path().apply{moveTo(p.head.x+13f,p.head.y);lineTo(p.head.x+18f,p.head.y+5f);lineTo(p.head.x+12f,p.head.y+7f);close()};drawPath(nose,SkinLight)
        segment(p.head + Offset(8f, 11f), p.head + Offset(12f, 11f), Color(0xFF9D6452), 1f)
    } else {
        drawCircle(Hair, 1.2f, p.head + Offset(-5f, 0f)); drawCircle(Hair, 1.2f, p.head + Offset(6f, 0f))
        drawArc(Color(0xFF9D6452), 15f, 150f, false, p.head + Offset(-3f, 5f), Size(8f, 6f), style=Stroke(1.2f, cap=StrokeCap.Round))
    }
}

@Composable fun ExerciseArt(exercise: Exercise, animate: Boolean, modifier: Modifier = Modifier, fixedPhase: Float = 0f) {
    val phase = if (animate) {
        val transition = rememberInfiniteTransition(label = "Exercise illustration")
        val value by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "Demonstration phase")
        value
    } else fixedPhase.coerceIn(0f, 1f)
    BoxWithConstraints(modifier.clip(RoundedCornerShape(24.dp)).background(Brush.verticalGradient(listOf(Color(0xFF243E33), Color(0xFF162820)))).testTag("exercise-art-${exercise.id}").semantics { contentDescription = "Ilustração de ${exercise.title}. ${exercise.cue}" }) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Brush.radialGradient(listOf(Shirt.copy(alpha=.09f), Color.Transparent), center=Offset(size.width*.5f,size.height*.45f), radius=size.minDimension*.6f), radius=size.minDimension*.6f, center=Offset(size.width*.5f,size.height*.45f))
            val ratio = minOf(size.width / 250f, size.height / 270f)
            translate((size.width - 250f * ratio) / 2f, (size.height - 270f * ratio) / 2f) {
                scale(ratio, ratio, pivot = Offset.Zero) { figure(exercise, phase) }
            }
        }
        if (maxWidth >= 180.dp && maxHeight >= 150.dp) {
            Text(if (animate) "DEMONSTRAÇÃO" else "POSTURA", Modifier.align(Alignment.TopStart).padding(12.dp), color=Mist.copy(alpha=.65f), style=MaterialTheme.typography.labelSmall)
            Row(Modifier.align(Alignment.BottomEnd).padding(12.dp), horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                repeat(3) { i -> Canvas(Modifier.size(4.dp)) { drawCircle(if (phase * 2f >= i) Lime else Mist.copy(alpha=.25f)) } }
            }
        }
    }
}

@Composable fun PositionPair(exercise: Exercise) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(0f to "1 · Início", 1f to "2 · Movimento").forEach { (phase, label) ->
            Column(Modifier.weight(1f)) {
                ExerciseArt(exercise, false, Modifier.fillMaxWidth().height(190.dp), phase)
                Text(label, Modifier.padding(top = 8.dp), color = Mist)
            }
        }
    }
}
