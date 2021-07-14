package bot.Modules.welcome

import bot.Bot
import bot.Core.structures.base.Module
import bot.Modules.welcome.events.WelcomeEvent
import java.util.*

class WelcomeModule(bot: Bot): Module(
    bot,
    arrayOf(),
    arrayOf(
        WelcomeEvent()
    ),
    arrayOf(),
    arrayOf()
), EventListener {
    override val name = "Welcome"
}