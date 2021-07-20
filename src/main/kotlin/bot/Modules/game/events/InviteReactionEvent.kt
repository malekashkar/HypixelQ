package bot.Modules.game.events

import bot.Bot
import bot.Core.database.models.Player
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Event
import bot.utils.Config
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import org.litote.kmongo.util.idValue

class InviteReactionEvent: Event() {
    override val name = "invitereaction"

    @Handler
    suspend fun handle(event: ButtonClickEvent) {
        if(event.button != null) {
            if(event.button!!.id == "accept-party") {
                event.message?.delete()?.queue()
                event.deferEdit().queue()

                val partyInviteData = Bot.database.partyInviteRepository.findInvite(inviteMessageId = event.messageId)
                if(partyInviteData != null) {
                    val playerData = Bot.database.userRepository.getUser(event.user.id)
                    if(playerData.uuid != null) {
                        val player = Player(event.user.id, playerData.uuid)
                        val pastParty = Bot.database.partyRepository.findPartyWithPlayer(player)
                        if (pastParty == null) {
                            val partyInviterData = Bot.database.userRepository.getUser(partyInviteData.inviterId)
                            if(partyInviterData.uuid != null) {
                                val party = Bot.database.partyRepository.getParty(Player(partyInviteData.inviterId, partyInviterData.uuid))
                                val timeAgo = event.message?.timeCreated?.toInstant()?.toEpochMilli()
                                    ?.minus(System.currentTimeMillis()) ?: 0
                                if (timeAgo < Config.Intervals.partyInviteExpire) {
                                    Bot.database.partyRepository.addPartyPlayer(party, player)
                                    Bot.database.partyInviteRepository.deleteInvite(inviteMessageId = event.messageId)

                                    try {
                                        event.channel.sendMessageEmbeds(
                                            EmbedTemplates.normal(
                                                "You join the party successfully!",
                                                "Party Joined"
                                            ).build()
                                        ).queue()

                                        val inviter = Bot.jda.retrieveUserById(partyInviteData.inviterId).await()
                                        val inviterDmChannel = inviter.openPrivateChannel().await()

                                        inviterDmChannel?.sendMessageEmbeds(
                                            EmbedTemplates.normal(
                                                "${event.user.asMention} joined your party!",
                                                "User Joined Party"
                                            ).build()
                                        )?.queue()
                                    } catch (err: Throwable) {
                                        println(err)
                                    }
                                } else {
                                    event.channel.sendMessageEmbeds(
                                        EmbedTemplates
                                            .error("The party invite has expired!")
                                            .build()
                                    ).queue()
                                }
                            }
                        } else {
                            Bot.database.partyInviteRepository.deleteInvite(inviteMessageId = event.messageId)
                            event.channel.sendMessageEmbeds(
                                EmbedTemplates
                                    .error("You're already in a party, you may not join another until you leave!")
                                    .build()
                            ).queue()
                        }
                    }
                }
            } else if(event.button!!.id == "deny-party") {
                event.message?.delete()?.queue()
                event.deferEdit().queue()

                val partyInviteData = Bot.database.partyInviteRepository.findInvite(inviteMessageId = event.messageId)
                if(partyInviteData != null) {
                    Bot.database.partyInviteRepository.deleteInvite(inviteMessageId = event.messageId)
                }
            }
        }
    }
}