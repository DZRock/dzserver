package ru.sleepyrabbit.dzserver

import kotlinx.coroutines.*
import ru.sleepyrabbit.dzserver.DataCoder
import ru.sleepyrabbit.dzserver.Router
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

class DzRestServer<T>(private val dataCoder: DataCoder<T>, private val router: Router<T>) {

    private lateinit var selector: Selector

    fun start() {
        val serverSocketChannel = ServerSocketChannel.open()
        selector = Selector.open();
        serverSocketChannel.configureBlocking(false)
        serverSocketChannel.socket().bind(InetSocketAddress(9000))
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)
        while (true) {
            selector.select()
            selector
                .selectedKeys()
                .removeIf {key ->
                    if (!key.isValid)
                        return@removeIf true

                    when {
                        key.isAcceptable -> accept(key)
                        key.isReadable -> read(key)
                        key.isWritable -> writeEmpty(key)
                    }
                    return@removeIf true
                }
        }
    }

    private fun accept(key: SelectionKey) {
        val serverChannel: ServerSocketChannel = key.channel() as ServerSocketChannel
        val channel = serverChannel.accept()
        channel.configureBlocking(false)
        channel.register(selector, SelectionKey.OP_READ)
    }

    private fun read(key: SelectionKey) {
        val channel = key.channel() as SocketChannel
        val buffer = ByteBuffer.allocate(1024)

        channel.read(buffer)

        if(!buffer.hasArray() || buffer.array().isEmpty())
            channel.register(selector, SelectionKey.OP_WRITE)

        GlobalScope.launch{
            val response = router.route(dataCoder.decode(buffer.array()))
            write(response, channel)
        }
    }

    private fun write(response: T, channel: SocketChannel) {
        val responseData = dataCoder.encode(response)
        val byteBuffer = ByteBuffer.allocate(responseData.size)

        byteBuffer.put(responseData)
        byteBuffer.flip()
        channel.write(byteBuffer)
        channel.close()
    }

    private fun writeEmpty(key: SelectionKey) {
        val channel = key.channel() as SocketChannel
        //TODO empty body error response
        channel.close()
    }
}