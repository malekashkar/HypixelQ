package bot.Modules.tickets.commands

import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import bot.utils.Config
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.interactions.components.Button

class CloseTicketCommand: Command() {
    override val name = "close"
    override val description = "Close your ticket channel."

    @Executor
    suspend fun execute(context: ICommandContext) {
        if((context.channel as? TextChannel)?.parent?.id == Config.Channels.supportCategory) {
            context.channel
                .sendMessageEmbeds(
                    EmbedTemplates
                        .normal("Please confirm you would like to close this channel.", "Close Confirmation")
                        .build()
                )
                .setActionRow(
                    Button.secondary("close-accept", Config.Emojis.accept),
                    Button.secondary("close-deny", Config.Emojis.deny)
                )
                .queue()
        }
    }
}