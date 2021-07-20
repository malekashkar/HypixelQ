package bot.Modules.registration.events

import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Event
import bot.utils.Config
import dev.minn.jda.ktx.Message
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.internal.utils.IOUtil
import java.awt.Color
import java.net.URL

class RegisterEvent : Event() {
    override val name = "registration"

    @Handler
    fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        if(event.member.user.isBot) return

        val registerChannel = event.guild.getTextChannelById(Config.Channels.registrationChannel)
        if(registerChannel != null) {
            registerChannel.sendMessage(
                Message(
                    event.member.asMention,
                    EmbedTemplates.normal(
                                "Link your discord using the following guide:\n" +
                                "1. Link your discord account to your Hypixel.\n" +
                                "2. Use the `${Config.prefix}register <MC Username>` command in this channel.",
                        "Registration Process"
                    )
                        .setColor(Color.getColor("#36393F"))
                        .build()
                )
            ).queue()

            val file = IOUtil.readFully(URL("https://cdn.discordapp.com/attachments/713825327670886500/864572511744163890/ezgif-3-5d765d3ec855.gif").openStream())
            registerChannel.sendFile(file, "verification.gif").queue()
        }
    }
}