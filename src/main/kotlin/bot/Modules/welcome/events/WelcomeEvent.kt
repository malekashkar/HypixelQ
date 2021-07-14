package bot.Modules.welcome.events

import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Event
import bot.utils.Config
import dev.minn.jda.ktx.Message
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent

class WelcomeEvent: Event() {
    override val name = "Welcome"

    @Handler
    fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        val welcomeChannel = event.guild.getTextChannelById(Config.Channels.welcomeChannel)
        if(welcomeChannel !== null) {
            welcomeChannel.sendMessage(
                Message(
                    "Welcome to the **${event.guild.name}** discord, ${event.member.asMention} (**${event.guild.memberCount}th** member).",
                    EmbedTemplates
                        .normal(
                            "A place where you can queue up with other competitive driven Bedwars players",
                            "Welcome to ${event.guild.name}, **${event.member.user.name}**"
                        )
                        .setThumbnail(event.member.user.avatarUrl)
                        .build()
                )
            ).queue()
        }
    }
}