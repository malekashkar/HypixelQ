package bot.Core.structures

import bot.Core.structures.base.BaseCommandContext
import net.dv8tion.jda.api.EmbedBuilder

object EmbedTemplates {
  enum class Color(val code: Int) {
    RED(0xf04747),
    BASE(0x2f3136)
  }

  fun empty() = EmbedBuilder().setColor(Color.BASE.code)

  fun error(description: String, title: String? = null): EmbedBuilder {
    val embedTitle = title ?: "Something went wrong..."
    return EmbedBuilder().setColor(Color.RED.code).setTitle(embedTitle).setDescription(description)
      .setFooter("Not sure what to do? Try contacting an administrator.")
  }

  fun normal(description: String, title: String? = null): EmbedBuilder {
    return empty().setDescription(description).setTitle(title)
  }
}
