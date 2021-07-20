package bot.Core.structures.base

import bot.Bot
import bot.Core.database.models.User
import net.dv8tion.jda.api.entities.MessageEmbed
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import bot.Core.structures.EmbedTemplates
import bot.api.ICommandContext
import bot.utils.Config
import java.lang.reflect.InvocationTargetException

abstract class BaseCommandContext(override val bot: Bot) : ICommandContext {
  protected var userData: User? = null

  override suspend fun hasStarted(sendMessage: Boolean): Boolean {
    return true
  }

  override fun shouldProcess(): Boolean {
    if (author.isBot) return false
    return Config.devs.contains(author.id) || (if (Config.officialServerOnlyMode) isFromGuild && guild!!.id == Config.mainServer else true)
  }

  override suspend fun getUserData(forceFetch: Boolean): User {
    if (userData == null || forceFetch) {
      userData = bot.database.userRepository.getUser(author.id)
    }
    return userData!!
  }

  override suspend fun getPrefix(): String {
    return Config.prefix
  }

  override suspend fun handleException(
    exception: Throwable,
    module: Module?,
    command: Command?,
    event: Event?,
    extras: Map<String, String>
  ) {
    var errorEmbed: MessageEmbed? = null
    if (command != null) {
      errorEmbed = EmbedTemplates.error(
        "Failed to execute the ${command.name} command. ",
        "Command Execution Failed"
      )
        .build()
    }
    val message = when {
      command != null -> {
        "Error occurred while executing the ${command.name} command"
      }
      event != null -> {
        "Error occurred while handling the ${event.name} event"
      }
      else -> {
        "Error occurred while doing something unknown"
      }
    }
    logger.error(
      message,
      if (exception is InvocationTargetException) exception.targetException else exception
    )
    errorEmbed?.let { reply(it) }
  }

  companion object {
    protected val logger: Logger = LoggerFactory.getLogger(ICommandContext::class.java)
  }
}
