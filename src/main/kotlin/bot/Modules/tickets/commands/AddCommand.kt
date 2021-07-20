package bot.Modules.tickets.commands

import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.Member

class AddCommand: Command() {
    override val name = "add"
    override val description = "Add someone to your ticket channel."

    @Executor
    suspend fun execute(
        context: ICommandContext,
        @Argument(optional = false) member: Member?
    ) {
        if(member != null) {
            (context.channel as? GuildChannel)
                ?.putPermissionOverride(member)
                ?.setAllow(
                    Permission.VIEW_CHANNEL,
                    Permission.MESSAGE_READ,
                    Permission.MESSAGE_HISTORY,
                    Permission.MESSAGE_WRITE
                )
                ?.queue()
            context.reply(
                EmbedTemplates
                    .normal(
                        "${member.asMention} has been added to the ticket!",
                        "Member Added"
                    )
                    .build()
            ).queue()
        } else {
            context.reply(
                EmbedTemplates
                    .error("Please tag the user you would like to add to the ticket channel!")
                    .build()
            ).queue()
        }
    }
}