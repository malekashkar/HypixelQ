package bot.Modules.general.commands

import bot.Core.structures.base.Command
import bot.api.ICommandContext

class SettingsCommand: Command() {
    override val name = "settings"
    override var aliases = arrayOf("setting")

    @Executor
    suspend fun execute(context: ICommandContext) {
    }
}