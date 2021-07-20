package bot.Core.structures

import bot.Bot
import bot.Core.structures.base.BaseCommandContext
import bot.Core.structures.base.Command
import bot.Core.structures.base.Event
import bot.Core.structures.base.Module
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction
import java.time.OffsetDateTime

class SlashCommandContext(bot: Bot, override val event: SlashCommandEvent) : BaseCommandContext(bot) {
  override val author: User
    get() = event.user
  override val channel: MessageChannel
    get() = event.channel
  override val guild: Guild?
    get() = event.guild
  override val isFromGuild: Boolean
    get() = event.isFromGuild
  override val jda: JDA
    get() = event.jda
  override val timeCreated: OffsetDateTime
    get() = event.timeCreated

  private var replyDeferred = false

  private val actionRows = mutableListOf<ActionRow>()

  fun deferReply(): ReplyAction {
    replyDeferred = true
    return event.deferReply()
  }

  override fun addActionRows(vararg actionRows: ActionRow) = this.also { this.actionRows.addAll(actionRows) }
  override fun clearActionRows() = this.also { actionRows.clear() }

  override fun reply(content: String, mentionRepliedUser: Boolean): RestAction<*> {
    return when {
      replyDeferred -> event.hook.editOriginal(content).setActionRows(actionRows)
      else -> event.reply(content).mentionRepliedUser(mentionRepliedUser).addActionRows(actionRows)
    }
  }

  override fun reply(embed: MessageEmbed, mentionRepliedUser: Boolean): RestAction<*> {
    return when {
      replyDeferred -> event.hook.editOriginalEmbeds(embed).setActionRows(actionRows)
      else -> event.replyEmbeds(embed).mentionRepliedUser(mentionRepliedUser).addActionRows(actionRows)
    }
  }

  override suspend fun handleException(
    exception: Throwable,
    module: Module?,
    command: Command?,
    event: Event?,
    extras: Map<String, String>
  ) {
    val tmpExtras = mutableMapOf(
      "channelId" to this.event.channel.id,

      "slashCommand/commandId" to this.event.commandId,
      "slashCommand/commandPath" to this.event.commandPath,
      "slashCommand/interactionId" to this.event.interaction.id,
      "slashCommand/options" to this.event.options.joinToString(", ") { it.toString() },
      "slashCommand/token" to this.event.token,
    )

    this.event.subcommandGroup?.let {
      tmpExtras["slashCommand/subCommandGroup"] = it
    }
    this.event.subcommandName?.let {
      tmpExtras["slashCommand/subCommandName"] = it
    }

    super.handleException(
      exception,
      module,
      command,
      event,
      tmpExtras
    )
  }
}
