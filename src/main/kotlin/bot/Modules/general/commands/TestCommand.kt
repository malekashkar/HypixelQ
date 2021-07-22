package bot.Modules.general.commands

import bot.Bot
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import bot.utils.Config
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.Permission
import java.util.*

class TestCommand: Command() {
    override val name = "test"
    override val description = "This command is meant for only testing!"

    override var requiredUserPermissions = arrayOf(Permission.MANAGE_SERVER)

    @Executor
    suspend fun execute(context: ICommandContext) {
        //if(Calendar.DAY_OF_MONTH == 1) {
            val donatorRole = context.guild!!.getRoleById(Config.Roles.donatorRole)
            if(donatorRole != null) {
                val donatingMembers = context.guild!!.loadMembers().await().filter { it.roles.contains(donatorRole) }
                if(donatingMembers.isNotEmpty()) {
                    val announcementsChannel = context.guild!!.getTextChannelById(Config.Channels.announcementsChannel)
                    val announcementsRole = context.guild!!.getRoleById(Config.Roles.announcementsRole)
                    if(announcementsChannel != null && announcementsRole != null) {
                        announcementsChannel.sendMessage(
                            "Thank you to the following members of our community for donating this month!" +
                                    "\n\n${donatingMembers.joinToString { it.asMention }}" +
                                    "\n\n(${announcementsRole.asMention})"
                        ).queue()
                    }
                }
            }
        //}
    }
}