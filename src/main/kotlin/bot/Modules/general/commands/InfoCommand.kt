package bot.Modules.general.commands

import bot.Bot
import bot.Core.database.models.HypixelData
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import bot.utils.api.Hypixel
import bot.utils.api.Mojang
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.entities.User

class InfoCommand: Command() {
    override val name = "info"
    override val description = "Check your own or any bedwars players stats."
    override var aliases = arrayOf("i")

    @Executor
    suspend fun execute(
        context: ICommandContext,
        @Argument(optional = true) user: User?,
        @Argument(optional = true) username: String?
    ) {
        var hypixelData: HypixelData? = null
        var discordUser: User? = null
        var uuid: String? = null
        var mcUsername: String? = null

        if(username != null) {
            val mojangProfile = Mojang.getMojangProfile(username)
            if(mojangProfile != null) {
                uuid = mojangProfile.id
                mcUsername = mojangProfile.name
                val userData = Bot.database.userRepository.getUser(mojangUuid = mojangProfile.id)
                if(userData.id != null) {
                    discordUser = context.jda.retrieveUserById(userData.id!!).await()
                    hypixelData = userData.hypixelData
                } else {
                    val hypixelPlayerData = Hypixel.getPlayerData(mojangProfile.id)
                    if(hypixelPlayerData != null) {
                        Bot.database.userRepository.updateUuid(userData, mojangProfile.id)
                        Bot.database.userRepository.updateStats(userData, hypixelPlayerData.statsData)
                    }
                }
            }
        } else if(user != null) {
            val userData = Bot.database.userRepository.getUser(user.id)
            if(userData.uuid != null) {
                uuid = userData.uuid
                mcUsername = Mojang.getCurrentName(userData.uuid!!)
                discordUser = user
                hypixelData = userData.hypixelData
            }
        } else {
            val userData = context.getUserData()
            if(userData.uuid != null) {
                uuid = userData.uuid
                mcUsername = Mojang.getCurrentName(userData.uuid!!)
                discordUser = context.author
                hypixelData = userData.hypixelData
            }
        }

        if(hypixelData != null) {
            context.reply(
                EmbedTemplates
                    .empty()
                    .addField("Discord", discordUser?.asTag ?: "N/A", true)
                    .addField("Username", mcUsername ?: "N/A", true)
                    .addField("UUID", uuid ?: "N/A", true)
                    .addField("FKDR", "%.2f".format(hypixelData.fkdr) , true)
                    .addField("Bedwars Level", hypixelData.bedwarsLevel.toString(), true)
                    .addField("Winstreak", hypixelData.winstreak.toString(), true)
                    .build()
            ).queue()
        } else {
            context.reply(
                EmbedTemplates
                    .error("Please make sure that the provided user has registered with our discord bot!")
                    .build()
            ).queue()
        }
    }
}