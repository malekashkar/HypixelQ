package bot.Modules.registration.commands

import bot.Bot
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import bot.utils.Config
import bot.utils.api.Hypixel
import bot.utils.api.Mojang
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import java.util.*

class ForceRegisterCommand: Command() {
    override val name = "forceregister"
    override val description = "Register a discord user for them."
    override var aliases = arrayOf(
        "fregister",
        "fr"
    )

    override var requiredUserPermissions = arrayOf(Permission.MANAGE_SERVER)

    @Executor
    suspend fun execute(
        context: ICommandContext,
        @Argument(optional = true) user: User?,
        @Argument(optional = true) username: String?
    ) {
        if(user != null) {
            if(username != null && username.trim() != "") {
                val mojangProfile = Mojang.getMojangProfile(username)
                if(mojangProfile != null) {
                    val userData = Bot.database.userRepository.getUser(uuid = mojangProfile.id)
                    if(userData.id == null) {
                        userData.id = user.id
                        Bot.database.userRepository.updateId(userData, user.id)

                        val playerData = Hypixel.getPlayerData(mojangProfile.id)
                        if(playerData != null) {
                            if(
                                playerData.discordTag != null &&
                                playerData.discordTag!!.lowercase() == user.asTag.lowercase()
                            ) {
                                userData.uuid = mojangProfile.id
                                userData.hypixel = playerData
                                bot.Modules.registration.User.updateUser(context.guild!!, userData)
                                context.reply(
                                    EmbedTemplates
                                        .normal(
                                            "You have force linked ${user.asMention}'s Hypixel account successfully.",
                                            "Linked $username"
                                        )
                                        .setAuthor("",user.avatarUrl)
                                        .build()
                                ).queue()
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
                    EmbedTemplates
                        .error("Please provide the username you would like to force register!")
                        .build()
                ).queue()
            }
        } else {
            context.reply(
                EmbedTemplates
                    .error("Please tag the user you would like to force register!")
                    .build()
            ).queue()
        }
    }
}