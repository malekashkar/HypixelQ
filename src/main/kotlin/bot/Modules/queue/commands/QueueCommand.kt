package bot.Modules.queue.commands

import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext

class QueueCommand: Command() {
    override val name = "queue"

    @Executor
    suspend fun execute(
        context: ICommandContext,
        @Argument(optional = false) mode: String?
    ) {
        if(mode == "duo") {
            // Queue them into duo
        } else if(mode == "trio") {
            // Queue them into trio
        } else if(mode == "fours") {
            // Queue them into fours
        } else {
            context.reply(
                EmbedTemplates
                    .error("Please provide one of the following modes: `duo/trio/fours`.")
                    .build()
            ).queue()
        }
    }
}