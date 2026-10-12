package dev.breda.fit

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import dev.breda.fit.ui.FitApp
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

private class FakeRepository(initial: FitData = FitData()) : FitRepository {
    private val state = MutableStateFlow(initial)
    override val data = state
    override suspend fun update(transform: (FitData) -> FitData) { state.value = transform(state.value).also(Validation::data) }
    override suspend fun reset() { state.value = FitData() }
}
class FitUiTest {
    @get:Rule val compose = createComposeRule()
    @Test fun profileCreationUnlocksNavigation() {
        compose.setContent { FitApp(FakeRepository()) }
        compose.onNodeWithText("Criar meu plano").performScrollTo().performClick()
        compose.onNodeWithText("Como quer ser chamado?").performScrollTo().performTextInput("Ana")
        compose.onNodeWithText("Idade (20 anos ou mais)").performScrollTo().performTextInput("32")
        compose.onNodeWithText("Altura (cm)").performScrollTo().performTextInput("170")
        compose.onNodeWithText("Peso inicial (kg)").performScrollTo().performTextInput("80")
        compose.onNodeWithText("Meta de peso (kg)").performScrollTo().performTextInput("70")
        compose.onNodeWithText("Começar minha jornada").performScrollTo().performClick()
        compose.onNodeWithText("Treinos").assertIsDisplayed().performClick()
        compose.onNodeWithText("Treinar em casa.").assertIsDisplayed()
    }
    @Test fun existingProfileShowsNavigationOnReopen() {
        val initial = FitData(profile = Profile("Ana", 32, 170.0, 80.0, 70.0, 3, 100), preferences = Preferences(reducedMotion = true))
        compose.setContent { FitApp(FakeRepository(initial)) }
        compose.onNodeWithText("Treinos").assertIsDisplayed().performClick()
        compose.onNodeWithText("Treinar em casa.").assertIsDisplayed()
        compose.onNodeWithText("Evolução").performClick()
        compose.onNodeWithText("Registrar medidas de hoje").assertIsDisplayed()
    }
}
