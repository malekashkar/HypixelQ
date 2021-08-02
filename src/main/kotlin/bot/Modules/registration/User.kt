package bot.Modules.registration

import bot.Bot
import bot.Core.database.models.Stats
import bot.Core.database.models.User
import bot.utils.Config
import bot.utils.api.Hypixel
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.entities.Guild as JDAGuild

object User {
    fun calculateScore(stats: Stats): Int {
        var points = 0

        // Level points
        points += when(stats.level) {
            0 -> 0
            in 1..100 -> 2
            in 101..200 -> 4
            in 201..300 -> 6
            in 301..500 -> 8
            in 501..700 -> 12
            in 701..1000 -> 16
            in 1001..1500 -> 20
            else -> 24
        }

        // Winstreak points
        points += when(stats.level) {
            in 0..10 -> -2
            in 11..20 -> -1
            in 21..40 -> 0
            in 41..60 -> 1
            in 61..80 -> 2
            in 81..100 -> 3
            else -> 4
        }

        // FKDR points
        points += when(stats.level) {
            in 0..1 -> 0
            in 1..2 -> 1
            in 2..3 -> 2
            in 3..5 -> 3
            in 6..8 -> 4
            in 9..10 -> 7
            in 11..24 -> 10
            else -> 12
        }

        return points
    }

    suspend fun updateUser(
        guild: JDAGuild,
        userData: User,
        updateData: Boolean = true
    ) {
        if(updateData) {
            if(userData.uuid != null) {
                Bot.database.userRepository.updateUuid(userData, userData.uuid!!)
            }

            if(userData.hypixel != null) {
                Bot.database.userRepository.updateHypixelData(userData, userData.hypixel!!)
            }
        }

        if(userData.id != null) {
            val guildMember = guild.retrieveMemberById(userData.id!!).await()
            if(guildMember != null) {
                if (
                    guild.selfMember.canInteract(guildMember) &&
                    userData.hypixel?.displayName != null &&
                    userData.hypixel?.stats?.level != null
                ) {
                    guildMember
                        .modifyNickname("[${userData.hypixel!!.stats!!.level} \uD83C\uDF1F] ${userData.hypixel!!.displayName}")
                        .queue()
                }

                val registeredRole = guild.getRoleById(Config.Roles.registeredRole)
                if (registeredRole != null) {
                    guild.addRoleToMember(guildMember, registeredRole).queue()
                }

                if(userData.hypixel?.rank != null) {
                    val rankClass = Hypixel.getRankClass(userData.hypixel!!.rank!!)
                    if(rankClass.roleId != null) {
                        val rankRole = guild.getRoleById(rankClass.roleId)
                        if(rankRole != null) {
                            guild.addRoleToMember(guildMember, rankRole).queue()
                        }
                    }
                }
            }
        }
    }
}