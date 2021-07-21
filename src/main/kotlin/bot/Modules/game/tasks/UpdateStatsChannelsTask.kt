package bot.Modules.game.tasks

import bot.Bot
import bot.Core.structures.base.Task
import bot.utils.Config

class UpdateStatsChannelsTask: Task() {
    override val interval = Config.Intervals.statsChannelUpdateInt
    override val name = "UpdateStatsChannels"

    override suspend fun execute() {
        val guild = Bot.getMainGuild()
        if(guild != null) {
            val totalGameStatsChannel = guild.getVoiceChannelById(Config.Channels.totalGameStats)
            if(totalGameStatsChannel != null) {
                val totalGames = Bot.database.archiveCollection.countDocuments()
                totalGameStatsChannel.manager.setName("\uD83D\uDC65 Total: $totalGames").queue()
            }

            val inQueueGameStatsChannel = guild.getVoiceChannelById(Config.Channels.inQueueGameStats)
            if(inQueueGameStatsChannel != null) {
                val inQueueGames = Bot.database.queueCollection.countDocuments()
                inQueueGameStatsChannel.manager.setName("⌛ In-Queue: $inQueueGames").queue()
            }

            val inProgressGameStatsChannel = guild.getVoiceChannelById(Config.Channels.inProgressGameStats)
            if(inProgressGameStatsChannel != null) {
                val inProgressGames = Bot.database.gameCollection.countDocuments()
                inProgressGameStatsChannel.manager.setName("\uD83C\uDF89 In-Progress: $inProgressGames").queue()
            }
        }
    }
}