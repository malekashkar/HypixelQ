package bot.Modules.general.commands

import bot.Bot
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import net.dv8tion.jda.api.Permission

class MaintenanceCommand : Command() {
    override val name = "maintenance"
    override val description = "Put the discord bot under maintenance mode."

    override var requiredUserPermissions = arrayOf(Permission.MANAGE_SERVER)

    @Executor
    fun execute(
        context: ICommandContext
    ) {
        if(Bot.maintenance) {
            Bot.maintenance = false;
            context.reply(
                EmbedTemplates
                    .normal("Bot maintenance has been toggled off!", "Maintenance Toggle")
                    .build()
            ).queue()
        } else {
            Bot.maintenance = true;
            context.reply(
                EmbedTemplates
                    .normal("Bot maintenance has been toggled on!", "Maintenance Toggle")
                    .build()
            ).queue()
        }
    }
}