package bot.Modules.general.events

import bot.Core.structures.base.Event
import bot.utils.Config
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent

class AutoRoleEvent: Event() {
    override val name = "autorole"

    @Handler
    fun handle(event: ButtonClickEvent) {
        if(event.button != null) {
            if(Config.autoRoles.map { it.roleId }.contains(event.button!!.id)) {
                event.deferEdit().queue()

                val role = event.button!!.id?.let { event.guild!!.getRoleById(it) }
                if(role != null && event.guild != null && event.member != null) {
                    if (event.member?.roles?.contains(role) == true) {
                        event.guild!!.removeRoleFromMember(event.member!!, role).queue()
                    } else {
                        event.guild!!.addRoleToMember(event.member!!, role).queue()
                    }
                }
            }
        }
    }
}