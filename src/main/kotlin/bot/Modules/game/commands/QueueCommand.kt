package bot.Modules.game.commands

import bot.Bot
import bot.Core.database.models.*
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.Modules.game.Game
import bot.api.ICommandContext
import bot.utils.Config
import dev.minn.jda.ktx.await

class QueueCommand: Command() {
    override val name = "queue"
    override val description = "Queue up to join any available games."
    override var aliases = arrayOf("q")

    @Executor
    suspend fun execute(
        context: ICommandContext,
        @Argument(optional = false) mode: String?
    ) {
        if(context.channel.id == Config.Channels.queueCommandsChannel) {
            val member = context.guild?.retrieveMemberById(context.author.id)?.await()
            if(member?.voiceState?.channel?.id == Config.Channels.queueRoomChannel) {
                val userData = context.getUserData()
                if(userData.uuid != null) {
                    val party = Bot.database.partyRepository.findPartyWithPlayer(member.id)
                    if(party != null) {
                        val partyLeader = party.players.find { it.leader }
                        if(partyLeader?.playerId == member.id) {
                            if(party.players.size > 1) {
                                Game.createGame(context.guild!!, party.players)
                            } else {
                                context.reply(
                                    EmbedTemplates
                                        .error("You must have more than 1 person in your party to queue!")
                                        .build()
                                ).queue()
                            }
                        } else {
                            context.reply(
                                EmbedTemplates
                                    .error("Only party leaders are able to start games in a party.")
                                    .build()
                            ).queue()
                        }
                    } else {
                        var gameType: GameType? = null
                        if(mode != null) {
                            when {
                                mode.contains("duo|two".toRegex()) -> gameType = GameType.DUOS
                                mode.contains("trio|three".toRegex()) -> gameType = GameType.TRIOS
                                mode.contains("squad|quad|four".toRegex()) -> gameType = GameType.FOURS
                            }
                        }

                        if(gameType != null) {
                            val alreadyQueued = Bot.database.queueRepository.findQueue(context.author.id, null)
                            if(alreadyQueued == null) {
                                val playerSearch = Bot.database.queueRepository.searchForPlayers(userData, gameType)
                                val queuePlayer = Player(false, member.id, userData.uuid)

                                playerSearch?.add(queuePlayer)
                                Bot.database.queueRepository.createQueue(
                                    queuePlayer,
                                    userData.score,
                                    userData.ignoredList,
                                    gameType
                                )

                                if(playerSearch != null) {
                                    Game.createGame(context.guild!!, playerSearch)
                                } else {
                                    context.reply(
                                        EmbedTemplates
                                            .error("We were unable to find a game instantly, you have been added to the queue!")
                                            .build()
                                    ).queue()
                                }
                            } else {
                                context.reply(
                                    EmbedTemplates
                                        .error("You already created a queue, please be patient!")
                                        .build()
                                ).queue()
                            }
                        } else {
                            context.reply(
                                EmbedTemplates
                                    .error("Please provide one of the following modes: `duo/trio/fours`.")
                                    .build()
                            ).queue()
                        }
                    }
                } else {
                    context.reply(
                        EmbedTemplates
                            .error("You must register before running this command!")
                            .build()
                    ).queue()
                }
            } else {
                val queueRoomChannel = context.guild?.getVoiceChannelById(Config.Channels.queueRoomChannel)
                if(queueRoomChannel != null) {
                    context.reply(
                        EmbedTemplates
                            .error("Please join the **${queueRoomChannel.name}** channel in order to queue!")
                            .build()
                    ).queue()
                } else {
                    context.reply(
                        EmbedTemplates
                            .error("The queue voice channel seems to be deleted, please notify an administrator!")
                            .build()
                    ).queue()
                }
            }
        }
    }
}