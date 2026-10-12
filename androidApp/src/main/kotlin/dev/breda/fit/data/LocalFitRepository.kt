package dev.breda.fit.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.breda.fit.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private val Context.fitStore by preferencesDataStore(name = "breda_fit_v1")
class LocalFitRepository(context: Context) : FitRepository {
    private val store = context.applicationContext.fitStore
    private val key = stringPreferencesKey("snapshot")
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = false }
    private fun decode(raw: String?): FitData = if (raw == null) FitData() else json.decodeFromString<FitData>(raw).also(Validation::data)
    override val data = store.data.map { decode(it[key]) }.flowOn(Dispatchers.Default)
    override suspend fun update(transform: (FitData) -> FitData) {
        withContext(Dispatchers.Default) {
        store.edit { prefs ->
            val next = transform(decode(prefs[key])).also(Validation::data)
            prefs[key] = json.encodeToString(FitData.serializer(), next)
        }
        }
    }
    override suspend fun reset() { store.edit { it.remove(key) } }
}
