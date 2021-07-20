package bot.utils

object Config {
  data class TicketType(
    val title: String,
    val label: String,
    val emoji: String
  )

  val ticketTypes = listOf(
    TicketType("General Support", "support", Emojis.supportTicket),
    TicketType("User Report", "report", Emojis.reportTicket)
  )

  const val prefix = "="
  const val version = "2.0.0"

  val officialServerOnlyMode = System.getenv("OFFICIAL_SERVER_ONLY") != null

  val devs =
    mutableListOf("584915458302672916", "574951722645192734", "693914342625771551", "610861621287583752")

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
  }

  object Channels {
    const val supportCategory = "865291473612177428"

    const val totalGameStats = "865082667493097502"
    const val inQueueGameStats = "865083552676970496"
    const val inProgressGameStats = "865082532819369984"

    const val generalGuideChannel = "864714730529619998"
    const val liveSupportChannel = "864714786678505472"

    const val registrationProcessChannel = "864652159815516160"
    const val registrationChannel = "864294706832605224"
    const val welcomeChannel = "864652271832137768"

    const val commandsChannel = "864289771018911757"

    const val queueRoomChannel = "864703101067722782"
    const val queueCommandsChannel = "864703077650661376"
  }

  object Roles {
    const val registeredRole = "864597891262709780"
    const val vipRole = "865458492444901426"
    const val vipPlusRole = "865459582209753108"
    const val mvpRole = "865460245021982790"
    const val mvpPlusRole = "865459866734297099"
    const val mvpPlusPlusRole = "865460562578112523"
  }

  const val mainServer = "718872125490069534"
}
