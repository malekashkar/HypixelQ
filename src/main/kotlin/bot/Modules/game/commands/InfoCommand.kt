package bot.Modules.game.commands

import bot.Bot
import bot.Core.database.models.User
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import bot.utils.api.Hypixel
import bot.utils.api.Mojang
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.entities.User as JDAUser

class InfoCommand: Command() {
    override val name = "info"
    override val description = "Check your own or any bedwars players stats."
    override var aliases = arrayOf("i")

    @Executor
    suspend fun execute(
        context: ICommandContext,
        @Argument(optional = true) user: JDAUser?,
        @Argument(optional = true) username: String?
    ) {
        var discordUser: JDAUser? = user
        var userData: User? = null

        if(username != null) {
            val mojangProfile = Mojang.getMojangProfile(username)
            if(mojangProfile != null) {
                userData = Bot.database.userRepository.getUser(uuid = mojangProfile.id)
                if(userData.id != null) {
                    discordUser = context.jda.retrieveUserById(userData.id!!).await()
                }
                if(userData.hypixel == null) {
                    val hypixelData = Hypixel.getPlayerData(mojangProfile.id)
                    if(hypixelData != null) {
                        userData.hypixel = hypixelData
                        Bot.database.userRepository.updateUuid(userData, mojangProfile.id)
                        Bot.database.userRepository.updateHypixelData(userData, hypixelData)
                    }
                }
            }
        } else if(user != null) {
            userData = Bot.database.userRepository.getUser(user.id)
        } else {
            userData = context.getUserData()
            discordUser = context.author
        }

        if(userData?.hypixel == null && userData?.uuid != null) {
            val hypixelData = Hypixel.getPlayerData(userData.uuid!!)
            if(hypixelData != null) {
                userData.hypixel = hypixelData
                Bot.database.userRepository.updateHypixelData(userData, hypixelData)
            }
        }

        if(userData?.hypixel != null) {
            context.reply(
                EmbedTemplates
                    .empty()
                    .addField("Discord", discordUser?.asTag ?: "N/A", true)
                    .addField("Username", userData.hypixel?.displayName ?: "N/A", true)
                    .addField("UUID", userData.uuid ?: "N/A", true)
                    .addField("FKDR", "%.2f".format(userData.hypixel?.stats?.fkdr) , true)
                    .addField("Bedwars Level", userData.hypixel!!.stats?.level.toString(), true)
                    .addField("Winstreak", userData.hypixel!!.stats?.winstreak.toString(), true)
                    .build()
            ).queue()
        } else {
            context.reply(
                EmbedTemplates
                    .error("We were unable to find the provided users information!")
                    .build()
            ).queue()
        }
    }
}