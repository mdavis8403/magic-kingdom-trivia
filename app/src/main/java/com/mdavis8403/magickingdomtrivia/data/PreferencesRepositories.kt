package com.mdavis8403.magickingdomtrivia.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mdavis8403.magickingdomtrivia.domain.GameSettings
import com.mdavis8403.magickingdomtrivia.domain.StatisticsAccumulator
import com.mdavis8403.magickingdomtrivia.domain.TriviaStatistics
import com.mdavis8403.magickingdomtrivia.domain.TriviaSummary
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.triviaDataStore: DataStore<Preferences> by preferencesDataStore(name = "trivia_preferences")

class SettingsRepository(context: Context) {
    private val dataStore = context.triviaDataStore

    val settings: Flow<GameSettings> = dataStore.safeData.map { preferences ->
        GameSettings(
            categoryId = preferences[Keys.categoryId] ?: QuestionRepository.MIXED_CATEGORY_ID,
            difficulty = preferences[Keys.difficulty].toDifficultyOrDefault(),
            questionCount = preferences[Keys.questionCount].validOrDefault(GameSettings.VALID_QUESTION_COUNTS, 10),
            timerSeconds = preferences[Keys.timerSeconds].validOrDefault(GameSettings.VALID_TIMER_SECONDS, 0),
            randomizeQuestionOrder = preferences[Keys.randomizeQuestions] ?: true,
            randomizeAnswerOrder = preferences[Keys.randomizeAnswers] ?: true,
            showExplanations = preferences[Keys.showExplanations] ?: true,
            automaticallyAdvance = preferences[Keys.automaticallyAdvance] ?: false,
            avoidRecentlyPlayed = preferences[Keys.avoidRecentlyPlayed] ?: true,
            soundEffects = preferences[Keys.soundEffects] ?: true,
        )
    }

    suspend fun save(settings: GameSettings) {
        dataStore.edit { preferences ->
            preferences[Keys.categoryId] = settings.categoryId
            preferences[Keys.difficulty] = settings.difficulty.name
            preferences[Keys.questionCount] = settings.questionCount
            preferences[Keys.timerSeconds] = settings.timerSeconds
            preferences[Keys.randomizeQuestions] = settings.randomizeQuestionOrder
            preferences[Keys.randomizeAnswers] = settings.randomizeAnswerOrder
            preferences[Keys.showExplanations] = settings.showExplanations
            preferences[Keys.automaticallyAdvance] = settings.automaticallyAdvance
            preferences[Keys.avoidRecentlyPlayed] = settings.avoidRecentlyPlayed
            preferences[Keys.soundEffects] = settings.soundEffects
        }
    }

    private object Keys {
        val categoryId = stringPreferencesKey("category_id")
        val difficulty = stringPreferencesKey("difficulty")
        val questionCount = intPreferencesKey("question_count")
        val timerSeconds = intPreferencesKey("timer_seconds")
        val randomizeQuestions = booleanPreferencesKey("randomize_questions")
        val randomizeAnswers = booleanPreferencesKey("randomize_answers")
        val showExplanations = booleanPreferencesKey("show_explanations")
        val automaticallyAdvance = booleanPreferencesKey("automatically_advance")
        val avoidRecentlyPlayed = booleanPreferencesKey("avoid_recently_played")
        val soundEffects = booleanPreferencesKey("sound_effects")
    }
}

class StatisticsRepository(
    context: Context,
    private val json: Json = Json,
) {
    private val dataStore = context.triviaDataStore

    val statistics: Flow<TriviaStatistics> = dataStore.safeData.map { preferences ->
        decode(preferences[STATISTICS_KEY])
    }

    suspend fun recordGame(summary: TriviaSummary) {
        dataStore.edit { preferences ->
            val updated = StatisticsAccumulator.record(decode(preferences[STATISTICS_KEY]), summary)
            preferences[STATISTICS_KEY] = json.encodeToString(updated)
        }
    }

    suspend fun reset() {
        dataStore.edit { preferences -> preferences.remove(STATISTICS_KEY) }
    }

    private fun decode(value: String?): TriviaStatistics {
        if (value == null) return TriviaStatistics()
        return try {
            json.decodeFromString<TriviaStatistics>(value)
        } catch (_: SerializationException) {
            TriviaStatistics()
        } catch (_: IllegalArgumentException) {
            TriviaStatistics()
        }
    }

    private companion object {
        val STATISTICS_KEY = stringPreferencesKey("statistics")
    }
}

private val DataStore<Preferences>.safeData: Flow<Preferences>
    get() = data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

private fun String?.toDifficultyOrDefault(): Difficulty =
    this?.let { value -> Difficulty.entries.firstOrNull { it.name == value } } ?: Difficulty.MIXED

private fun Int?.validOrDefault(valid: List<Int>, default: Int): Int =
    takeIf { it in valid } ?: default
