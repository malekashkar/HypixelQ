package bot.utils

object Config {
  data class TicketType(
    val title: String,
    val label: String,
    val emoji: String
  )

  data class AutoRole(
    val roleId: String,
    val emoji: String,
    val description: String
  )

  val woolRoles = listOf(
    "867445560633458688",
    "867445764836950087",
    "867445919980453898",
    "867446014440505406",
    "867446411082334238",
    "867446578268078080",
    "867446980937908246",
    "867447133225091162"
  )

  val ticketTypes = listOf(
    TicketType("General Support", "support", Emojis.supportTicket),
    TicketType("User Report", "report", Emojis.reportTicket)
  )

  const val prefix = "="
  const val version = "1.0.0"

  val devs =
    mutableListOf("584915458302672916")

  val autoRoles = listOf(
    AutoRole(Roles.bridgersRole, "\uD83C\uDF09", "Let your teammates know your the best bedwars bridger."), // Bridger
    AutoRole(Roles.announcementsRole, "\uD83C\uDFA4", "Be notified whenever we announce anything of less importance."), // Announcements
    AutoRole(Roles.updatesRole, "\uD83D\uDCEA", "Be notified when Hypixel or we come out with an update."), // Updates
    AutoRole(Roles.eventsRole, "\uD83C\uDF88", "Be notified whenever we create or start an event."), // Events
    AutoRole(Roles.giveawaysRole, "\uD83C\uDF89", "Be notified whenever we come out with a new giveaway."), // Giveaways
  )

  object Emojis {
    const val accept = "✅"
    const val deny = "❌"
    const val supportTicket = "\uD83C\uDFAB"
    const val reportTicket = "\uD83D\uDED1"
  }

  object Intervals {
    const val statsChannelUpdateInt = 300_000L
    const val userStatsUpdateInt = 10_800_000L
    const val partyInviteExpire = 600_000L
    const val donatorsShoutoutCheckInt = 86_400_000L
    const val closeInactiveGame = 120_000L
  }

  object Channels {
    const val supportCategory = "865291473612177428"

    const val totalGameStats = "865082667493097502"
    const val inQueueGameStats = "865083552676970496"
    const val inProgressGameStats = "865082532819369984"

    const val registrationProcessChannel = "864652159815516160"
    const val registrationChannel = "864294706832605224"
    const val welcomeChannel = "864652271832137768"

    const val announcementsChannel = "866025066373054526"
    const val autoRolesChannel = "866030855356153906"
    const val generalGuideChannel = "864714730529619998"
    const val liveSupportChannel = "864714786678505472"
    const val donatorColorRolesChannel = "867428894969167912"

    const val gameCountLb = "868519309561966633"
    const val gameTimeLb = "868519328176287774"
    const val commandsChannel = "864289771018911757"

    const val queueRoomChannel = "864703101067722782"
    const val queueCommandsChannel = "864703077650661376"
  }

  object Roles {
    const val bridgersRole = "867257881648037909"
    const val announcementsRole = "867258100582580234"
    const val updatesRole = "867258128902520832"
    const val eventsRole = "867258146866331668"
    const val giveawaysRole = "867258168936628235"

    const val donatorRole = "867428720234594384"
    const val registeredRole = "864597891262709780"
    const val vipRole = "865458492444901426"
    const val vipPlusRole = "865459582209753108"
    const val mvpRole = "865460245021982790"
    const val mvpPlusRole = "865459866734297099"
    const val mvpPlusPlusRole = "865460562578112523"
  }

  const val mainGuildId = "862795940205035530"
}
