package bot.utils

object Config {
  const val prefix = "="
  const val version = "2.0.0"

  val officialServerOnlyMode = System.getenv("OFFICIAL_SERVER_ONLY") != null

  val devs =
    mutableListOf("584915458302672916", "574951722645192734", "693914342625771551", "610861621287583752")

  object Channels {
    val howToPlayChannel = "864714721050361917"
    val generalGuideChannel = "864714730529619998"
    val liveSupportChannel = "864714786678505472"

    val registrationProcessChannel = ""
    val registrationChannel = "864294706832605224"
    val welcomeChannel = "864652271832137768"

    val commandsChannel = ""

    val queueRoomChannel = "864703101067722782"
    val queueCommandsChannel = "864703077650661376"
  }

  object Roles {
    val registeredRole = "864597891262709780"
  }

  const val mainServer = "718872125490069534"
  const val testingServer = "757972619986337823"

  val officialServers = listOf(
    mainServer,
    testingServer
  )

  object Emojis {
    val alphabet = listOf(
      "🇦",
      "🇧",
      "🇨",
      "🇩",
      "🇪",
      "🇫",
      "🇬",
      "🇭",
      "🇮",
      "🇯",
      "🇰",
      "🇱",
      "🇲",
      "🇳",
      "🇴",
      "🇵",
      "🇶",
      "🇷",
      "🇸",
      "🇹",
      "🇺",
      "🇻",
      "🇼",
      "🇽",
      "🇾",
      "🇿"
    )
  }
}
