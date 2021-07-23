package bot.Modules.game

import bot.Bot
import bot.Core.structures.base.Module
import bot.Modules.game.commands.*
import bot.Modules.game.events.EndGameEvent
import bot.Modules.game.events.InviteReactionEvent
import bot.Modules.game.events.LeaveQueueEvent
import bot.Modules.game.events.QueueEvent
import bot.Modules.game.tasks.UpdateStatsChannelsTask
import net.dv8tion.jda.api.requests.GatewayIntent
import java.util.*

class GameModule(bot: Bot): Module(
    bot,
    arrayOf(
        QueueCommand(),
        EndGameCommand(),
        IgnoreCommand(),
        PartyCommand(),
        HistoryCommand(),
        PartyCommand(),
        PartyCommand.TransferCommand(),
        PartyCommand.DisbandCommand(),
        PartyCommand.InviteCommand(),
        PartyCommand.KickCommand(),
        PartyCommand.LeaveCommand(),
        PartyCommand.ListCommand()
    ),
    arrayOf(
        QueueEvent(),
        LeaveQueueEvent(),
        EndGameEvent(),
        InviteReactionEvent()
    ),
    arrayOf(
        UpdateStatsChannelsTask()
    ),
    arrayOf(
        GatewayIntent.GUILD_MEMBERS,
        GatewayIntent.GUILD_VOICE_STATES,
        GatewayIntent.DIRECT_MESSAGE_REACTIONS
    )
), EventListener {
    override val name = "Game"
}