package bot.Modules.general

import bot.Bot
import bot.Core.structures.base.Module
import bot.Modules.general.commands.HelpCommand
import bot.Modules.general.commands.InfoCommand
import bot.Modules.general.commands.MaintenanceCommand
import java.util.*

class GeneralModule(bot: Bot) : Module(
    bot,
    arrayOf(
        MaintenanceCommand(),
        InfoCommand(),
        HelpCommand()
    ),
), EventListener {
    override val name = "General"
}