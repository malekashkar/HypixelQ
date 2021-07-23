package bot.Modules.general.commands

import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import bot.utils.Config
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button

class AutoRoleCommand: Command() {
    override val name = "autorole"
    override val description = "Send the autorole message in a channel."

    override var requiredUserPermissions = arrayOf(Permission.MANAGE_SERVER)

    data class RoleInformation(
        val description: String,
        val button: Button,
    )

    @Executor
    suspend fun execute(context: ICommandContext) {
        val information = Config.autoRoles.mapNotNull {
            val role = context.guild!!.getRoleById(it.roleId)
            if(role != null) {
                RoleInformation(
                    "${it.emoji} **${role.name}**\n`•` ${it.description}",
                    Button.secondary(it.roleId, "${it.emoji} ${role.name}")
                )
            } else {
                null
            }
        }
        val description = information.joinToString("\n\n") { it.description }
        val buttons = information.map { it.button }.chunked(5).map { ActionRow.of(it) }

        val rolesChannel = context.guild!!.getTextChannelById(Config.Channels.autoRolesChannel)
        rolesChannel
            ?.sendMessageEmbeds(EmbedTemplates.normal(description, "Auto Roles Menu").build())
            ?.setActionRows(buttons)
            ?.await()
    }
}