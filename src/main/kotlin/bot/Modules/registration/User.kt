package bot.Modules.registration

import bot.Bot
import bot.Core.database.models.User
import bot.utils.Config
import bot.utils.api.Hypixel
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.entities.Guild as JDAGuild

object User {
    suspend fun updateUser(
        guild: JDAGuild,
        userData: User,
        updateData: Boolean = true
    ) {
        if(userData.uuid != null && updateData) {
            Bot.database.userRepository.updateUuid(userData, userData.uuid!!)
        }

        if(userData.hypixelData.bedwarsLevel != null) {
            if(updateData) {
                Bot.database.userRepository.updateStats(userData, userData.hypixelData)
            }
            if(userData.id != null) {
                val guildMember = guild.retrieveMemberById(userData.id!!).await()
                if(guildMember != null) {
                    if (guild.selfMember.canInteract(guildMember) && userData.hypixelData.displayName != null) {
                        guildMember
                            .modifyNickname("[${userData.hypixelData.bedwarsLevel} \uD83C\uDF1F] ${userData.hypixelData.displayName}")
                            .queue()
                    }

                    val registeredRole = guild.getRoleById(Config.Roles.registeredRole)
                    if (registeredRole != null) {
                        guild.addRoleToMember(guildMember, registeredRole).queue()
                    }

                    if(userData.hypixelData.rank != null) {
                        val rankClass = Hypixel.getRankClass(userData.hypixelData.rank!!)
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
}