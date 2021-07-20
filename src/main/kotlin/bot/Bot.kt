package bot

import bot.Core.database.Database
import bot.Core.structures.CommandHandler
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import bot.utils.Config
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import dev.minn.jda.ktx.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import bot.Core.structures.base.Module
import bot.Core.structures.base.ParentCommand
import bot.Modules.game.GameModule
import bot.Modules.general.GeneralModule
import bot.Modules.registration.RegistrationModule
import bot.Modules.tickets.TicketModule
import bot.Modules.welcome.WelcomeModule
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import net.dv8tion.jda.api.entities.Guild
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberFunctions

object Bot {
        lateinit var jda: JDA
        private lateinit var commandHandler: CommandHandler

        private lateinit var version: String

        private val devEnv = System.getenv("DEV").equals("true", true)
        private val TOKEN = System.getenv("TOKEN")

        val database: Database = Database()
        var modules = linkedMapOf<String, Module>()

        private var started = false
        var maintenance = devEnv

        val ktorClient by lazy {
                HttpClient {
                        install(JsonFeature) {
                                serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                                        ignoreUnknownKeys = true
                                })
                        }
                }
        }

        @JvmStatic
        fun main(args: Array<String>) {
                this.version = if (devEnv) "DEV" else Config.version
                val intents = mutableListOf(
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                )

                val botModules = listOf(
                        GeneralModule(this),
                        RegistrationModule(this),
                        WelcomeModule(this),
                        GameModule(this),
                        TicketModule(this)
                )
                botModules.forEach {
                        modules[it.name.lowercase()] = it
                }
                val moduleArray = modules.values.filter { it.enabled }.map { it }.toTypedArray()
                for (module in moduleArray) {
                        for (intent in module.intents) {
                                if (!intents.contains(intent)) {
                                        intents.add(intent)
                                }
                        }
                        module.load()
                }
                commandHandler = CommandHandler(this)
                val jdaBuilder = JDABuilder.createDefault(TOKEN)
                        .injectKTX()
                        .setStatus(OnlineStatus.DO_NOT_DISTURB)
                        .setActivity(Activity.playing("BedwarsQ | =info to begin!"))
                        .addEventListeners(commandHandler, *moduleArray)
                        .setEnabledIntents(intents)
                jda = jdaBuilder.build()
                started = true

        }

        fun getMainGuild(): Guild? {
                return jda.getGuildById(Config.mainServer)
        }

        private fun getModuleHelpEmbedLine(prefix: String, command: Command): String {
                val name =
                        (if (command.parentCommand != null) "${command.parentCommand!!.name} ${command.name}" else command.name).lowercase()
                return "**${prefix}${name}**${if (command.usage.isEmpty()) "" else " `${command.usage}` "}${if (command.description.isEmpty()) "" else " - ${command.description} "}"
        }

        fun getHelpEmbed(context: ICommandContext, module: Module, prefix: String = "p!"): EmbedBuilder? {
                val commandEntries: ArrayList<String> = arrayListOf()
                for (command in module.commands) {
                        if (!command.enabled) continue
                        if (!command.canRun(context)) continue
                        if (command::class.hasAnnotation<Command.ChildCommand>()) continue
                        if (command is ParentCommand) {
                                command.childCommands.forEach { childClass ->
                                        val childCommand = module.commands.find { it::class == childClass }
                                        if (childCommand != null) {
                                                if (!childCommand.excludeFromHelp) commandEntries.add(getModuleHelpEmbedLine(prefix, childCommand))
                                        }
                                }
                        }
                        if (!command.excludeFromHelp) commandEntries.add(getModuleHelpEmbedLine(prefix, command))
                }
                if (commandEntries.isNotEmpty()) {
                        return EmbedTemplates.normal(
                                commandEntries.joinToString("\n"),
                                "${module.name} Commands"
                        )
                }
                return null
        }

        fun getHelpEmbed(context: ICommandContext, command: Command): EmbedBuilder? {
                if (!command.enabled || command.excludeFromHelp || !command.canRun(context)) return null
                val descriptionLines = arrayListOf<String>()
                if (command.description.isNotEmpty()) {
                        descriptionLines.add("${command.description}\n")
                }
                if (command.usage.isNotEmpty()) {
                        descriptionLines.add("**Usage**: `${command.usage}`\n")
                }
                if (command.aliases.isNotEmpty()) {
                        val aliasesString = command.aliases.joinToString(", ") { "`${it}`" }
                        descriptionLines.add("**Aliases**: $aliasesString")
                        val executor = command::class.memberFunctions.find { it.hasAnnotation<Command.Executor>() }
                        if (executor != null) {
                                val argumentAliasesLines = executor.parameters
                                        .mapNotNull { param ->
                                                val annotation = param.findAnnotation<Command.Argument>() ?: return@mapNotNull null
                                                if (annotation.aliases.isEmpty()) null
                                                else "`${if (annotation.name == "") param.name else annotation.name}` - ${
                                                        annotation.aliases.joinToString(", ") { "`${it}`" }
                                                }"
                                        }
                                if (argumentAliasesLines.isNotEmpty()) {
                                        descriptionLines += "**Argument Aliases**\n" + argumentAliasesLines.joinToString(
                                                "\n"
                                        )
                                }
                        }
                }
                return EmbedTemplates.normal(
                        descriptionLines.joinToString("\n"),
                        "${command.parentCommand?.name ?: ""} ${command.name} Command - ${command.module.name}".trim()
                )
        }

        fun getHelpEmbeds(context: ICommandContext, prefix: String = "!"): List<EmbedBuilder> {
                return modules.mapNotNull { getHelpEmbed(context, it.value, prefix) }
        }

        fun getHelpEmbeds(context: ICommandContext, commands: List<Command>): List<EmbedBuilder> {
                return commands.mapNotNull { getHelpEmbed(context, it) }
        }
}