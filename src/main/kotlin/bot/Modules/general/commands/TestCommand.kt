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
        val gameCount = Bot.database.archiveRepository.gameCountWeeklyLeaderboard()
        val gameLength = Bot.database.archiveRepository.gameLengthWeeklyLeaderboard()
        println(gameCount)
        println(gameLength)
    }
}