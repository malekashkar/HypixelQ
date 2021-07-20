package bot.Modules.registration.commands

import bot.Bot
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.Modules.registration.User
import bot.api.ICommandContext
import bot.utils.Config
import bot.utils.api.Hypixel
import bot.utils.api.Mojang

class RegisterCommand : Command() {
    override val name = "register"
    override val description = "Register yourself to the discord."
    override var aliases = arrayOf("r")

    @Executor
    suspend fun execute(
        context: ICommandContext,
        @Argument(optional = false) username: String?
    ) {
        if(context.channel.id == Config.Channels.registrationChannel) {
            if(username != null) {
                val profile = Mojang.getMojangProfile(username)
                if(profile != null) {
                    var userData = context.getUserData()
                    if(userData.uuid != profile.id) {
                        val uuidUserData = Bot.database.userRepository.getUser(mojangUuid = profile.id)
                        if(uuidUserData.id == null && uuidUserData.id != context.author.id) {
                            Bot.database.userRepository.updateId(userData, context.author.id)
                            userData = uuidUserData
                            userData.id = context.author.id
                        }

                        val playerData = Hypixel.getPlayerData(profile.id)
                        if(playerData != null) {
                            if(
                                playerData.discordTag != null &&
                                playerData.discordTag.lowercase() == context.author.asTag.lowercase()
                            ) {
                                context.reply(
                                    EmbedTemplates
                                        .normal(
                                            "You have linked your Hypixel account successfully.",
                                            "Linked $username"
                                        )
                                        .setAuthor("",context.author.avatarUrl)
                                        .build()
                                ).queue()

                                userData.uuid = playerData.uuid
                                userData.hypixelData = playerData.statsData
                                User.updateUser(context.guild!!, userData)
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
                                .error("The username $username has already been registered in the past!")
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