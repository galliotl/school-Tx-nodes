package tx.nodes

import kotlin.concurrent.thread
import java.io.PrintWriter
import java.net.InetSocketAddress
import com.sun.net.httpserver.HttpServer
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException


/**
 * TODO: define
 */
class MasterNode() : Node(ip = "localhost", port = 7777) {
    private val httpPort = 7770

    override fun run() {
        // Start listening server
        thread { tcpServer() }
        thread{ httpserver() }
        thread{ idleCheck(10000) }
    }

    fun httpserver() {
        fun getQueryParams(params: String): String {
            var toReturn = "could not find query params"
            try {
                val p = Pattern.compile("=(.*)")
                val matcher = p.matcher(params)
                if (matcher.find()) {
                    toReturn = matcher.group(1)
                }
            } catch (ex: PatternSyntaxException) {
                ex.printStackTrace()
                // error handling
            }
            return toReturn
        }
        fun getQueryType(params: String): String {
            var toReturn = "could not find query params"
            try {
                val p = Pattern.compile("(.*)=")
                val matcher = p.matcher(params)
                if (matcher.find()) {
                    toReturn = matcher.group(1)
                }
            } catch (ex: PatternSyntaxException) {
                ex.printStackTrace()
            }
            return toReturn
        }

        val httpserver = HttpServer.create(InetSocketAddress(httpPort), 0).apply {
            createContext("/request") { http ->
                when (http.requestMethod.toString()) {
                    "GET" -> {
                        println("   MN: ${getQueryParams(http.requestURI.query.toString())}")
                        println("   MN: ${getQueryType(http.requestURI.query.toString())}")
                        http.responseHeaders.add("Content-type", "text/plain")
                        http.sendResponseHeaders(200, 0)
                        PrintWriter(http.responseBody).use { out ->
                            out.println("Hello ${http.remoteAddress.hostName}!")
                        }
                    }
                    "POST" -> {
                        http.responseHeaders.add("Content-type", "text/plain")
                        http.sendResponseHeaders(200, 0)
                        PrintWriter(http.responseBody).use { out ->
                            out.println("Hello ${http.remoteAddress.hostName}!")
                        }
                    }
                    else -> {
                        http.responseHeaders.add("Content-type", "text/plain")
                        http.sendResponseHeaders(200, 0)
                        PrintWriter(http.responseBody).use { out ->
                            out.println("Do not use it")
                        }
                    }
                }
            }
            start()
        }
    }
}

fun main() {
    val master = MasterNode()
    master.run()
}