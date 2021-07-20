package bot.Modules.tickets.events

import bot.Bot
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Event
import bot.utils.Config
import dev.minn.jda.ktx.Message
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import java.util.concurrent.TimeUnit

class CreateTicketEvent: Event() {
    override val name = "createticket"

    @Handler
    suspend fun handle(event: ButtonClickEvent) {
        if(
            event.channel.id == Config.Channels.liveSupportChannel &&
            event.button !== null
        ) {
            val configData = Bot.database.configRepository.getConfig()
            if(configData.ticketPanelId == event.messageId) {
                event.deferEdit().queue()

                val supportCategory = event.guild!!.getCategoryById(Config.Channels.supportCategory)
                if(supportCategory != null) {
                    val type = Config.ticketTypes.find { it.emoji == event.button!!.emoji!!.name }?.label
                    val channel = supportCategory
                        .createTextChannel("$type-${event.user.name}")
                        .addPermissionOverride(event.guild!!.publicRole, mutableListOf(), mutableListOf(Permission.VIEW_CHANNEL))
                        .addMemberPermissionOverride(
                            event.member!!.idLong,
                            mutableListOf(
                                Permission.VIEW_CHANNEL,
                                Permission.MESSAGE_WRITE,
                                Permission.MESSAGE_READ,
                                Permission.MESSAGE_HISTORY
                            ),
                            mutableListOf()
                        )
                        .await()

                    if(type == "support") {
                        channel.sendMessage(
                            Message(
                                event.user.asMention,
                                EmbedTemplates
                                    .normal(
                                        "Welcome to the **${event.guild!!.name}** ticket system!\n\n" +
                                                "Please tell us what you would like support with today.\n" +
                                                "A staff member will be here to help you as soon as possible!",
                                        "Support Ticket Opened"
                                    )
                                    .build()
                            )
                        ).queue()
                    } else if(type == "report") {
                        channel.sendMessage(
                            Message(
                                event.user.asMention,
                                EmbedTemplates
                                    .normal(
                                        "Welcome to the **${event.guild!!.name}** ticket system!\n\n" +
                                                "Please provide as much information on the user you are reporting.\n" +
                                                "If you have any sort of proof, please provide that too!",
                                        "Report Ticket Opened"
                                    )
                                    .build()
                            )
                        ).queue()
                    }
                } else {
                    val errorMessage = event.channel.sendMessage(
                        Message(
                            event.user.asMention,
                            EmbedTemplates
                                .error("The support category seems to be deleted, please notify an administrator!")
                                .build()
                        )
                    ).await()
                    errorMessage.delete().queueAfter(10L, TimeUnit.SECONDS)
                }
            }
        }
    }
}