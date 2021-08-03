package bot.Core.database

import bot.Core.database.models.*
import bot.Core.database.repositories.*
import com.mongodb.ClientSessionOptions
import com.mongodb.ConnectionString
import org.litote.kmongo.coroutine.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.litote.kmongo.reactivestreams.KMongo
import java.util.concurrent.Executors

class Database(cache: Cache) {
    private val client: CoroutineClient
    val database: CoroutineDatabase

    val configCollection: CoroutineCollection<Config>
    val gameCollection: CoroutineCollection<Game>
    val userCollection: CoroutineCollection<User>
    val queueCollection: CoroutineCollection<Queue>
    val archiveCollection: CoroutineCollection<Archive>
    val partyCollection: CoroutineCollection<Party>
    val partyInviteCollection: CoroutineCollection<PartyInvite>
    val settingCollection: CoroutineCollection<Setting>

    val configRepository: ConfigRepository
    val userRepository: UserRepository
    val queueRepository: QueueRepository
    val gameRepository: GameRepository
    val archiveRepository: ArchiveRepository
    val partyRepository: PartyRepository
    val partyInviteRepository: PartyInviteRepository
    val settingRepository: SettingRepository

    init {
        val connectionString = ConnectionString(System.getenv("MONGO_URL") ?: "mongodb://localhost/bedwarsq")
        client = KMongo.createClient(connectionString).coroutine
        database = client.getDatabase(connectionString.database ?: "bedwarsq")

        configCollection = database.getCollection()
        archiveCollection = database.getCollection()
        queueCollection = database.getCollection()
        gameCollection = database.getCollection()
        userCollection = database.getCollection()
        partyCollection = database.getCollection()
        partyInviteCollection = database.getCollection()
        settingCollection = database.getCollection()

        configRepository = ConfigRepository(this, configCollection)
        archiveRepository = ArchiveRepository(this, archiveCollection)
        queueRepository = QueueRepository(this, queueCollection)
        userRepository = UserRepository(this, userCollection, cache.userMap)
        gameRepository = GameRepository(this, gameCollection)
        partyRepository = PartyRepository(this, partyCollection)
        partyInviteRepository = PartyInviteRepository(this, partyInviteCollection)
        settingRepository = SettingRepository(this, settingCollection, cache.settingMap)

        CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher()).launch { createIndexes() }
    }

    private suspend fun createIndexes() {
        configRepository.createIndexes()
        archiveRepository.createIndexes()
        queueRepository.createIndexes()
        userRepository.createIndexes()
        gameRepository.createIndexes()
        partyRepository.createIndexes()
        partyInviteRepository.createIndexes()
        settingRepository.createIndexes()
    }

    suspend fun startSession(options: ClientSessionOptions? = null) =
        if(options != null) client.startSession(options) else client.startSession()

    fun close() {
        client.close()
    }
}