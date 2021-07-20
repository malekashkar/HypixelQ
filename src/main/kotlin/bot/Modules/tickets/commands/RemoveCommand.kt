package bot.Modules.tickets.commands

import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.Member

class RemoveCommand: Command() {
    override val name = "remove"
    override val description = "Remove someone from your ticket channel."

    @Executor
    suspend fun execute(
        context: ICommandContext,
        @Argument(optional = false) member: Member?
    ) {
        if(member != null) {
            (context.channel as? GuildChannel)
                ?.putPermissionOverride(member)
                ?.setDeny(
                    Permission.VIEW_CHANNEL,
                    Permission.MESSAGE_READ,
                    Permission.MESSAGE_HISTORY,
                    Permission.MESSAGE_WRITE
                )
                ?.queue()
            context.reply(
                EmbedTemplates
                    .normal(
                        "${member.asMention} has been removed from the ticket!",
                        "Member Removed"
                    )
                    .build()
            ).queue()
        } else {
            context.reply(
                EmbedTemplates
                    .error("Please tag the user you would like to remove from the ticket channel!")
                    .build()
            ).queue()
        }
    }
}