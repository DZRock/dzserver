package ru.sleepyrabbit.dzserver.http

import ru.sleepyrabbit.dzserver.DataCoder
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.StringReader
import java.nio.charset.Charset

class HttpDataCoder: DataCoder<HttpPacket> {

    override fun decode(data: ByteArray): HttpPacket {
        val reader = BufferedReader(StringReader(String(data)))
        val statusLine = reader.readLine()

        val headers = mutableMapOf<String, String>()
        var header = reader.readLine()
        while (header.isNotEmpty()) {
            val idx = header.indexOf(":")
            if (idx == -1) {
                throw HttpFormatException("Invalid Header Parameter: $header")
            }
            headers[header.substring(0, idx)]= header.substring(idx + 1, header.length)
            header = reader.readLine()
        }

        var bodyLine = reader.readLine()
        val stringBuilder = StringBuilder()
        while (bodyLine != null) {
            stringBuilder.appendln(bodyLine)
            bodyLine = reader.readLine()
        }

        return HttpPacket(statusLine, headers, stringBuilder.toString().toByteArray())
    }

    override fun encode(data: HttpPacket): ByteArray {
        val byteAOS = ByteArrayOutputStream()
        byteAOS.writeBytes(data.statusLine.toByteArray())
        byteAOS.writeBytes(NL_BYTES)
        data.headers.forEach { (key, value) ->
            byteAOS.writeBytes(key.toByteArray())
            byteAOS.writeBytes(KEY_VALUE_DELIMITER)
            byteAOS.writeBytes(value.toByteArray())
            byteAOS.writeBytes(NL_BYTES)
        }

        byteAOS.writeBytes(NL_BYTES)
        byteAOS.writeBytes(data.body)

        return byteAOS.toByteArray()
    }

    companion object {
        private val NL_BYTES = "\r\n".toByteArray()
        private val KEY_VALUE_DELIMITER = ":".toByteArray()
    }
}