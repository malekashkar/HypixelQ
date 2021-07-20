package bot.Modules.tickets

import bot.Bot
import bot.Core.structures.base.Module
import bot.Modules.tickets.commands.AddCommand
import bot.Modules.tickets.commands.CloseTicketCommand
import bot.Modules.tickets.commands.CreatePanelCommand
import bot.Modules.tickets.commands.RemoveCommand
import bot.Modules.tickets.events.CloseTicketEvent
import bot.Modules.tickets.events.CreateTicketEvent
import java.util.*

class TicketModule(bot: Bot): Module(
    bot,
    arrayOf(
        CreatePanelCommand(),
        CloseTicketCommand(),
        AddCommand(),
        RemoveCommand()
    ),
    arrayOf(
        CreateTicketEvent(),
        CloseTicketEvent()
    )
), EventListener {
    override val name = "Ticket"
}