package bot.Modules.registration

import bot.Bot
import bot.Core.structures.base.Module
import bot.Modules.registration.commands.ForceRegisterCommand
import bot.Modules.registration.commands.RegisterCommand
import bot.Modules.registration.events.RegisterEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import java.util.*

class VerificationModule(bot: Bot) : Module(
    bot,
    arrayOf(
        RegisterCommand(),
        ForceRegisterCommand()
    ),
    arrayOf(
        RegisterEvent()
    ),
    arrayOf(),
    arrayOf(
        GatewayIntent.GUILD_MEMBERS
    )
)
, EventListener {
    override val name = "Registration"
}