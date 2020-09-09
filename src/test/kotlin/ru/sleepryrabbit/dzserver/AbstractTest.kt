package ru.sleepryrabbit.dzserver

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import ru.sleepyrabbit.dzserver.DzRestServer
import ru.sleepyrabbit.dzserver.http.HttpDataCoder
import ru.sleepyrabbit.dzserver.http.HttpPacket
import ru.sleepyrabbit.dzserver.http.HttpRouter
import java.net.URI

@TestInstance(PER_CLASS)
abstract class AbstractTest {

    fun String.toUri(): URI {
        return URI(this)
    }

    lateinit var server: DzRestServer<HttpPacket>
    val httpRouter = HttpRouter()
    private val httpDataCoder = HttpDataCoder()

    @BeforeAll
    internal fun init() {
        println("Starting server ...")
        GlobalScope.launch(Dispatchers.Default) {
            server = DzRestServer(
                httpDataCoder,
                httpRouter
            )
            server.start()
        }
        println("Server start")
    }
}