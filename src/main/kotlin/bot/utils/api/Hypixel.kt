package bot.utils.api

import bot.Bot
import bot.Core.database.models.HypixelData
import bot.Core.database.models.Stats
import bot.utils.Config
import bot.utils.Json
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.math.floor
import kotlin.math.round

object Hypixel {
    private const val HIGHEST_PRESTIGE = 10;

    private const val EASY_LEVELS = 4;
    private const val EASY_LEVELS_XP = 7000;
    private const val XP_PER_PRESTIGE = 96 * 5000 + EASY_LEVELS_XP;
    private const val LEVELS_PER_PRESTIGE = 100;

    private enum class Endpoint(val path: String) {
        Player("/player"),
    }

    enum class Ranks(val roleId: String?) {
        NONE(null),
        VIP(Config.Roles.vipRole),
        VIP_PLUS(Config.Roles.vipPlusRole),
        MVP(Config.Roles.mvpRole),
        MVP_PLUS(Config.Roles.mvpPlusRole),
        MVP_PLUS_PLUS(Config.Roles.mvpPlusPlusRole)
    }

    data class HypixelPlayerInfo(
        val uuid: String,
        val name: String? = null,
        val statsData: HypixelData,
        val discordTag: String? = null,
        val rank: String? = null
    )

    private const val root = "https://api.hypixel.net"

    private fun getFKDR(finalKills: Int, finalDeaths: Int): Double {
        return if(finalDeaths == 0) {
            finalKills.toDouble()
        } else {
            round(finalKills.toDouble() / finalDeaths.toDouble() * 100) / 100
        }
    }

    private fun getLevelForExp(exp: Int): Int {
        val prestiges = floor(exp.toDouble() / XP_PER_PRESTIGE)
        var level = prestiges * LEVELS_PER_PRESTIGE

        var expWithoutPrestiges = exp - (prestiges * XP_PER_PRESTIGE)

        for (i in 1..EASY_LEVELS) {
            val expForEasyLevel = getExpForLevel(i)
            if(expWithoutPrestiges < expForEasyLevel) {
                break
            }

            level++
            expWithoutPrestiges -= expForEasyLevel
        }
        level += floor(expWithoutPrestiges / 5000)

        return level.toInt()
    }

    private fun getExpForLevel(level: Int): Int {
        if(level == 0) return 0

        val respectedLevel = getLevelRespectingPrestige(level)
        if(respectedLevel > EASY_LEVELS) {
            return 5000
        }

        when(respectedLevel) {
            1 -> return 500
            2 -> return 1000
            3 -> return 2000
            4 -> return 3500
        }
        return 5000
    }

    private fun getLevelRespectingPrestige(level: Int): Int {
        return if(level > HIGHEST_PRESTIGE * LEVELS_PER_PRESTIGE) level - HIGHEST_PRESTIGE * LEVELS_PER_PRESTIGE else level % LEVELS_PER_PRESTIGE
    }

    fun getRankClass(rank: String): Ranks {
        return when(rank) {
            "MVP_PLUS_PLUS" -> Ranks.MVP_PLUS_PLUS
            "MVP_PLUS" -> Ranks.MVP_PLUS
            "MVP" -> Ranks.MVP
            "VIP_PLUS" -> Ranks.VIP_PLUS
            "VIP" -> Ranks.VIP
            else -> Ranks.NONE
        }
    }

    suspend fun getPlayerData(uuid: String): HypixelData? {
        val httpResponse = Bot.ktorClient.get<HttpResponse>("$root${Endpoint.Player.path}") {
            header("API-Key", System.getenv("HYPIXEL_TOKEN"))
            contentType(ContentType.Application.Json)
            parameter("uuid", uuid)
        }

        if(httpResponse.status == HttpStatusCode.OK) {
            val jsonTest = httpResponse.readText()
            val json = Json.parseToJsonElement(jsonTest).jsonObject

            val hypixelPlayer = json["player"]

            val playerDisplayName = (hypixelPlayer!!.jsonObject["displayname"] as? JsonPrimitive)?.content

            var finalKills = 0
            var finalDeaths = 0
            var winstreak = 0
            var bedwarsExperience = 0

            val playerStats = hypixelPlayer.jsonObject["stats"]
            if(playerStats != null) {
                val bedwarsStats = playerStats.jsonObject["Bedwars"]
                if(bedwarsStats != null) {
                    finalKills = (bedwarsStats.jsonObject["final_kills_bedwars"] as? JsonPrimitive)?.content?.toIntOrNull() ?: 0
                    finalDeaths = (bedwarsStats.jsonObject["final_deaths_bedwars"] as? JsonPrimitive)?.content?.toIntOrNull() ?: 0
                    winstreak = (bedwarsStats.jsonObject["winstreak"] as? JsonPrimitive)?.content?.toIntOrNull() ?: 0
                    bedwarsExperience = (bedwarsStats.jsonObject["Experience"] as? JsonPrimitive)?.content?.toIntOrNull() ?: 0
                }
            }

            var discord: String? = null
            val playerSocialMedia = hypixelPlayer.jsonObject["socialMedia"]
            if(playerSocialMedia != null) {
                val socialMediaLinks = playerSocialMedia.jsonObject["links"]
                if(socialMediaLinks != null) {
                    discord = (socialMediaLinks.jsonObject["DISCORD"] as? JsonPrimitive)?.content
                }
            }

            val rank = (hypixelPlayer.jsonObject["newPackageRank"] as? JsonPrimitive)?.content

            if(playerDisplayName != null) {
                return HypixelData(
                    playerDisplayName,
                    rank,
                    discord,
                    Stats(
                        getFKDR(finalKills, finalDeaths),
                        winstreak,
                        getLevelForExp(bedwarsExperience),
                    )
                )
            }
        }
        return null
    }
}