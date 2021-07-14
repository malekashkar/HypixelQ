package bot.Modules.general.commands

import bot.Core.structures.base.Command
import bot.api.ICommandContext

class InfoCommand: Command() {
    override val name = "info"

    @Executor
    fun execute(
        context: ICommandContext,
        @Argument(optional = false) uuid: String?
    ) {}
}