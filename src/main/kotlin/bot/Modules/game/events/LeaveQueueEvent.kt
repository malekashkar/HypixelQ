package bot.Modules.game.events

import bot.Bot
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Event
import bot.utils.Config
import dev.minn.jda.ktx.Message
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent

class LeaveQueueEvent: Event() {
    override val name = "leavequeue"

    @Handler
    suspend fun handle(event: GuildVoiceLeaveEvent) {
        if(event.channelLeft.id == Config.Channels.queueRoomChannel) {
            val queueData = Bot.database.queueRepository.findQueue(event.member.id)
            if(queueData != null) {
                Bot.database.queueRepository.deleteQueue(event.member.id)
                val queueCommandsChanel = event.guild.getTextChannelById(Config.Channels.queueCommandsChannel)
                queueCommandsChanel?.sendMessage(
                    Message(
                        event.member.asMention,
                        EmbedTemplates
                            .error("You have been taken out of the queue for leaving the **${event.channelLeft.name}** channel!")
                            .build()
                    )
                )?.queue()
            }
        }
    }
}