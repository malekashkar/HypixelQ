package bot.Modules.tickets.commands

import bot.Bot
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import bot.utils.Config
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.interactions.components.Button

class CreatePanelCommand: Command() {
    override val name = "createpanel"
    override val description = "Create a panel where tickets may be opened from."

    override var requiredUserPermissions = arrayOf(Permission.MANAGE_SERVER)

    @Executor
    suspend fun execute(context: ICommandContext) {
        val buttonList = Config.ticketTypes.map {
            Button.secondary(it.label, Emoji.fromUnicode(it.emoji))
        }
        val text = Config.ticketTypes
            .map { "${it.emoji} → ${it.title}" }
            .joinToString { "\n" }
        val panelMessage = context.channel
            .sendMessageEmbeds(
            EmbedTemplates
                .normal(
                    "Click the reaction corresponding to your request.\n\n$text",
                    "Need Support?"
                )
                .build()
        )
            .setActionRow(buttonList)
            .await()
        Bot.database.configRepository.setTicketPanelId(panelMessage.id)
    }
}