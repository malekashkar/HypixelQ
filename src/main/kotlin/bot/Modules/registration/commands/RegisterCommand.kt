package bot.Modules.registration.commands

import bot.Bot
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import bot.utils.Config
import bot.utils.api.Hypixel
import bot.utils.api.Mojang
import dev.minn.jda.ktx.await

class RegisterCommand : Command() {
    override val name = "register"

    @Executor
    suspend fun execute(
        context: ICommandContext,
        @Argument(optional = false) username: String?
    ) {
        if(context.channel.id == Config.Channels.registrationChannel) {
            if(username !== null) {
                val mojangProfile = Mojang.getMojangProfile(username)
                if(mojangProfile !== null) {
                    val userData = context.getUserData()
                    if(userData.uuid == mojangProfile.id) {
                        context.reply(
                            EmbedTemplates
                                .error("The username $username has already been registered in the past!")
                                .build()
                        ).queue()
                        return
                    }

                    val playerData = Hypixel.getPlayerData(mojangProfile.id)
                    if(playerData !== null) {
                        if(
                            playerData.discordTag !== null &&
                            playerData.discordTag.lowercase() == context.author.asTag.lowercase()
                        ) {
                            val guildMember = context.guild?.retrieveMember(context.author)?.await()
                            if(guildMember !== null) {
                                val memberRole = context.guild?.getRoleById(Config.Roles.registeredRole)
                                if(memberRole !== null) {
                                    context.guild?.addRoleToMember(guildMember, memberRole)?.queue()
                                    if(context.guild?.selfMember!!.canInteract(guildMember)) {
                                        guildMember.modifyNickname("[${playerData.bedwarsLevel} \uD83C\uDF1F] $username").queue()
                                    }
                                    Bot.database.userRepository.updateUuid(userData, mojangProfile.id)
                                    Bot.database.userRepository.updateStats(
                                        userData,
                                        playerData.fkdr,
                                        playerData.winstreak,
                                        playerData.bedwarsLevel,
                                    )
                                    context.reply(
                                        EmbedTemplates
                                            .normal(
                                                "You have linked your Hypixel account successfully.",
                                                "Linked $username"
                                            )
                                            .setAuthor("",context.author.avatarUrl)
                                            .build()
                                    ).queue()
                                } else {
                                    context.reply(
                                        EmbedTemplates
                                            .error("The member role has been deleted, please notify an administrator!")
                                            .build()
                                    ).queue()
                                }
                            }
                        } else {
                            context.reply(
                                EmbedTemplates
                                    .error("You did not link your discord to Hypixel correctly, please try again!")
                                    .build()
                            ).queue()
                        }
                    } else {
                        context.reply(
                            EmbedTemplates
                                .error("The Hypixel API seems to be offline currently, please try again later!")
                                .build()
                        ).queue()
                    }
                } else {
                    context.reply(
                        EmbedTemplates
                            .error("The Mojang API seems to be offline currently, please try again later!")
                            .build()
                    ).queue()
                }
            } else {
                context.reply(
                    EmbedTemplates.error("Please provide a username to register with!").build()
                ).queue()
            }
        }
    }
}