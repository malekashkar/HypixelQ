package bot.Modules.general.tasks

import bot.Bot
import bot.Core.structures.base.Task
import bot.utils.Config
import dev.minn.jda.ktx.await
import java.util.*

class DonatorsShoutoutTask: Task() {
    override val name = "donatorsshoutout"
    override val interval = Config.Intervals.donatorsShoutoutCheckInt

    override suspend fun execute() {
        if(Calendar.DAY_OF_MONTH == 1) {
            val guild = Bot.getMainGuild()
            if(guild != null) {
                val donatorRole = guild.getRoleById(Config.Roles.donatorRole)
                if(donatorRole != null) {
                    val donatingMembers = guild.loadMembers().await().filter { it.roles.contains(donatorRole) }
                    if(donatingMembers.isNotEmpty()) {
                        val announcementsChannel = guild.getTextChannelById(Config.Channels.announcementsChannel)
                        val announcementsRole = guild.getRoleById(Config.Roles.announcementsRole)
                        if(announcementsChannel != null && announcementsRole != null) {
                            announcementsChannel.sendMessage(
                                "Thank you to the following members of our community for donating this month!" +
                                        "\n\n${donatingMembers.joinToString { it.asMention }}" +
                                        "\n\n(${announcementsRole.asMention})"
                            ).queue()
                        }
                    }
                }
            }
        }
    }
}