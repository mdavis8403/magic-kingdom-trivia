package com.mdavis8403.magickingdomtrivia.domain

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GameStateCodec(
    private val json: Json = Json,
) {
    fun encode(state: TriviaGameState): String = json.encodeToString(state)

    fun decode(value: String?): TriviaGameState? {
        if (value == null) return null
        return try {
            json.decodeFromString<TriviaGameState>(value)
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
