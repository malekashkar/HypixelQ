package bot.utils.api

import bot.Bot
import bot.utils.Json
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

object Mojang {
    private enum class Endpoint(val path: String) {
        Profile("/users/profiles/minecraft"),
    }

    private const val root = "https://api.mojang.com"

    data class MojangProfile(val id: String, val name: String)

    suspend fun getMojangProfile(username: String): MojangProfile? {
        val httpResponse = Bot.ktorClient.get<HttpResponse>("$root${Endpoint.Profile.path}/$username")
        if(httpResponse.status == HttpStatusCode.OK) {
            val jsonTest = httpResponse.readText()
            val json = Json.parseToJsonElement(jsonTest).jsonObject
            return MojangProfile(
                (json["id"] as JsonPrimitive).content,
                (json["name"] as JsonPrimitive).content
            )
        }
        return null
    }
}