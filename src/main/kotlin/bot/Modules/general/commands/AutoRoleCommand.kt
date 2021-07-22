package bot.Modules.general.commands

import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import bot.utils.Config
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.components.Button

class AutoRoleCommand: Command() {
    override val name = "autorole"
    override val description = "Send the autorole message in a channel."

    override var requiredUserPermissions = arrayOf(Permission.MANAGE_SERVER)

    @Executor
    suspend fun execute(context: ICommandContext) {
        val rolesChannel = context.guild!!.getTextChannelById(Config.Channels.autoRolesChannel)

        val description = Config.autoRoles
            .mapNotNull {
                val role = context.guild!!.getRoleById(it.roleId)
                var text: String? = null
                if(role != null) {
                    text = "${it.emoji} **${role.name}**\n`•` ${it.description}"
                }
                text
            }
            .joinToString("\n\n")

        val buttons = Config.autoRoles.mapNotNull {
            context.guild!!.getRoleById(it.roleId)?.let { role ->
                Button.secondary(it.roleId, "${it.emoji} ${role.name}")
            }
        }

        val rolesMessage = rolesChannel
            ?.sendMessageEmbeds(EmbedTemplates.normal(description, "Auto-Roles Menu").build())
            ?.setActionRow(buttons)
            ?.await()
            // Send the auto roles message
            // Set the message in the config
    }
}