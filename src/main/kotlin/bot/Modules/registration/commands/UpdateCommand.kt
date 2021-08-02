package bot.Modules.registration.commands

import bot.Bot
import bot.Core.database.models.HypixelData
import bot.Core.database.models.User
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import bot.utils.api.Hypixel
import bot.utils.api.Mojang
import org.litote.kmongo.eq
import org.litote.kmongo.set
import org.litote.kmongo.setTo

class UpdateCommand: Command() {
    override val name = "update"
    override val description = "Update your Hypixel stats on your info profile."

    @Executor
    suspend fun execute(context: ICommandContext) {
        val userData = context.getUserData()
        if(userData.uuid != null) {
            if(userData.lastUpdated < System.currentTimeMillis() - 3_600_000L) {
                val hypixelData = Hypixel.getPlayerData(userData.uuid!!)
                if (hypixelData != null) {
                    val mojangProfile = Mojang.getMojangProfile(hypixelData.displayName!!)
                    if(mojangProfile?.id != null) {
                        userData.uuid = mojangProfile.id
                    }

                    userData.hypixel = hypixelData
                    bot.Modules.registration.User.updateUser(context.guild!!, userData)

                    context.reply(
                        EmbedTemplates
                            .normal(
                                "Your stats have been updated!",
                                "Stats Updated"
                            ).build()
                    ).queue()
                } else {
                    context.reply(
                        EmbedTemplates
                            .error("The Hypixel API seems to be offline currently, please try again later!")
                            .build()
                    ).queue()
                }
            } else {
                val lastUpdated = System.currentTimeMillis() - userData.lastUpdated!! // Format time
                context.reply(
                    EmbedTemplates
                        .error("Your stats were last updated $lastUpdated ago!\n" +
                                "You may only update your stats every hour!")
                        .build()
                ).queue()
            }
        } else {
            context.reply(
                EmbedTemplates
                    .error("You must register before running this command!")
                    .build()
            ).queue()
        }
    }
}