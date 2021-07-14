package bot.Core.structures

import bot.Bot
import bot.Core.structures.base.BaseCommandContext
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import bot.utils.Config
import bot.utils.extensions.*
import dev.minn.jda.ktx.CoroutineEventListener
import dev.minn.jda.ktx.await
import kotlinx.coroutines.*
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions

class CommandHandler(val bot: Bot) : CoroutineEventListener {
  private val coroutineScope = CoroutineScope(Executors.newCachedThreadPool().asCoroutineDispatcher())

  override suspend fun onEvent(event: GenericEvent) {
    when (event) {
      is ButtonClickEvent -> onButtonClick(event)
      is SlashCommandEvent -> onSlashCommand(event)
      is MessageReceivedEvent -> onMessageReceived(event)
    }
  }

  private fun onButtonClick(event: ButtonClickEvent) {
    coroutineScope.launch {
      delay(2500)
      try {
        if (!event.isAcknowledged) event.deferEdit().setActionRows().await()
      } catch (ignored: Throwable) {
      }
    }
  }

  private suspend fun onSlashCommand(event: SlashCommandEvent) {
    val context = SlashCommandContext(bot, event)
    try {
      if (!context.shouldProcess()) return
      if (bot.maintenance && !Config.devs.contains(context.author.id)) return

      var command: Command? = null
      for (module in bot.modules.values) {
        command = module.commandMap[event.name.lowercase()]
        if (command != null) break
      }

      if (command == null) return

      val userData = context.getUserData()

      if (event.isFromGuild) {
        if (!event.guild!!.selfMember.permissions.containsAll(command.requiredClientPermissions.toList())) {
          return
        } else if (event.member != null) {
          if (!event.member!!.permissions.containsAll(command.requiredUserPermissions.toList())) {
            return
          }
        }
      }

      if (!command.canRun(context)) return

      val executorFunction =
        command.javaClass.kotlin.memberFunctions.find { it.annotations.any { annotation -> annotation is Command.Executor } }
          ?: return

      val parameters = executorFunction.parameters.filter { it.kind == KParameter.Kind.VALUE }
      val parsedParameters = arrayListOf<Any?>()

      for (param in parameters) {
        val commandArgumentAnnotation = param.findAnnotation<Command.Argument>()
        if (commandArgumentAnnotation != null) {
          if (event.options.isEmpty()) {
            parsedParameters.add(null)
          } else {
            val option = event.getOption(commandArgumentAnnotation.name.ifEmpty { param.name!! })
            val parsedParam: Any? = when {
              param.type.isInteger -> option?.asLong?.toInt()
              param.type.isBoolean -> option?.asBoolean
              param.type.isString -> option?.asString
              param.type.isRegex -> option?.asString?.toRegex()
              param.type.isUser -> option?.asUser
              param.type.isMember -> option?.asMember
              param.type.isRole -> option?.asRole
              param.type.isTextChannel -> option?.asGuildChannel?.let { if (it !is TextChannel) null else it }
              param.type.isVoiceChannel -> option?.asGuildChannel?.let { if (it !is VoiceChannel) null else it }
              else -> null
            }
            parsedParameters.add(parsedParam)
          }
        } else {
          when {
            param.type.isMessageCommandContext -> {
              parsedParameters.add(context)
            }
            param.type.isCommandContext -> {
              parsedParameters.add(context as ICommandContext)
            }
            param.type.isBaseCommandContext -> {
              parsedParameters.add(context as BaseCommandContext)
            }
            param.type.isMessageReceivedEvent -> {
              parsedParameters.add(event)
            }
            else -> {
              throw UnsupportedParameterException(param.name.toString())
            }
          }
        }
      }

      coroutineScope.launch {
        try {
          if (executorFunction.isSuspend) {
            executorFunction.callSuspend(command, *parsedParameters.toTypedArray()) // TODO: args
          } else {
            executorFunction.call(command, *parsedParameters.toTypedArray()) // TODO: args
          }
        } catch (e: Throwable) {
          context.handleException(e, command.module, command)
        }
      }
    } catch (e: Throwable) {
      context.handleException(e)
    }
  }

