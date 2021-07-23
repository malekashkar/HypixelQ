package bot.Modules.general.commands

import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import bot.utils.Config
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button

class ColorRolesCommand: Command() {
    override val name = "colorroles"
    override val description = "Send the colored wool roles embed."

    @Executor
    suspend fun execute(context: ICommandContext) {
        val roles = Config.woolRoles.mapNotNull { context.guild!!.getRoleById(it) }
        val description = roles.joinToString("\n") { "`•` ${it.asMention}" }
        val buttons = roles.map { Button.secondary(it.id, it.name) }

        val rolesChannel = context.guild!!.getTextChannelById(Config.Channels.donatorColorRolesChannel)
        rolesChannel
            ?.sendMessageEmbeds(EmbedTemplates.normal(description, "Colored Roles Menu").build())
            ?.setActionRows(buttons.chunked(5).map { ActionRow.of(it) })
            ?.await()
    }
}