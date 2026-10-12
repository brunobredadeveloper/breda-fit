package dev.breda.fit

import android.app.Application
import dev.breda.fit.data.LocalFitRepository

class FitApplication : Application() {
    val repository: FitRepository by lazy { LocalFitRepository(this) }
}
