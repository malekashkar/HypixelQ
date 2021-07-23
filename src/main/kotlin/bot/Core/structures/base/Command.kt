package bot.Core.structures.base

import bot.api.ICommandContext
import bot.utils.extensions.isBoolean
import bot.utils.extensions.removeAccents
import net.dv8tion.jda.api.Permission
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions

abstract class Command {
  @Target(AnnotationTarget.FUNCTION)
  @Retention(AnnotationRetention.RUNTIME)
  annotation class Executor

  @Target(AnnotationTarget.VALUE_PARAMETER)
  @Retention(AnnotationRetention.RUNTIME)
  annotation class Argument(
    val consumeRest: Boolean = false,
    val name: String = "",
    val description: String = "\u200E",
    val aliases: Array<String> = [],
    val optional: Boolean = false,
    val prefixed: Boolean = false,
  )

  @Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
  @Retention(AnnotationRetention.RUNTIME)
  annotation class ChildCommand

  abstract val name: String
  lateinit var module: Module

  var parentCommand: Command? = null
  open val description: String? = null
  open var aliases: Array<String> = arrayOf()
  open var enabled = true
  open var excludeFromHelp = false
  open var requiredClientPermissions: Array<Permission> =
    arrayOf(Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)

  open var requiredUserPermissions: Array<Permission> = arrayOf(Permission.MESSAGE_READ)

  open val usage: String by lazy {
    this::class.memberFunctions.forEach { function ->
      val executorAnnotation = function.findAnnotation<Executor>()
      if (executorAnnotation != null) {
        var usageString = ""
        function.parameters.forEach { parameter ->
          val argumentAnnotation = parameter.findAnnotation<Argument>()
          if (argumentAnnotation != null) {
            if (usageString.isEmpty()) usageString += " "
            var prefix = if (argumentAnnotation.optional || argumentAnnotation.prefixed) "[" else "<"
            var suffix = if (argumentAnnotation.optional || argumentAnnotation.prefixed) "]" else ">"
            var argName = argumentAnnotation.name.ifEmpty { parameter.name }
            if (parameter.type.isBoolean) {
              if (argumentAnnotation.optional || argumentAnnotation.prefixed) {
                prefix = "["
                suffix = "]"
              } else {
                argName += " (true/false)"
              }
            }
            usageString += "$prefix${argName}$suffix "
          }
        }
        return@lazy usageString.trim()
      }
    }
    return@lazy ""
  }

  open fun canRun(context: ICommandContext): Boolean {
    return true
  }
}
