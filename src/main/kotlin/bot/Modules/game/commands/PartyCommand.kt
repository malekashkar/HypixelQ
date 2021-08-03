package bot.Modules.game.commands

import bot.Bot
import bot.Core.database.models.PartyInvite
import bot.Core.database.models.Player
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.Core.structures.base.ParentCommand
import bot.api.ICommandContext
import bot.utils.Config
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.components.Button

class PartyCommand: ParentCommand() {
    override val name = "party"
    override var aliases = arrayOf("p")

    @ChildCommand
    class TransferCommand: Command() {
        override val name = "transfer"
        override val description = "Transfer party leader to someone else in your party."

        @Executor
        suspend fun execute(
            context: ICommandContext,
            @Argument(optional = false) user: User?
        ) {
            val party = Bot.database.partyRepository.findPartyWithPlayer(context.author.id)
            if(party != null) {
                if(user != null) {
                    if(party.players.find { it.leader }?.playerId == context.author.id) {
                        if(party.players.find { it.playerId == user.id } != null) {
                            Bot.database.partyRepository.transferLeadership(party, context.author.id, user.id)
                            context.reply(
                                EmbedTemplates
                                    .normal(
                                        "You transferred the party leader role to ${user.asMention}.",
                                        "Party Leader Transferred"
                                    )
                                    .build()
                            ).queue()
                        } else {
                            context.reply(
                                EmbedTemplates
                                    .error("The user you mention is not in your party!")
                                    .build()
                            ).queue()
                        }
                    } else {
                        context.reply(
                            EmbedTemplates
                                .error("You may not transfer the party if you are not party leader!")
                                .build()
                        ).queue()
                    }
                } else {
                    context.reply(
                        EmbedTemplates
                            .error("Please mention the user you would like to transfer party leader to.")
                            .build()
                    ).queue()
                }
            } else {
                context.reply(
                    EmbedTemplates
                        .error("You are not in a party currently!")
                        .build()
                ).queue()
            }
        }
    }

    @ChildCommand
    class InviteCommand: Command() {
        override val name = "invite"
        override val description = "Invite someone to your party."

