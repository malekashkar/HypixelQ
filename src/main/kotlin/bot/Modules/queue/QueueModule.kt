package bot.Modules.queue

import bot.Bot
import bot.Core.structures.base.Module
import bot.Modules.queue.commands.QueueCommand
import bot.Modules.queue.events.QueueEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import java.util.*

class QueueModule(bot: Bot): Module(
    bot,
    arrayOf(
        QueueCommand()
    ),
    arrayOf(
        QueueEvent()
    ),
    arrayOf(),
    arrayOf(
        GatewayIntent.GUILD_VOICE_STATES
    )
), EventListener {
    override val name = "Queue"
}