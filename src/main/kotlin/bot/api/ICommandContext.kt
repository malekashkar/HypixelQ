package bot.api

import bot.Bot
import bot.Core.database.models.User
import bot.Core.structures.base.Command
import bot.Core.structures.base.Event
import bot.Core.structures.base.Module
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.requests.RestAction
import java.time.OffsetDateTime
import net.dv8tion.jda.api.entities.Guild as JDAGuild
import net.dv8tion.jda.api.entities.User as JDAUser

interface ICommandContext {
    val author: JDAUser
    val bot: Bot
    val channel: MessageChannel
    val event: GenericEvent
    val guild: JDAGuild?
    val isFromGuild: Boolean
    val jda: JDA
    val timeCreated: OffsetDateTime

    suspend fun getPrefix(): String
    suspend fun getUserData(forceFetch: Boolean = false): User
    suspend fun handleException(
        exception: Throwable,
        module: Module? = null,
        command: Command? = null,
        event: Event? = null,
        extras: Map<String, String> = mapOf()
    )

    fun addActionRows(vararg actionRows: ActionRow): ICommandContext
    fun clearActionRows(): ICommandContext

    suspend fun hasStarted(sendMessage: Boolean = false): Boolean
    fun reply(content: String, mentionRepliedUser: Boolean = false): RestAction<*>
    fun reply(embed: MessageEmbed, mentionRepliedUser: Boolean = false): RestAction<*>
    fun shouldProcess(): Boolean
}
