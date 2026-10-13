package dev.breda.fit

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import dev.breda.fit.ui.*
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import java.io.File

class ExerciseVisualTest {
    @get:Rule val compose = createComposeRule()
    private fun screenshot(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val image = compose.onRoot().captureToImage().asAndroidBitmap()
        File(instrumentation.targetContext.getExternalFilesDir(null), name).outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    @Test fun catalogIllustrationsRemainVisible() {
        compose.setContent {
            FitTheme {
                Column(Modifier.fillMaxSize().background(Ink).padding(12.dp)) {
                    Text("BREDA FIT · GUIAS VISUAIS", color=Color.White)
                    Catalog.exercises.chunked(3).forEach { group ->
                        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            group.forEach { exercise ->
                                Column(Modifier.weight(1f)) {
                                    ExerciseArt(exercise, false, Modifier.fillMaxWidth().weight(1f), .8f)
                                    Text(exercise.title, maxLines=2, color=Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
        Catalog.exercises.forEach { compose.onNodeWithTag("exercise-art-${it.id}").assertIsDisplayed() }
        screenshot("visual-catalog.png")
    }
    @Test fun chairComparisonRendersBothPositions() {
        val exercise = Catalog.exercise("chair")
        compose.setContent {
            FitTheme {
                Column(Modifier.fillMaxSize().background(Ink).padding(20.dp)) {
                    Text("BREDA FIT · SENTAR E LEVANTAR", color=Color.White)
                    ExerciseArt(exercise, false, Modifier.fillMaxWidth().weight(1f), 0f)
                    Text("1 · Em pé", color=Color.White)
                    ExerciseArt(exercise, false, Modifier.fillMaxWidth().weight(1f), 1f)
                    Text("2 · Sentado", color=Color.White)
                }
            }
        }
        assertEquals(2, compose.onAllNodesWithTag("exercise-art-chair").fetchSemanticsNodes().size)
        screenshot("visual-chair.png")
    }
}
