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
    @Test fun sceneCameraChangesRenderedFrameAndCanExpand(){
        val exercise=Catalog.exercise("chair")
        compose.setContent{FitTheme{ExerciseArt(exercise,false,Modifier.fillMaxSize().padding(20.dp))}}
        compose.waitUntil(30000){compose.onAllNodesWithTag("scene-ready-chair").fetchSemanticsNodes().size==1}
        compose.waitForIdle();Thread.sleep(500)
        val before=compose.onNodeWithTag("exercise-art-chair").captureToImage().asAndroidBitmap()
        val a=IntArray(before.width*before.height);before.getPixels(a,0,before.width,0,0,before.width,before.height)
        compose.onNodeWithContentDescription("Girar personagem para a direita").performClick()
        compose.waitForIdle();Thread.sleep(700)
        val after=compose.onNodeWithTag("exercise-art-chair").captureToImage().asAndroidBitmap()
        val b=IntArray(after.width*after.height);after.getPixels(b,0,after.width,0,0,after.width,after.height)
        assertFalse("Orbit must change the rendered scene",a.contentEquals(b))
        compose.onNodeWithText("Ampliar").performClick()
        compose.onNodeWithText("Explore o movimento de outros ângulos.").assertIsDisplayed()
        compose.waitUntil(30000){compose.onAllNodesWithTag("scene-ready-chair").fetchSemanticsNodes().size>=2}
        screenshot("visual-expanded-3d.png")
        compose.onNodeWithText("Fechar demonstração").performClick()
        compose.onNodeWithText("Ampliar").assertIsDisplayed()
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
        compose.waitUntil(30000){Catalog.exercises.all{compose.onAllNodesWithTag("scene-ready-${it.id}").fetchSemanticsNodes().isNotEmpty()}}
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
        compose.waitUntil(30000){compose.onAllNodesWithTag("scene-ready-chair").fetchSemanticsNodes().size==2}
        assertEquals(2, compose.onAllNodesWithTag("exercise-art-chair").fetchSemanticsNodes().size)
        screenshot("visual-chair.png")
    }
}
