package bot.Modules.game.commands

import bot.Bot
import bot.Core.database.models.PartyInvite
import bot.Core.database.models.Player
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.components.Button

class InviteCommand: Command() {
    override val name = "invite"
    override val description = "Invite a player to your party"

    @Executor
    suspend fun execute(
        context: ICommandContext,
        @Argument(optional = false) user: User?
    ) {
        if(user != null) {
            val playerData = Bot.database.userRepository.getUser(discordId = user.id)
            if(playerData.uuid != null) {
                val party = Bot.database.partyRepository.findPartyWithPlayer(Player(playerData.id!!, playerData.uuid))
                if(party != null && party.leaderId == playerData.id) {
                    val pastInvite = Bot.database.partyInviteRepository.findInvite(context.author.id, user.id)
                    if(pastInvite == null) {
                        val dmChannel = user
                            .openPrivateChannel()
                            .await()

                        try {
                            val inviteMessage = dmChannel
                                .sendMessageEmbeds(
                                    EmbedTemplates.normal(
                                        "You have been invited to ${context.author.asMention} party on **${context.guild!!.name}**.",
                                        "Party Invite"
                                    ).build()
                                )
                                .setActionRow(
                                    listOf(
                                        Button.success("accept-party", "Accept"),
                                        Button.danger("deny-party", "Deny")
                                    )
                                )
                                .await()

                            Bot.database.partyInviteRepository.createInvite(
                                PartyInvite(inviteMessage.id, context.author.id, user.id)
                            )

                            context.reply(
                                EmbedTemplates
                                    .normal(
                                        "${user.asMention} has been invited to your party",
                                        "User Invited"
                                    )
                                    .build()
                            ).queue()
                        } catch (err: Throwable) {
                            println(err)
                            context.reply(
                                EmbedTemplates
                                    .error(
                                        "${user.asMention} has their DM's closed; you may not invite them to your party!"
                                    )
                                    .build()
                            ).queue()
                        }
                    } else {
                        context.reply(
                            EmbedTemplates
                                .error("You already invited this user to a party in the past!")
                                .build()
                        ).queue()
                    }
                } else {
                    context.reply(
                        EmbedTemplates
                            .error("Only party leaders are able to invite players!")
                            .build()
                    ).queue()
                }
            } else {
                context.reply(
                    EmbedTemplates
                        .error("Make sure the user registers before inviting them!")
                        .build()
                ).queue()
            }
        }
    }
}