  @Suppress("UNUSED")
  suspend fun onMessageReceived(event: MessageReceivedEvent) {
    val context = MessageCommandContext(bot, event)
    try {
      if (!context.shouldProcess()) return
      if (bot.maintenance && !Config.devs.contains(context.author.id)) return

      val effectivePrefix = context.getPrefix()
      val splitMessage = event.message.contentRaw.split("\\s|\\n".toRegex()).toMutableList()
      var commandString = splitMessage.removeFirst()
      if (!commandString.startsWith(effectivePrefix, true)) return

      commandString = commandString.drop(effectivePrefix.length).trim().ifEmpty {
        splitMessage.removeFirstOrNull() ?: return
      }

      var command: Command? = null
      for (module in bot.modules.values) {
        command = module.commandMap[commandString.lowercase()]
        if (command != null) break
      }

      if (command != null && command.enabled) {
        if (splitMessage.size >= 1) {
          val childCommand =
            command.module.commandMap["${command.name.lowercase()}.${splitMessage.first().lowercase()}"]
          if (childCommand != null) {
            command = childCommand
            splitMessage.removeAt(0)
          }
        }
      }

      if (command == null || !command.enabled) return

      val userData = context.getUserData()

      if (event.isFromGuild) {
        if (!event.guild.selfMember.permissions.containsAll(command.requiredClientPermissions.toList())) {
          return
        } else if (event.member != null) {
          if (!event.member!!.permissions.containsAll(command.requiredUserPermissions.toList())) {
            return
          }
        }
      }

      if (!command.canRun(context)) return

      val executorFunction =
        command.javaClass.kotlin.memberFunctions.find { it.annotations.any { annotation -> annotation is Command.Executor } }

      if (executorFunction != null) {
        val parameters = executorFunction.parameters.filter { it.kind == KParameter.Kind.VALUE }
        val parsedParameters = arrayListOf<Any?>()
        val argumentParser =
          ArgumentParser(context, splitMessage)

        for (param in parameters) {
          val commandArgumentAnnotation = param.findAnnotation<Command.Argument>()
          if (commandArgumentAnnotation != null) {
            if (splitMessage.isEmpty()) {
              parsedParameters.add(null)
            } else {
              val consumeRest = commandArgumentAnnotation.consumeRest
              val isPrefixed = commandArgumentAnnotation.prefixed
              val optional = commandArgumentAnnotation.optional
              val argName =
                commandArgumentAnnotation.name.ifEmpty { param.name!! }
              val argAliases = commandArgumentAnnotation.aliases
              argumentParser.provideArgumentInfo(
                consumeRest,
                isPrefixed,
                optional,
                param.type,
                argName,
                argAliases
              )
              when {
                param.type.isInteger -> {
                  parsedParameters.add(argumentParser.getInteger())
                }
                param.type.isBoolean -> {
                  parsedParameters.add(
                    argumentParser.getBoolean()
                  )
                }
                param.type.isString -> {
                  parsedParameters.add(argumentParser.getString())
                }
                param.type.isRegex -> {
                  parsedParameters.add(argumentParser.getRegex())
                }
                param.type.isUser -> {
                  parsedParameters.add(argumentParser.getUser())
                }
                param.type.isMember -> {
                  parsedParameters.add(argumentParser.getMember())
                }
                param.type.isRole -> {
                  parsedParameters.add(argumentParser.getRole())
                }
                param.type.isTextChannel -> {
                  parsedParameters.add(argumentParser.getTextChannel())
                }
                param.type.isVoiceChannel -> {
                  parsedParameters.add(argumentParser.getVoiceChannel())
                }
                else -> {
                  throw UnsupportedCommandArgumentException(param.name.toString())
                }
              }
            }
          } else {
            when {
              param.type.isMessageCommandContext -> {
                parsedParameters.add(context)
              }
              param.type.isCommandContext -> {
                parsedParameters.add(context as ICommandContext)
              }
              param.type.isBaseCommandContext -> {
                parsedParameters.add(context as BaseCommandContext)
              }
              param.type.isMessageReceivedEvent -> {
                parsedParameters.add(event)
              }
              else -> {
                throw UnsupportedParameterException(param.name.toString())
              }
            }
          }
        }

        val commandJob = coroutineScope.launch {
          try {
            if (executorFunction.isSuspend) {
              executorFunction.callSuspend(command, *parsedParameters.toTypedArray())
            } else {
              executorFunction.call(command, *parsedParameters.toTypedArray())
            }
          } catch (e: Throwable) {
            context.handleException(e, command.module, command)
          }
        }

        coroutineScope.launch {
          delay(1000)
          while (!commandJob.isCompleted) {
            event.channel.sendTyping().queue()
            delay(5000)
          }
        }

        coroutineScope.launch {
          val startedAt = System.currentTimeMillis()
          while (!commandJob.isCompleted) {
            delay(1000)
            if (System.currentTimeMillis() - startedAt >= 60_000) {
              commandJob.cancelAndJoin()
              context.reply("Command execution timed out, please try again later.").queue()
              break
            }
          }
        }
      }
    } catch (e: Throwable) {
      context.handleException(e)
    }
  }

  private class UnsupportedParameterException(parameterName: String) :
    Exception("Parameter $parameterName had an unsupported type.")

  private class UnsupportedCommandArgumentException(parameterName: String) :
    Exception("Command argument $parameterName had an unsupported type.")
}
