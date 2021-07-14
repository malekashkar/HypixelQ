package bot.Core.structures.base

import bot.Bot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.requests.GatewayIntent
import bot.Core.structures.MessageCommandContext
import bot.utils.extensions.isMessageCommandContext
import java.util.concurrent.Executors
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaType

@Suppress("UNUSED", "COULD_BE_PRIVATE")
abstract class Module(
    val bot: Bot,
    val commands: Array<Command> = arrayOf(),
    val events: Array<Event> = arrayOf(),
    private val tasks: Array<Task> = arrayOf(),
    val intents: Array<GatewayIntent> = GatewayIntent.getIntents(GatewayIntent.DEFAULT).toTypedArray()
) : EventListener {
  abstract val name: String

  val commandMap = linkedMapOf<String, Command>()
  open var enabled = true
  private val eventMap = hashMapOf<String, Event>()
  private val taskMap = hashMapOf<String, Task>()
  private val coroutineScope = CoroutineScope(Executors.newCachedThreadPool().asCoroutineDispatcher())

  fun load() {
    for (command in commands) {
      val executorFunction =
        command.javaClass.kotlin.memberFunctions.find { it.annotations.any { annotation -> annotation is Command.Executor } }
          ?: continue

      var allConsumed = false

      for (param in executorFunction.parameters.filter { it.kind == KParameter.Kind.VALUE }) {
        val commandArgumentAnnotation = param.findAnnotation<Command.Argument>()
        if (commandArgumentAnnotation != null) {
          if (allConsumed) {
            throw IllegalArgumentException("Command arguments cannot appear after another command argument that has consumeRest = true.")
          }
          if (!param.type.isMarkedNullable) {
            throw IllegalArgumentException("All command arguments must be nullable.")
          }
          if (commandArgumentAnnotation.consumeRest) allConsumed = true
        }
      }
      command.module = this
      if (!command::class.hasAnnotation<Command.ChildCommand>()) {
        this.commandMap[command.name.lowercase()] = command
        for (alias in command.aliases) {
          this.commandMap[alias.lowercase()] = command
        }
        if (command is ParentCommand) {
          val childCommandClasses = command::class.nestedClasses.filter { it.hasAnnotation<Command.ChildCommand>() }
          for (childCommandClass in childCommandClasses) {
            val childCommand = command.module.commands.find { it::class == childCommandClass }
            if (childCommand != null) {
              childCommand.parentCommand = command
              command.childCommands.add(childCommand)
              this.commandMap["${command.name.lowercase()}.${childCommand.name.lowercase()}"] = childCommand
              for (alias in childCommand.aliases) {
                this.commandMap["${command.name.lowercase()}.${alias.lowercase()}"] = childCommand
              }
            }
          }
        }
      }
    }
    for (event in events) {
      event.module = this
      this.eventMap[event.name] = event
      event.onLoad()
    }
    for (task in tasks) {
      task.module = this
      taskMap[task.name] = task
    }
  }

  override fun onEvent(event: GenericEvent) {
    if (!enabled) return
    if (event is ReadyEvent) {
      for (task in tasks) {
        if (task.enabled) task.start()
      }
    }
    for (ev in events) {
      if (!ev.enabled) continue
      val handlerFunctions =
        ev.javaClass.kotlin.memberFunctions.filter { it.annotations.any { annotation -> annotation is Event.Handler } }
      for (handlerFunction in handlerFunctions) {
        val firstParam = handlerFunction.parameters.find { it.kind != KParameter.Kind.INSTANCE } ?: return
        try {
          if (firstParam.type.javaType == event::class.java) {
            if (handlerFunction.isSuspend) {
              coroutineScope.launch {
                handlerFunction.callSuspend(ev, event)
              }
            } else {
              handlerFunction.call(ev, event)
            }
          } else if (firstParam.type.isMessageCommandContext && event::class.java == MessageReceivedEvent::class.java) {
            val context = MessageCommandContext(bot, event as MessageReceivedEvent)
            if (handlerFunction.isSuspend) {
              coroutineScope.launch {
                handlerFunction.callSuspend(ev, context)
              }
            } else {
              handlerFunction.call(ev, context)
            }
          }
        } catch (e: Throwable) {
          e.printStackTrace()
        }
//        }
      }
    }
  }
}
