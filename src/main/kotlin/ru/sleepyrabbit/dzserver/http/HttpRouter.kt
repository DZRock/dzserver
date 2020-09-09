package ru.sleepyrabbit.dzserver.http

import ru.sleepyrabbit.dzserver.IncorrectRouteException
import ru.sleepyrabbit.dzserver.Router

class HttpRouter: Router<HttpPacket>() {

    override fun route(message: HttpPacket): HttpPacket {
        val (requestMethod, path, protocol) = parseStatusLine(message.statusLine)

        val result = routes[requestMethod + "_$path"]?.invoke(preProcess(message))
        return postProcess(result ?: throw IncorrectRouteException(requestMethod+"_$path"))
    }

    fun bind(requestMethod: String, path: String, action: (Any) -> Any): Router<HttpPacket> {
        return super.bindRoute(requestMethod+"_$path", action)
    }

    fun unbind(requestMethod: String, path: String): Router<HttpPacket> {
        return super.unbindRoute(requestMethod+"_$path")
    }

    private fun parseStatusLine(statusLine: String) : List<String> {
        return statusLine.split(" ")
    }

    companion object {
        const val GET = "GET"
        const val POST = "POST"
        const val DELETE = "DELETE"
        const val PUT = "PUT"
        const val PATCH = "PATCH"
    }
}