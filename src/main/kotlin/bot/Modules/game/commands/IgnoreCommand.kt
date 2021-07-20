package bot.Modules.game.commands

import bot.Bot
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import bot.utils.api.Mojang
import net.dv8tion.jda.api.entities.User

class IgnoreCommand: Command() {
    override val name = "ignore"
    override val description = "Ignore a user in order not to be queued with them."

    @Executor
    suspend fun execute(
        context: ICommandContext,
        @Argument(optional = true) user: User?,
        @Argument(optional = true) username: String?
    ) {
        var uuid: String? = null
        var mcUsername: String? = username
        if(username != null) {
            val mojangProfile = Mojang.getMojangProfile(username)
            if(mojangProfile != null) {
                uuid = mojangProfile.id
                mcUsername = mojangProfile.name
            }
        } else if(user != null) {
            val userData = Bot.database.userRepository.getUser(user.id)
            if(userData.uuid != null) {
                uuid = userData.uuid
                mcUsername = Mojang.getCurrentName(userData.uuid!!)
            }
        }

        if(uuid != null) {
            val userData = Bot.database.userRepository.getUser(context.author.id)
            if(uuid != userData.uuid) {
                if(userData.ignoredList.contains(uuid)) {
                    Bot.database.userRepository.removeFromIgnoreList(userData, uuid)
                    context.reply(
                        EmbedTemplates
                            .normal(
                                "`$mcUsername` has been removed from your ignored list!",
                                "Ignored List Updated"
                            )
                            .build()
                    ).queue()
                } else {
                    Bot.database.userRepository.addToIgnoredList(userData, uuid)
                    context.reply(
                        EmbedTemplates
                            .normal(
                                "`$mcUsername` has been added onto your ignored list!",
                                "Ignored List Updated"
                            )
                            .build()
                    ).queue()
                }
            } else {
                context.reply(
                    EmbedTemplates
                        .error("You may not ignore yourself!")
                        .build()
                ).queue()
            }
        } else {
            context.reply(
                EmbedTemplates
                    .error("Please either tag a registered discord user or provide a Minecraft username to ignore.")
                    .build()
            ).queue()
        }
    }
}