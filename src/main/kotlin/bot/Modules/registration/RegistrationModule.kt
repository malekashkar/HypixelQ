package bot.Modules.registration

import bot.Bot
import bot.Core.structures.base.Module
import bot.Modules.registration.commands.ForceRegisterCommand
import bot.Modules.registration.commands.RegisterCommand
import bot.Modules.registration.commands.UpdateCommand
import bot.Modules.registration.events.RegisterEvent
import bot.Modules.registration.tasks.UpdateUsersTask
import net.dv8tion.jda.api.requests.GatewayIntent
import java.util.*

class RegistrationModule(bot: Bot) : Module(
    bot,
    arrayOf(
        RegisterCommand(),
        ForceRegisterCommand(),
        UpdateCommand()
    ),
    arrayOf(
        RegisterEvent()
    ),
    arrayOf(
        UpdateUsersTask()
    ),
    arrayOf(
        GatewayIntent.GUILD_MEMBERS
    )
)
, EventListener {
    override val name = "Registration"
}