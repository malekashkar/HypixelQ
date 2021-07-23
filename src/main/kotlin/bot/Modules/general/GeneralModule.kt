package bot.Modules.general

import bot.Bot
import bot.Core.structures.base.Module
import bot.Modules.general.commands.*
import bot.Modules.general.events.AutoRoleEvent
import bot.Modules.general.tasks.DonatorsShoutoutTask
import java.util.*

class GeneralModule(bot: Bot) : Module(
    bot,
    arrayOf(
        MaintenanceCommand(),
        InfoCommand(),
        HelpCommand(),
        AutoRoleCommand(),
        TestCommand(),
        ColorRolesCommand()
    ),
    arrayOf(
        AutoRoleEvent()
    ),
    arrayOf(
        DonatorsShoutoutTask()
    )
), EventListener {
    override val name = "General"
}