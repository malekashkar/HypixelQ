package bot.Modules.general.commands

import bot.Bot
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext

class MaintenanceCommand : Command() {
    override val name = "maintenance"

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