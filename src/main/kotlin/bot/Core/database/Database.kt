package bot.Core.database

import bot.Core.database.models.Queue
import bot.Core.database.models.User
import bot.Core.database.repositories.QueueRepository
import com.mongodb.ClientSessionOptions
import com.mongodb.ConnectionString
import org.litote.kmongo.coroutine.*
import bot.Core.database.repositories.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.litote.kmongo.reactivestreams.KMongo
import java.util.concurrent.Executors

class Database() {
    private val client: CoroutineClient
    val database: CoroutineDatabase

    private val userCollection: CoroutineCollection<User>
    private val queueCollection: CoroutineCollection<Queue>

    val userRepository: UserRepository
    val queueRepository: QueueRepository

    init {
        val connectionString = ConnectionString(System.getenv("MONGO_URL") ?: "mongodb://localhost/hypixelq")
        client = KMongo.createClient(connectionString).coroutine
        database = client.getDatabase(connectionString.database ?: "hypixelq")

        queueCollection = database.getCollection()
        userCollection = database.getCollection()

        queueRepository = QueueRepository(this, queueCollection)
        userRepository = UserRepository(this, userCollection)

        CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher()).launch { createIndexes() }
    }

    private suspend fun createIndexes() {
        userRepository.createIndexes()
    }

    suspend fun startSession(options: ClientSessionOptions? = null) =
        if(options != null) client.startSession(options) else client.startSession()

    fun close() {
        client.close()
    }
}