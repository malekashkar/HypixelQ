package bot.utils.api

import bot.Bot
import bot.utils.Json
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

object Mojang {
    private enum class Endpoint(val path: String) {
        Profile("/users/profiles/minecraft"),
    }

    private const val root = "https://api.mojang.com"

    data class MojangProfile(val id: String, val name: String)
    data class NameHistory(val name: String, val changedToAt: Long?)

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

    private suspend fun getNameHistory(uuid: String): List<NameHistory>? {
        val httpResponse = Bot.ktorClient.get<HttpResponse>("$root/user/profiles/$uuid/names")
        if(httpResponse.status == HttpStatusCode.OK) {
            val jsonTest = httpResponse.readText()
            val json = Json.parseToJsonElement(jsonTest).jsonArray
            return json.map {
                NameHistory((it.jsonObject["name"] as JsonPrimitive).content, (it.jsonObject["changedToAt"] as? JsonPrimitive)?.content?.toLong())
            }
        }
        return null
    }

    suspend fun getCurrentName(uuid: String): String? {
        val nameHistory = getNameHistory(uuid)
        return if(nameHistory != null && nameHistory.isNotEmpty()) {
            nameHistory.last().name
        } else {
            null
        }
    }
}