        @Executor
        suspend fun execute(
            context: ICommandContext,
            @Argument(optional = false) user: User?
        ) {
            if(user != null) {
                val playerData = Bot.database.userRepository.getUser(discordId = user.id)
                if(playerData.uuid != null) {
                    val party = Bot.database.partyRepository.findPartyWithPlayer(playerData.id!!)
                    val leader = party?.players?.find { it.leader }
                    if(leader == null || context.author.id != leader.playerId) {
                        if(party == null || party.players.size < 4) {
                            val pastInvite = Bot.database.partyInviteRepository.findInvite(context.author.id, user.id)
                            if(
                                pastInvite == null ||
                                pastInvite.cratedAt - System.currentTimeMillis() < Config.Intervals.partyInviteExpire
                            ) {
                                try {
                                    val inviteMessage = user
                                        .openPrivateChannel()
                                        .await()
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

                                    val userData = context.getUserData()
                                    if(userData.uuid != null) {
                                        val leaderPlayer = Player(true, context.author.id, userData.uuid!!, userData.score)
                                        val invitedPlayer = Player(false, user.id, playerData.uuid!!, playerData.score)

                                        if(party == null) {
                                            Bot.database.partyRepository.createParty(mutableListOf(leaderPlayer))
                                        }
                                        Bot.database.partyInviteRepository.createInvite(
                                            PartyInvite(inviteMessage.id, leaderPlayer, invitedPlayer)
                                        )

                                        context.reply(
                                            EmbedTemplates
                                                .normal(
                                                    "${user.asMention} has been invited to your party",
                                                    "User Invited"
                                                )
                                                .build()
                                        ).queue()
                                    } else {
                                        context.reply(
                                            EmbedTemplates
                                                .error("You must register before inviting players to your party!")
                                                .build()
                                        ).queue()
                                    }
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
                                if(pastInvite.cratedAt - System.currentTimeMillis() >= Config.Intervals.partyInviteExpire) {
                                    Bot.database.partyInviteRepository.deleteInvite(inviteMessageId = pastInvite.inviteMessageId)
                                }
                                context.reply(
                                    EmbedTemplates
                                        .error("You already invited this user to a party in the past!")
                                        .build()
                                ).queue()
                            }
                        } else {
                            context.reply(
                                EmbedTemplates
                                    .error("You cannot have more than 4 people in a party!")
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
            } else {
                context.reply(
                    EmbedTemplates
                        .error("Please tag the user you would like to invite to your party!")
                        .build()
                ).queue()
            }
        }
    }

    @ChildCommand
    class KickCommand: Command() {
        override val name = "kick"
        override val description = "Kick someone from your party."

        @Executor
        suspend fun execute(
            context: ICommandContext,
            @Argument(optional = false) user: User?
        ) {
            if(user != null) {
                val userData = context.getUserData()
                if(userData.uuid != null) {
                    val party = Bot.database.partyRepository.findPartyWithPlayer(context.author.id)
                    if(party != null) {
                        val targetUserData = Bot.database.userRepository.getUser(discordId = user.id)
                        if(targetUserData.uuid != null) {
                            if(party.players.find { it.leader }?.playerId == context.author.id) {
                                when {
                                    context.author.id == user.id -> {
                                        context.reply(
                                            EmbedTemplates
                                                .error("You may not kick yourself from your own party!")
                                                .build()
                                        ).queue()
                                    }
                                    party.players.contains(party.players.find { it.playerId == user.id }) -> {
                                        Bot.database.partyRepository.removePartyPlayer(party, user.id)
                                        context.reply(
                                            EmbedTemplates
                                                .normal(
                                                    "You kicked ${user.asMention} from your party!",
                                                    "User Kicked"
                                                ).build()
                                        ).queue()
                                    }
                                    else -> {
                                        context.reply(
                                            EmbedTemplates
                                                .error("The user you mentioned is not in your party!")
                                                .build()
                                        ).queue()
                                    }
                                }
                            } else {
                                context.reply(
                                    EmbedTemplates
                                        .error("You are not the leader of your current party!")
                                        .build()
                                ).queue()
                            }
                        } else {
                            context.reply(
                                EmbedTemplates
                                    .error("The user you would like to kick is not registered.")
                                    .build()
                            ).queue()
                        }
                    } else {
                        context.reply(
                            EmbedTemplates
                                .error("You are not in a party currently to kick anyone from.")
                                .build()
                        ).queue()
                    }
                } else {
                    context.reply(
                        EmbedTemplates
                            .error("You may not run this command before registering.")
                            .build()
                    ).queue()
                }
            } else {
                context.reply(
                    EmbedTemplates
                        .error("Please tag the user you would like to kick from your party!")
                        .build()
                ).queue()
            }
        }
    }

    @ChildCommand
    class DisbandCommand: Command() {
        override val name = "disband"
        override val description = "Disband a party you've created."

        @Executor
        suspend fun execute(context: ICommandContext) {
            val playerData = Bot.database.userRepository.getUser(discordId = context.author.id)
            if(playerData.uuid != null) {
                val party = Bot.database.partyRepository.findPartyWithPlayer(playerData.id!!)
                if(party != null) {
                    if(party.players.find { it.leader }?.playerId == context.author.id) {
                        Bot.database.partyRepository.deleteParty(party)
                        context.reply(
                            EmbedTemplates
                                .normal(
                                    "Your party has been disbanded!",
                                    "Party Disbanded"
                                )
                                .build()
                        ).queue()
                    } else {
                        context.reply(
                            EmbedTemplates
                                .error("You may not disband this party because you are not the leader!")
                                .build()
                        ).queue()
                    }
                } else {
                    context.reply(
                        EmbedTemplates
                            .error("You are not in a party to disband currently.")
                            .build()
                    ).queue()
                }
            } else {
                context.reply(
                    EmbedTemplates
                        .error("Please sign up before running this command!")
                        .build()
                ).queue()
            }
        }
    }

    @ChildCommand
    class LeaveCommand: Command() {
        override val name = "leave"
        override val description = "Leave the party you are currently in."

        @Executor
        suspend fun execute(context: ICommandContext) {
            val playerData = Bot.database.userRepository.getUser(discordId = context.author.id)
            if(playerData.uuid != null) {
                val party = Bot.database.partyRepository.findPartyWithPlayer(playerData.id!!)
                val leader = party?.players?.find { it.leader }
                if(party != null && leader != null) {
                    if(leader.playerId == context.author.id) {
                        Bot.database.partyRepository.deleteParty(party)
                        context.reply(
                            EmbedTemplates
                                .normal(
                                    "Your party has been disbanded since you were leader!",
                                    "Party Disbanded"
                                )
                                .build()
                        ).queue()
                    } else {
                        Bot.database.partyRepository.removePartyPlayer(party, playerData.id!!)
                        context.reply(
                            EmbedTemplates
                                .normal(
                                    "You left the party you were in!",
                                    "Party Left"
                                )
                                .build()
                        ).queue()
                    }
                } else {
                    context.reply(
                        EmbedTemplates
                            .error("You are not in a party to leave currently.")
                            .build()
                    ).queue()
                }
            } else {
                context.reply(
                    EmbedTemplates
                        .error("Please sign up before running this command!")
                        .build()
                ).queue()
            }
        }
    }

    @ChildCommand
    class ListCommand: Command() {
        override val name = "list"
        override val description = "List all the members in your party currently."

        @Executor
        suspend fun execute(context: ICommandContext) {
            val playerData = Bot.database.userRepository.getUser(discordId = context.author.id)
            if(playerData.uuid != null) {
                val party = Bot.database.partyRepository.findPartyWithPlayer(playerData.id!!)
                if(party != null) {
                    val players = party.players.map { context.guild!!.retrieveMemberById(it.playerId).await() }
                    val leader = party.players.find { it.leader }
                    val playersMention = players.filter { it.id != leader?.playerId }.joinToString { it.asMention }
                    context.reply(
                        EmbedTemplates
                            .normal(
                                "Below is the list of players in your party.",
                                "Party Players"
                            )
                            .addField(
                                "Leader",
                                players.find { it.id == leader?.playerId }?.asMention,
                                true
                            )
                            .addField("Players", playersMention, true)
                            .build()
                    ).queue()
                } else {
                    context.reply(
                        EmbedTemplates
                            .error("You are not in a party currently.")
                            .build()
                    ).queue()
                }
            } else {
                context.reply(
                    EmbedTemplates
                        .error("Please sign up before running this command!")
                        .build()
                ).queue()
            }
        }
    }
}