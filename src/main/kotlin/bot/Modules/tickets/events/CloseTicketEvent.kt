package bot.Modules.tickets.events

import bot.Core.structures.base.Event
import bot.utils.Config
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent

class CloseTicketEvent: Event() {
    override val name = "closeticket"

    @Handler
    fun handle(event: ButtonClickEvent) {
        if(
            (event.channel as? TextChannel)?.parent?.id == Config.Channels.supportCategory &&
            event.button !== null
        ) {
            event.deferEdit().queue()
            if(event.button!!.id == "close-accept") {
                event.textChannel.delete().queue()
            } else if(event.button!!.id == "close-deny") {
                event.message?.delete()?.queue()
            }
        }
    }
}