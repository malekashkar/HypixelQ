package bot.Modules.registration.commands

import bot.Core.structures.base.Command
import bot.api.ICommandContext

class ForceRegisterCommand: Command() {
    override val name = "forgeregister"

    @Executor
    suspend fun execute(context: ICommandContext) {

    }
}