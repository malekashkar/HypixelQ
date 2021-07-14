package bot.Core.structures.base

abstract class Event {
  @Target(AnnotationTarget.FUNCTION)
  @Retention(AnnotationRetention.RUNTIME)
  annotation class Handler

  abstract val name: String

  lateinit var module: Module

  var enabled = true

  open fun onLoad() {}
}
